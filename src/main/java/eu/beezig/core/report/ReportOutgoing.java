package eu.beezig.core.report;

import eu.beezig.core.net.util.PacketBuffer;

import java.util.EnumSet;

public class ReportOutgoing {
    private final ReportType type;
    private final String[] targets;
    private final EnumSet<ReportReason> reasons;

    public ReportOutgoing(ReportType type, String[] targets, EnumSet<ReportReason> reasons) {
        this.type = type;
        this.targets = targets;
        this.reasons = reasons;
    }

    public enum ReportType {
        BLOCK, PLAYER;
    }

    private long getReasonBits() {
        long result = 0;
        for(ReportReason reason : reasons) result |= 1L << reason.getIndex();
        return result;
    }

    public void writeTo(PacketBuffer buffer) {
        buffer.writeByte((byte) type.ordinal());
        buffer.writeInt(targets.length);
        for(String player : targets) buffer.writeString(player);
        buffer.writeLong(getReasonBits());
    }
}
