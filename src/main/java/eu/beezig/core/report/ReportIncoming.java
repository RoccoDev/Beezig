package eu.beezig.core.report;

import eu.beezig.core.net.util.PacketBuffer;

import java.util.EnumSet;

public class ReportIncoming {
    private ReportOutgoing.ReportType type;
    private int id;
    private String sender;
    private String[] targets;
    private EnumSet<ReportReason> reasons;

    public ReportOutgoing.ReportType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String[] getTargets() {
        return targets;
    }

    public EnumSet<ReportReason> getReasons() {
        return reasons;
    }

    private void setReasons(long bits) {
        this.reasons = EnumSet.noneOf(ReportReason.class);
        for(ReportReason reason : reasons) {
            long bit = 1L << reason.getIndex();
            if((bits & bit) == bit) reasons.add(reason);
        }
    }

    public static ReportIncoming readFrom(PacketBuffer buffer) {
        ReportIncoming result = new ReportIncoming();
        result.id = buffer.readInt();
        result.type = ReportOutgoing.ReportType.values()[(int)buffer.readByte()];
        result.sender = buffer.readString();
        result.targets = new String[buffer.readInt()];
        for(int i = 0; i < result.targets.length; i++) result.targets[i] = buffer.readString();
        result.setReasons(buffer.readLong());
        return result;
    }
}
