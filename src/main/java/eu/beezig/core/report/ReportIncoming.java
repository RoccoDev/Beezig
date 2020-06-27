package eu.beezig.core.report;

import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.StringUtils;

import java.util.Arrays;

public class ReportIncoming {
    private ReportOutgoing.ReportType type;
    private int id;
    private String sender;
    private String[] targets;
    private String[] reasons;
    private String claimer;
    private boolean handled;

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

    public String[] getReasons() {
        return reasons;
    }

    public boolean isClaimed() {
        return !claimer.isEmpty();
    }

    public boolean isHandled() {
        return handled;
    }

    public String formatReasons() {
        return Color.accent() + StringUtils.localizedJoin(Arrays.asList(reasons)) + Color.primary();
    }

    public String formatTargets() {
        return Color.accent() + StringUtils.localizedJoin(Arrays.asList(targets)) + Color.primary();
    }

    public static ReportIncoming readFrom(PacketBuffer buffer) {
        ReportIncoming result = new ReportIncoming();
        result.id = buffer.readInt();
        result.type = ReportOutgoing.ReportType.values()[(int)buffer.readByte()];
        result.sender = buffer.readString();
        result.targets = new String[buffer.readInt()];
        for(int i = 0; i < result.targets.length; i++) result.targets[i] = buffer.readString();
        result.reasons = new String[buffer.readInt()];
        for(int i = 0; i < result.reasons.length; i++) result.reasons[i] = buffer.readString();
        result.claimer = buffer.readString();
        result.handled = buffer.readBoolean();
        return result;
    }
}
