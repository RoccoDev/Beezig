package eu.beezig.core.net.packets;

import eu.beezig.core.Beezig;
import eu.beezig.core.Constants;
import eu.beezig.core.api.BeezigForge;
import eu.beezig.core.net.Packet;
import eu.beezig.core.net.Protocol;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.profile.OwnProfile;
import eu.beezig.core.net.profile.UserRole;
import eu.beezig.core.net.util.PacketBuffer;

public class PacketProfile implements Packet {

    // In
    private int id;
    private UserRole role;
    private long firstLogin;
    private String regionId;
    private String regionName;

    // Out
    private String timezone;

    public PacketProfile(String timezone) {
        this.timezone = timezone;
    }

    public PacketProfile() {}

    @Override
    public void read(PacketBuffer buffer) {
        id = buffer.readInt();
        role = UserRole.fromIndex(buffer.readByte());
        firstLogin = buffer.readLong();
        boolean regionOk = buffer.readBoolean();
        if(regionOk) {
            regionId = buffer.readString();
            regionName = buffer.readString();
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(Protocol.VERSION);
        buffer.writeString(timezone == null ? "" : timezone);
        buffer.writeString(Constants.VERSION);
        buffer.writeBoolean(BeezigForge.isSupported());
    }

    @Override
    public void handle(Connection handler) {
        Beezig.net().setProfile(new OwnProfile(id, role, firstLogin, regionId, regionName));
    }
}
