package eu.beezig.core.report;

import eu.beezig.core.net.util.PacketBuffer;

public class ReportOutgoing {
    private final ReportType type;
    private final String[] targets;
    private final String[] reasons;

    public ReportOutgoing(ReportType type, String[] targets, String[] reasons) {
        this.type = type;
        this.targets = targets;
        this.reasons = reasons;
    }

    public enum ReportType {
        BLOCK, PLAYER;
    }

    public void writeTo(PacketBuffer buffer) {
        buffer.writeByte((byte) type.ordinal());
        buffer.writeByte((byte) targets.length);
        for(String player : targets) buffer.writeString(player);
        buffer.writeByte((byte) reasons.length);
        for(String reason : reasons) buffer.writeString(reason);
    }
}
