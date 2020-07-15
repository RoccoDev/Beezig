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
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketUserSettings;
import eu.beezig.core.net.profile.UserRole;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToggleBeeCommand implements Command {
    @Override
    public String getName() {
        return "togglebee";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"/togglebee", "/btogglerank"};
    }

    private boolean isFakeRole;
    private long lastSettingTime;

    @Override
    public boolean execute(String[] args) {
        long now = System.currentTimeMillis();
        if(now - lastSettingTime < 5000) {
            Message.error(Beezig.api().translate("error.wait", (5000 - (now - lastSettingTime)) / 1000));
            return true;
        }
        UserRole current = Beezig.net().getProfile().getRole();
        if (args.length == 0) {
            UserRole changed = current;
            if(!isFakeRole) {
                if(current == UserRole.DEVELOPER) changed = UserRole.NONE;
                else changed = UserRole.USER;
            }
            Beezig.net().getHandler().sendPacket(new PacketUserSettings(changed));
            Message.info(Beezig.api().translate("msg.rank.toggle", changed.getDisplayName() + Color.primary()));
            isFakeRole = !isFakeRole;
            lastSettingTime = now;
            return true;
        }
        String newName = args[0];
        UserRole role;
        try {
            role = UserRole.valueOf(newName.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex) {
            String possibleValues = Stream.of(UserRole.values()).map(UserRole::name).collect(Collectors.joining(", "));
            Message.error(Beezig.api().translate("error.enum", "§6" + possibleValues));
            return true;
        }
        if(role == UserRole.NONE && current.compareTo(UserRole.DEVELOPER) < 0) {
            Message.error(Beezig.api().translate("error.rank.perm", UserRole.DEVELOPER.getDisplayName() + "§c"));
            return true;
        }
        if(current.compareTo(role) < 0) {
            Message.error(Beezig.api().translate("error.rank.perm", role.getDisplayName() + "§c"));
            return true;
        }
        Beezig.net().getHandler().sendPacket(new PacketUserSettings(role));
        Message.info(Beezig.api().translate("msg.rank.toggle", role.getDisplayName() + Color.primary()));
        isFakeRole = !isFakeRole;
        lastSettingTime = now;
        return true;
    }
}
