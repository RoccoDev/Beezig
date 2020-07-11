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

package eu.beezig.core.net.packets;

import eu.beezig.core.Beezig;
import eu.beezig.core.net.Packet;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.task.WorldTask;
import eu.beezig.core.util.text.Message;

public class PacketServerStats implements Packet {

    private int userCount;

    @Override
    public void read(PacketBuffer buffer) {
        userCount = buffer.readInt();
    }

    @Override
    public void write(PacketBuffer buffer) {

    }

    @Override
    public void handle(Connection handler) {
        WorldTask.submit(() -> Message.info(Beezig.api().translate("msg.server.count",
                Color.accent() + Message.formatNumber(userCount) + Color.primary())));
    }
}
