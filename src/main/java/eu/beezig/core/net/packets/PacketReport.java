package eu.beezig.core.net.packets;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.commands.ReportsCommand;
import eu.beezig.core.net.Packet;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.report.ReportIncoming;
import eu.beezig.core.report.ReportOutgoing;
import eu.beezig.core.util.text.Message;

public class PacketReport implements Packet {

    private Type type;
    private ReportOutgoing out;
    private ReportIncoming in;
    private int id;
    private boolean includeClaimed;
    private String message, sender;
    private ReportIncoming[] reports, claimed;

    // C->S, send outgoing report
    public static PacketReport newReport(ReportOutgoing report) {
        PacketReport result = new PacketReport();
        result.type = Type.NEW;
        result.out = report;
        return result;
    }

    // C->S, claim report
    public static PacketReport claim(int id) {
        PacketReport result = new PacketReport();
        result.type = Type.CLAIM;
        result.id = id;
        return result;
    }

    // C->S, send message in the report's chat
    public static PacketReport chat(int id, String message) {
        PacketReport result = new PacketReport();
        result.type = Type.CHAT;
        result.id = id;
        result.message = message;
        return result;
    }

    // C->S, handle report
    public static PacketReport handle(int id) {
        PacketReport result = new PacketReport();
        result.type = Type.HANDLE;
        result.id = id;
        return result;
    }

    // C->S, list reports
    public static PacketReport request(boolean includeClaimed) {
        PacketReport result = new PacketReport();
        result.type = Type.REQUEST;
        result.includeClaimed = includeClaimed;
        return result;
    }

    // S->C, new incoming report
    public PacketReport() {}

    @Override
    public void read(PacketBuffer buffer) {
        type = Type.values()[buffer.readByte()];
        if(type == Type.NEW || type == Type.NEW_INCOMING || type == Type.CHAT) in = ReportIncoming.readFrom(buffer);
        else if(type == Type.CLAIM || type == Type.HANDLE) id = buffer.readInt();
        if(type == Type.CHAT) {
            sender = buffer.readString();
            message = buffer.readString(buffer.readInt());
        }
        if(type == Type.REQUEST) {
            reports = new ReportIncoming[buffer.readInt()];
            for(int i = 0; i < reports.length; i++) reports[i] = ReportIncoming.readFrom(buffer);
            claimed = new ReportIncoming[buffer.readInt()];
            for(int i = 0; i < claimed.length; i++) claimed[i] = ReportIncoming.readFrom(buffer);
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeByte((byte) type.ordinal());
        if(type == Type.NEW) out.writeTo(buffer);
        else if(type == Type.CLAIM || type == Type.HANDLE) buffer.writeInt(id);
        else if(type == Type.REQUEST) buffer.writeBoolean(includeClaimed);
        else if(type == Type.CHAT) {
            buffer.writeInt(id);
            buffer.writeBigString(message);
        }
    }

    @Override
    public void handle(Connection handler) {
        if(type == Type.NEW) {
            Message.info(Beezig.api().translate("msg.report.submitted", in.formatTargets(), in.formatReasons()));
        }
        else if(type == Type.NEW_INCOMING) {
            Message.info(Beezig.api().translate("msg.report.incoming", in.getSender(), in.formatTargets(), in.formatReasons()));
        }
        else if(type == Type.CLAIM) Message.info(Beezig.api().translate("msg.report.claim", id));
        else if(type == Type.HANDLE) Message.info(Beezig.api().translate("msg.report.handle", id));
        else if(type == Type.CHAT) Message.info(Beezig.api().translate("msg.report.chat", in.formatTargets(), sender, message));
        else if(type == Type.REQUEST) ReportsCommand.sendResult(reports, claimed);
        else if(type == Type.SET_MOD) Beezig.get().setMod(true);
    }

    private enum Type {
        REQUEST, NEW, CLAIM, HANDLE, CHAT, NEW_INCOMING, SET_MOD
    }
}
