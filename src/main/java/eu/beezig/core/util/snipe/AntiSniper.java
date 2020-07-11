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

package eu.beezig.core.util.snipe;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.TextButton;
import eu.the5zig.mod.event.ChatSendEvent;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.server.IPatternResult;
import eu.the5zig.mod.util.component.MessageComponent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiSniper {
    private String lastSender;
    private String lastBroadcastSender;
    private boolean kickedFromParty;
    private Pattern commandTypoRegex;

    public AntiSniper() {
        try {
            loadRegex();
        } catch (IOException e) {
            Beezig.logger.error("Couldn't load the command typo regex", e);
        }
    }

    private void loadRegex() throws IOException {
        File file = new File(Beezig.get().getBeezigDir(), "commandTypos.txt");
        List<String> commands;
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            String[] defaults = new String[] {"r", "reply", "msg", "t", "traitor", "detective", "p", "tell", "message"};
            commands = Arrays.asList(defaults);
            FileUtils.writeLines(file, "UTF-8", commands);
        }
        else {
            commands = FileUtils.readLines(file, "UTF-8");
        }
        commandTypoRegex = Pattern.compile("(?:7|[^/]/)((" + String.join("|", commands) + ").*)", Pattern.CASE_INSENSITIVE);
    }

    @EventHandler
    public void onChatSend(ChatSendEvent event) {
        if(!ServerHive.isCurrent()) return;
        String msg = event.getMessage();
        if(commandTypoRegex != null && Settings.SNIPE_TYPO.get().getBoolean()) {
            Matcher m = commandTypoRegex.matcher(msg);
            if (m.matches()) {
                event.setCancelled(true);
                String cmd = m.group(1);
                sendTypo(new BlockAction(msg, "/" + cmd));
            }
        }
        if(lastSender != null && Settings.SNIPE_PMS.get().getBoolean()) {
            if(msg.toLowerCase(Locale.ROOT).startsWith("/r ")) {
                event.setCancelled(true);
                String[] args = msg.split(" ");
                String reply = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                Beezig.api().sendPlayerMessage("/msg " + lastSender + " " + reply);
            }
        }
    }

    public void onMatch(String key, IPatternResult match) {
        if("msg.private".equals(key)) {
            String sender = match.get(0);
            if(!sender.equalsIgnoreCase(Beezig.user().getName()))
                lastSender = sender;
        }
        if("msg.broadcast".equals(key)) {
            lastBroadcastSender = match.get(0);
            String text = match.get(2);
            MessageComponent base = new MessageComponent(Message.infoPrefix());
            TextButton reply = new TextButton("btn.broadcast.reply.name", "btn.broadcast.reply.desc", "§a");
            reply.doSuggestCommand("/msg " + lastBroadcastSender + " ");
            TextButton reBroadcast = new TextButton("btn.broadcast.re.name", "btn.broadcast.re.desc", "§b");
            reBroadcast.doRunCommand("/friend broadcast " + text);
            base.getSiblings().add(reply);
            base.getSiblings().add(new MessageComponent(" "));
            base.getSiblings().add(reBroadcast);
            Beezig.get().getAsyncExecutor().schedule(() -> Beezig.api().messagePlayerComponent(base, false), 1, TimeUnit.MILLISECONDS);
        }
    }

    private void sendTypo(BlockAction action) {
        String text = Message.infoPrefix() + Beezig.api().translate("msg.snipe.blocked",
                Color.accent() + action.getWithFix() + Color.primary());
        MessageComponent main = new MessageComponent(text + "\n");
        main.getSiblings().add(action.getButtons());
        TextButton disable = new TextButton("btn.snipe.disable", "btn.snipe.disable", "§e");
        disable.doRunCommand("/bsettings snipe.typo false");
        main.getSiblings().add(new MessageComponent(" "));
        main.getSiblings().add(disable);
        Beezig.api().messagePlayerComponent(main, false);
    }

    public String getLastBroadcastSender() {
        return lastBroadcastSender;
    }
}
