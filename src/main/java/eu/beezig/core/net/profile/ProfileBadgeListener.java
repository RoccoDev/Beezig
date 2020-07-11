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

package eu.beezig.core.net.profile;

import eu.beezig.core.Beezig;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.util.UUIDUtils;
import eu.the5zig.mod.event.ChatEvent;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.util.minecraft.ChatColor;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileBadgeListener {

    private static final Pattern LIST_REGEX = Pattern.compile("§8▍ .+ ▏ §3Online (?:spectators|participants) \\(.+\\): .+");

    @EventHandler
    public void onChat(ChatEvent event) {
        if(!ServerHive.isCurrent()) return;
        Matcher m = LIST_REGEX.matcher(event.getMessage());
        if(m.matches()) {
            String[] parts = event.getMessage().split(":");
            MessageComponent main = new MessageComponent("§a" + parts[0] + ": ");
            String[] players = parts[1].split(", ");
            for(int i = 0; i < players.length; i++) {
                String player = players[i].replace(".", "").trim();
                MessageComponent cmp = new MessageComponent(player);
                UUID id = UUIDUtils.getLocalUUID(ChatColor.stripColor(player));
                if(id != null) cmp.getSiblings().add(UUIDUtils.getUserRole(id));
                main.getSiblings().add(cmp);
                if(i != players.length - 1) main.getSiblings().add(new MessageComponent("§3, "));
            }
            main.getSiblings().add(new MessageComponent("§3."));
            event.setCancelled(true);
            Beezig.api().messagePlayerComponent(main, false);
        }
    }
}
