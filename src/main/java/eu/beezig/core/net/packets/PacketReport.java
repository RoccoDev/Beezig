package eu.beezig.core.net.packets;

import eu.beezig.core.net.Packet;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.report.ReportIncoming;
import eu.beezig.core.report.ReportOutgoing;

public class PacketReport implements Packet {

    private ReportOutgoing out;
    private ReportIncoming in;

    // C->S, send outgoing report
    public PacketReport(ReportOutgoing report) {
        this.out = report;
    }

    // S->C, new incoming report
    public PacketReport() {}

    @Override
    public void read(PacketBuffer buffer) {
        in = ReportIncoming.readFrom(buffer);
    }

    @Override
    public void write(PacketBuffer buffer) {
        out.writeTo(buffer);
    }

    @Override
    public void handle(Connection handler) {

    }
}
