package eu.beezig.core.net.packets;

import eu.beezig.core.net.Packet;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.profile.UserRole;
import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.util.task.WorldTask;
import eu.beezig.core.util.text.Message;

public class PacketUserSettings implements Packet {

    private UserRole newRole;

    public PacketUserSettings(UserRole newRole) {
        this.newRole = newRole;
    }

    public PacketUserSettings() {}

    @Override
    public void read(PacketBuffer buffer) {

    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeByte((byte)newRole.getIndex());
    }

    @Override
    public void handle(Connection handler) {
        WorldTask.submit(() -> Message.info(Message.translate("msg.config.save")));
    }
}
