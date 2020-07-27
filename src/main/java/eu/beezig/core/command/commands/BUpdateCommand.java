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
import eu.beezig.core.command.Command;
import eu.beezig.core.util.text.Message;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;

public class BUpdateCommand implements Command {

    private static long confirmUntil = 0L;
    private static boolean updated = false;
    private static String code;

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
        if (updated) {
            Message.error(Beezig.api().translate("update.error.already_updated"));
            return true;
        }
        if (args.length == 1 && args[0].equals("confirm")) {
            if (System.currentTimeMillis() > confirmUntil)
            {
                Message.error(Beezig.api().translate("update.error.expired"));
                return true;
            }
            Beezig.get().getAsyncExecutor().execute(() -> {
                try {
                    URL updateUrl = new URL(String.format("https://go.beezig.eu/%s-beta", code));
                    URLConnection connection = updateUrl.openConnection();
                    // We need this to "bypass" Cloudflare
                    connection.setRequestProperty("User-Agent", String.format("Beezig/7.0 (%s) Beezig/%s-%s",
                            (SystemUtils.IS_OS_MAC ? "Macintosh" : System.getProperty("os.name")),
                            Constants.VERSION, Beezig.getVersion()));
                    ReadableByteChannel byteChannel = Channels.newChannel(connection.getInputStream());
                    FileChannel fileChannel = new FileOutputStream(
                            new File(Beezig.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getChannel();
                    fileChannel.transferFrom(byteChannel, 0, Long.MAX_VALUE);
                    updated = true;
                    Message.info(Beezig.api().translate("update.success"));
                } catch (MalformedURLException e) {
                    Message.error((Beezig.api().translate("update.error.code.invalid")));
                } catch (Exception e) {
                    Message.error(Beezig.api().translate("update.error"));
                    // We're leaving this error message untranslated
                    Message.error(e.getMessage());
                    // TODO Add Discord link?
                    e.printStackTrace();
                }
            });
            return true;
        }

        code = Beezig.get().isLaby() ? "laby" : "5zig";
        // Timeout in minutes for the /bupdate confirm command
        int confirm_timeout = 2;
        if (args.length == 2 && args[0].equalsIgnoreCase("code")) {
            // Use a custom beta (format: code-platform)
            code = String.format("%s-%s", args[1], code);
            Message.info(Beezig.api().translate("update.confirm.custom", code));
            confirmUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(confirm_timeout);
        } else if (args.length > 0) {
            Message.error(Beezig.api().translate("update.syntax"));
        } else {
            // Use the latest beta
            Message.info(Beezig.api().translate("update.confirm"));
            confirmUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(confirm_timeout);
        }
        return true;
    }
}
