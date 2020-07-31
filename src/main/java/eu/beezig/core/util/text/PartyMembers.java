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

package eu.beezig.core.util.text;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.util.Color;
import eu.the5zig.mod.event.ChatEvent;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;
import eu.the5zig.util.minecraft.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyMembers {
    private static final Pattern MSG_REGEX = Pattern.compile("▍ Party ▏ .+ wants you to join their party!");
    private static final Pattern MEMBERS_REGEX = Pattern.compile("[^,:. ]+");
    private static final Pattern FRIEND_REGEX = Pattern.compile("▍ Friends ▏ ✚ ([^\\s]+)");

    @EventHandler
    public void onChat(ChatEvent event) {
        if(event.getChatComponent() instanceof MessageComponent) {
            if(Settings.CHAT_PARTY.get().getBoolean()) {
                MessageComponent comp = (MessageComponent) event.getChatComponent();
                if (comp.getSiblings().size() == 0) return;
                String original = ChatColor.stripColor(comp.getText());
                Matcher party = MSG_REGEX.matcher(original);
                if (party.matches()) {
                    Matcher members = MEMBERS_REGEX.matcher(comp.getSiblings().get(comp.getSiblings().size() - 1)
                            .getStyle().getOnHover().getComponent().getText());
                    if (!members.find()) return;
                    if (!members.find()) return;
                    List<String> memberList = new ArrayList<>();
                    while (members.find()) memberList.add(members.group(0));
                    String display = String.join(", ", memberList.subList(0, memberList.size() - 1));
                    event.setCancelled(true);
                    Beezig.api().messagePlayer(event.getMessage());
                    Message.info(Beezig.api().translate("msg.party.members", display));
                    Beezig.api().messagePlayer("");
                }
            }
            if(Settings.CHAT_FRIENDS.get().getBoolean()) {
                String original = ChatColor.stripColor(event.getMessage());
                Matcher friends = FRIEND_REGEX.matcher(original);
                if(friends.matches()) {
                    String friend = friends.group(1);
                    MessageComponent msg = new MessageComponent(event.getMessage() + " ");
                    msg.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT,
                            new MessageComponent(Color.primary() + Beezig.api().translate("msg.party.friend", Color.accent() + friend + Color.primary()))));
                    msg.getStyle().setOnClick(new MessageAction(MessageAction.Action.RUN_COMMAND, "/party invite " + friend));
                    event.setCancelled(true);
                    Beezig.api().messagePlayerComponent(msg, false);
                }
            }
        }
    }
}
