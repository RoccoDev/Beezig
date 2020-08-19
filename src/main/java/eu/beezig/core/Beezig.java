/*
 * Copyright (C) 2017-2020 Beezig Team
 *
 * This file is part of Beezig.
 *
 * Beezig is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beezig is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beezig.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.beezig.core;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import eu.beezig.core.advrec.anywhere.AdvancedRecordsAnywhere;
import eu.beezig.core.api.BeezigServiceLoader;
import eu.beezig.core.command.CommandManager;
import eu.beezig.core.config.BeezigConfiguration;
import eu.beezig.core.data.BeezigData;
import eu.beezig.core.logging.TemporaryPointsManager;
import eu.beezig.core.modules.Modules;
import eu.beezig.core.net.BeezigNetManager;
import eu.beezig.core.net.session.NetSessionManager;
import eu.beezig.core.net.session.The5zigProvider;
import eu.beezig.core.notification.NotificationManager;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.util.DirectoryMigration;
import eu.beezig.core.util.process.ProcessManager;
import eu.beezig.core.util.snipe.AntiSniper;
import eu.beezig.core.util.task.WorldTaskManager;
import eu.beezig.core.util.text.LinkSnipper;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.PartyMembers;
import eu.beezig.hiveapi.wrapper.HiveWrapper;
import eu.the5zig.mod.ModAPI;
import eu.the5zig.mod.The5zigAPI;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.event.LoadEvent;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.mod.plugin.Plugin;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Plugin(name = "Beezig", version = Constants.VERSION)
public class Beezig {
    public static Logger logger;
    public static Gson gson = new Gson();
    public static boolean DEBUG = false;
    private static Beezig instance;

    private ModAPI api;
    private ScheduledExecutorService asyncExecutor;
    private BeezigConfiguration config;
    private File beezigDir;
    private BeezigData data;
    private BeezigNetManager networkManager;
    private WorldTaskManager worldTaskManager;
    private TemporaryPointsManager temporaryPointsManager;
    private NotificationManager notificationManager;
    private AntiSniper antiSniper;
    private ProcessManager processManager;
    private BeezigServiceLoader serviceLoader;
    private boolean laby;
    private boolean titleDebug;
    private boolean isMod;

    public Beezig(boolean laby, File labyDir) {
        this.laby = laby;
        if(labyDir != null) {
            this.beezigDir = new File(labyDir, "Beezig");
            beezigDir.mkdirs();
        }
    }

    public Beezig() {
        this(false, null);
        try {
            NetSessionManager.provider = new The5zigProvider();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        try {
            return new Scanner(Beezig.class.getResourceAsStream("/version.txt"), "UTF-8").useDelimiter("\\A").next();
        } catch (Exception e) {
            logger.error(String.format("Couldn't load commit from version.txt: %s", e.getMessage()));
            return "development";
        }
    }

    @EventHandler
    public void load(LoadEvent event) {
        IOverlay progress = The5zigAPI.getAPI().createOverlay();
        progress.displayMessage("Beezig", "Loading...");
        setupLogger();
        logger.info("Load started");
        long timeStart = System.currentTimeMillis();

        // Init fields
        instance = this;
        api = The5zigAPI.getAPI();
        asyncExecutor = Executors.newScheduledThreadPool(10);
        worldTaskManager = new WorldTaskManager();
        api.getPluginManager().registerListener(this, worldTaskManager);
        api.getPluginManager().registerListener(this, new PartyMembers());
        HiveWrapper.setAsyncExecutor(asyncExecutor);
        HiveWrapper.setUserAgent(Message.getUserAgent());

        // Init configuration
        try {
            if(beezigDir == null) {
                File minecraftDir = new File(Beezig.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .getParentFile().getParentFile().getParentFile();
                beezigDir = new File(minecraftDir, "Beezig");
                DirectoryMigration.migrateIfNeeded(new File(minecraftDir, "5zigtimv"), beezigDir);
                if (!beezigDir.exists() && !beezigDir.mkdir())
                    throw new RuntimeException("Could not create config dir.");
            }
            config = new BeezigConfiguration();
            config.load(new File(beezigDir, "config.json"));
        } catch(Exception e) {
            logger.error("Could not create config directory! Aborting load.");
            e.printStackTrace();
            return;
        }

        data = new BeezigData(beezigDir);
        try {
            data.tryUpdate();
        } catch (Exception e) {
            logger.error("Couldn't update data!");
            e.printStackTrace();
        }

        DirectoryMigration.migrateFolders(beezigDir);

        temporaryPointsManager = new TemporaryPointsManager();
        try {
            temporaryPointsManager.init();
        } catch (ReflectiveOperationException e) {
            logger.error("Couldn't load temporary points.", e);
        }

        antiSniper = new AntiSniper();
        api.getPluginManager().registerListener(this, antiSniper);
        api.getPluginManager().registerListener(this, new LinkSnipper());

        // Register Hive stuff
        api.registerServerInstance(this, ServerHive.class);
        Modules.register(this, api);
        CommandManager.init(this);
        notificationManager = new NotificationManager();
        processManager = new ProcessManager();
        AdvancedRecordsAnywhere.register();

        networkManager = new BeezigNetManager();
        networkManager.connect();

        serviceLoader = new BeezigServiceLoader();
        serviceLoader.attemptLoad();

        logger.info(String.format("Load complete in %d ms.", System.currentTimeMillis() - timeStart));
        progress.displayMessage("Beezig", "Loaded!");
    }

    private void setupLogger() {
        logger = LogManager.getLogger("Beezig");
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        LoggerConfig cfg = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        if("true".equals(System.getProperty("beezig.debug"))) {
            DEBUG = true;
            cfg.setLevel(Level.DEBUG);
        }
        ctx.updateLoggers();
        logger.debug("Debug is active.");
    }

    public boolean isMod() {
        return isMod;
    }

    public void setMod(boolean mod) {
        isMod = mod;
    }

    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public BeezigConfiguration getConfig() {
        return config;
    }

    public BeezigData getData() {
        return data;
    }

    public boolean isLaby() {
        return laby;
    }

    public File getBeezigDir() {
        return beezigDir;
    }

    public BeezigNetManager getNetworkManager() {
        return networkManager;
    }

    public WorldTaskManager getWorldTaskManager() {
        return worldTaskManager;
    }

    public TemporaryPointsManager getTemporaryPointsManager() {
        return temporaryPointsManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public AntiSniper getAntiSniper() {
        return antiSniper;
    }

    public ProcessManager getProcessManager() {
        return processManager;
    }

    public BeezigServiceLoader getServiceLoader() {
        return serviceLoader;
    }

    public boolean isTitleDebugEnabled() {
        return titleDebug;
    }

    public boolean toggleTitleDebug() {
        return titleDebug = !titleDebug;
    }

    public static Beezig get() {
        return instance;
    }

    public static BeezigConfiguration cfg() {
        return instance.config;
    }

    public static ModAPI api() {
        return instance.api;
    }

    public static GameProfile user() {
        return instance.api.getGameProfile();
    }

    public static BeezigNetManager net() {
        return instance.networkManager;
    }
}
