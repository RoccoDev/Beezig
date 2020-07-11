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
import eu.beezig.core.net.packets.PacketServerStats;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.text.Message;

public class OnBeezigCommand implements Command {
    @Override
    public String getName() {
        return "onbeezig";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"/ob", "/onbeezig"};
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            Beezig.net().getHandler().sendPacket(new PacketServerStats());
            return true;
        }
        UUIDUtils.getUUID(args[0])
                .thenAcceptAsync(uuid -> Beezig.net().getProfilesCache().getProfile(uuid)
                        .thenAcceptAsync(profile -> {
                            if (!profile.isPresent())
                                Message.error(Beezig.api().translate("msg.user.offline", args[0]));
                            else Message.info(Beezig.api().translate("msg.user.online",
                                    Color.accent() + args[0] + Color.primary(), profile.get().getRole().getDisplayName()));
                        })).exceptionally(e -> {
                            Message.error(Message.translate("error.online_users"));
                            Beezig.logger.error(e);
                            return null;
        });
        return true;
    }
}
