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
import eu.beezig.core.api.BeezigForge;
import eu.beezig.core.command.Command;
import eu.beezig.core.util.text.Message;
import org.apache.commons.lang3.SystemUtils;

import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
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
    private static AtomicBoolean updated = new AtomicBoolean(false);
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
        if (updated.get()) {
            Message.error(Message.translate("update.error.already_updated"));
            return true;
        }
        if (args.length > 0) {
            switch (args[0]) {
                case "confirm":
                    try {
                        Beezig beezig = Beezig.get();
                        if (System.currentTimeMillis() > confirmUntil) {
                            Message.error(Message.translate("update.error.expired"));
                            return true;
                        }
                        Map<URL, Class<?>> updates = new HashMap<>(4);
                        if (beezig.isLaby()) {
                            // Only update the BeezigLaby jar
                            updates.put(new URL("https://go.beezig.eu/" + code + "laby-beta"), Class.forName("eu.beezig.laby.LabyMain"));
                        } else {
                            if (BeezigForge.isSupported() && beezig.getBeezigForgeUpdateAvailable()) {
                                updates.put(new URL("https://go.beezig.eu/beezigforge-beta"), Class.forName("eu.beezig.forge.BeezigForgeMod"));
                            }
                            // Update Beezig even if no update is available
                            if (beezig.getUpdateAvailable() || updates.isEmpty()) {
                                updates.put(new URL("https://go.beezig.eu/" + code + "5zig-beta"), Beezig.class);
                            }
                        }
                        final String userAgent = String.format("Beezig/7.0 (%s) Beezig/%s-%s",
                            (SystemUtils.IS_OS_MAC ? "Macintosh" : System.getProperty("os.name")),
                            Constants.VERSION, Beezig.getVersionString());
                        updates.forEach((k, v) -> {
                            try {
                                URLConnection connection = k.openConnection();
                                connection.setRequestProperty("User-Agent", userAgent);
                                ReadableByteChannel byteChannel = Channels.newChannel(connection.getInputStream());
                                URL jarLocation = v.getProtectionDomain().getCodeSource().getLocation();
                                String jarFile = jarLocation.getFile();
                                if (jarLocation.getProtocol().equals("jar")) {
                                    jarFile = jarFile.substring(0, jarFile.lastIndexOf("!"));
                                }
                                FileChannel fileChannel = new FileOutputStream(new URI(jarFile).getPath()).getChannel();
                                fileChannel.transferFrom(byteChannel, 0, Long.MAX_VALUE);
                            } catch (Exception e) {
                                Message.error(Message.translate("update.error"));
                                Message.error(e.getMessage());
                                e.printStackTrace();
                            }
                        });
                        updated.set(true);
                        Message.info(Message.translate("update.success"));
                    } catch (MalformedURLException e) {
                        Message.error(Message.translate("update.invalid"));
                    } catch (ClassNotFoundException e) {
                        Message.info(Message.translate("update.error"));
                        Message.error(e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case "code":
                    if (args.length == 2) {
                        code = args[1] + "-";
                        Message.info(Beezig.api().translate("update.confirm.custom", code));
                        confirmUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
                        break;
                    }
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
