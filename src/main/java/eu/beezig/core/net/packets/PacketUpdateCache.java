package eu.beezig.core.net.packets;

import eu.beezig.core.Beezig;
import eu.beezig.core.net.Packet;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.util.PacketBuffer;

import java.util.UUID;

public class PacketUpdateCache implements Packet {

    private UUID uuid;
    private byte newRole;

    @Override
    public void read(PacketBuffer buffer) {
        uuid = buffer.readUUID();
        newRole = buffer.readByte();
    }

    @Override
    public void write(PacketBuffer buffer) {

    }

    @Override
    public void handle(Connection handler) {
        Beezig.net().getProfilesCache().update(uuid, newRole);
    }
}
