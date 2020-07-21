package eu.beezig.core.net.packets;

import eu.beezig.core.Beezig;
import eu.beezig.core.net.Packet;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.util.task.WorldTask;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;

public class PacketServerMessage implements Packet {
    private Type type;
    private String key;
    private String[] format;

    @Override
    public void read(PacketBuffer buffer) {
        type = Type.values()[(int)buffer.readByte()];
        key = buffer.readString(buffer.readInt());
        if(type != Type.ANNOUNCEMENT) {
            format = new String[buffer.readInt()];
            for (int i = 0; i < format.length; i++) format[i] = buffer.readString(buffer.readInt());
        }
    }

    @Override
    public void write(PacketBuffer buffer) {

    }

    @Override
    public void handle(Connection handler) {
        if(type == Type.ANNOUNCEMENT) {
            WorldTask.submit(this::sendAnnouncement);
            return;
        }
        String message = Beezig.api().translate(key, (Object[]) format);
        WorldTask.submit(() -> {
            if (type == Type.INFO) Message.info(message);
            else Message.error(message);
        });
    }

    private void sendAnnouncement() {
        Beezig.api().messagePlayer(StringUtils.linedCenterText("§7", "§b§l" + Message.translate("msg.announcement")));
        Beezig.api().messagePlayer(" §b" + key);
        Message.bar();
        Beezig.api().playSound("note.pling", 1f);
    }

    private enum Type {
        INFO, ERROR, ANNOUNCEMENT
    }
}
