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

package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.Constants;
import eu.beezig.core.Version;
import eu.beezig.core.api.BeezigForge;
import eu.beezig.core.command.Command;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.text.Message;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BUpdateCommand implements Command {

    private static long confirmUntil = 0L;
    private static final AtomicBoolean updated = new AtomicBoolean(false);
    private static String code = "";

    @Override
    public String getName() {
        return "bupdate";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/bupdate"};
    }

    @Override
    public boolean execute(String[] args) {
        Beezig beezig = Beezig.get();
        Version beezigVersion = beezig.getVersion();
        if (beezig.isLaby() && beezigVersion.getType().equals("release")) return false;
        if (updated.get()) {
            Message.error(Message.translate("update.error.already_updated"));
            return true;
        }
        if (args.length > 0) {
            switch (args[0]) {
                case "confirm":
                    try {
                        if (System.currentTimeMillis() > confirmUntil) {
                            Message.error(Message.translate("update.error.expired"));
                            return true;
                        }
                        try {
                            beezig.fetchRemoteVersions();
                        } catch (IOException e) {
                            ExceptionHandler.catchException(e);
                        }
                        Map<URI, Class<?>> updates = new HashMap<>(4);
                        if (beezig.isLaby()) {
                            // Only update the BeezigLaby jar
                            updates.put(new URI("https://go.beezig.eu/" + code + "laby-" + beezigVersion.getType()),
                                Class.forName("eu.beezig.laby.LabyMain"));
                        } else {
                            if (BeezigForge.isSupported() && beezig.getBeezigForgeUpdateAvailable()) {
                                updates.put(new URI("https://go.beezig.eu/beezigforge-" + beezigVersion.getType()),
                                    Class.forName("eu.beezig.forge.BeezigForgeMod"));
                            }
                            // Update Beezig even if no update is available
                            if (beezig.getUpdateAvailable() || updates.isEmpty()) {
                                updates.put(new URI("https://go.beezig.eu/" + code + "5zig-"+ beezigVersion.getType()), Beezig.class);
                            }
                        }
                        final String userAgent = String.format("Beezig/7.0 (%s) Beezig/%s-%s",
                            (SystemUtils.IS_OS_MAC ? "Macintosh" : System.getProperty("os.name")),
                            Constants.VERSION, Beezig.getVersionString());
                        updates.forEach((k, v) -> {
                            try {
                                URLConnection connection = k.toURL().openConnection();
                                connection.setRequestProperty("User-Agent", userAgent);
                                ReadableByteChannel byteChannel = Channels.newChannel(connection.getInputStream());
                                URL jarLocation = v.getProtectionDomain().getCodeSource().getLocation();
                                String jarFile = jarLocation.getFile();
                                if (jarLocation.getProtocol().equals("jar")) {
                                    jarFile = jarFile.substring(0, jarFile.lastIndexOf("!"));
                                }
                                File currentJar = new File(new URI(jarFile).getPath());
                                Version remoteVersion;
                                switch (v.getName()) {
                                    case "eu.beezig.laby.LabyMain":
                                        remoteVersion = beezig.getRemoteLabyVersion();
                                        jarFile = "BeezigLaby-" + Constants.VERSION;
                                        break;
                                    case "eu.beezig.forge.BeezigForgeMod":
                                        remoteVersion = beezig.getRemoteBeezigForgeVersion();
                                        jarFile = "BeezigForge-" + Constants.VERSION;
                                        break;
                                    default:
                                        jarFile = "Beezig-" + Constants.VERSION;
                                        remoteVersion = beezig.getRemoteVersion();
                                }
                                String newPath = String.format("%s%s%s-%s", currentJar.getParent(), File.separator, jarFile,
                                    remoteVersion == null ? "unknown" : remoteVersion.getCommit().substring(0, 8));
                                String currentJarName = currentJar.getName();
                                if (newPath.endsWith(currentJarName.substring(0, currentJarName.lastIndexOf(".jar")))) {
                                    newPath += "-new.jar";
                                } else {
                                    newPath += ".jar";
                                }
                                FileChannel fileChannel = new FileOutputStream(newPath).getChannel();
                                fileChannel.transferFrom(byteChannel, 0, Long.MAX_VALUE);
                                currentJar.deleteOnExit();
                            } catch (Exception e) {
                                Message.error(Message.translate("update.error"));
                                Message.error(e.getMessage());
                                ExceptionHandler.catchException(e);
                            }
                        });
                        updated.set(true);
                        Message.info(Message.translate("update.success"));
                    } catch (URISyntaxException e) {
                        Message.error(Message.translate("update.invalid"));
                    } catch (ClassNotFoundException e) {
                        Message.info(Message.translate("update.error"));
                        Message.error(e.getMessage());
                        ExceptionHandler.catchException(e);
                    }
                    break;
                case "code":
                    if (args.length == 2) {
                        code = args[1] + "-";
                        Message.info(Beezig.api().translate("update.confirm.custom", code));
                        confirmUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
                        break;
                    }
                    // fall through
                default:
                    Message.error(Message.translate("update.syntax"));
                    break;
            }
        } else {
            // Use the latest beta
            Message.info(Beezig.api().translate("update.confirm"));
            confirmUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
        }
        return true;
    }
}
