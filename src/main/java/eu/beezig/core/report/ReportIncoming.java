package eu.beezig.core.report;

import eu.beezig.core.Beezig;
import eu.beezig.core.net.util.PacketBuffer;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.core.util.text.TextButton;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;

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

    public MessageComponent getForList(boolean modDisplay) {
        StringBuilder sb = new StringBuilder();
        sb.append(Color.primary()).append(" - ").append(formatTargets()).append(" (").append(formatReasons()).append(") ");
        if(!modDisplay && isClaimed()) {
            sb.append(" [§a§l").append(Message.translate("msg.report.claimed")).append(Color.primary()).append("]");
        }
        MessageComponent comp = new MessageComponent(sb.toString());
        comp.getStyle().setOnHover(new MessageAction(MessageAction.Action.SHOW_TEXT, new MessageComponent(
            modDisplay
            ? String.format("%s#%d%s, %s", Color.accent(), id, Color.primary(), Beezig.api().translate("msg.reports.reported_by", Color.accent() + sender))
            : String.format("%s#%d%s", Color.accent(), id, isClaimed() ? " " + Color.primary() + Beezig.api().translate("msg.reports.claimed_by", Color.accent() + claimer) : "")
        )));
        return comp;
    }

    public MessageComponent getActions() {
        MessageComponent base = new MessageComponent(Message.infoPrefix());
        TextButton claim = new TextButton("btn.report.claim.name", "btn.report.claim.desc", "§e");
        claim.doRunCommand("/bclaim " + id);
        TextButton claimHandle = new TextButton("btn.report.handle.name", "btn.report.handle.desc", "§a");
        claimHandle.doRunCommand("/bclaim " + id + " -h");
        base.getSiblings().add(claim);
        base.getSiblings().add(new MessageComponent(" "));
        if(targets.length == 1) {
            TextButton claimTp = new TextButton("btn.report.tp.name", "btn.report.tp.desc", "§b");
            claimTp.doRunCommand("/bclaim " + id + " -tp " + targets[0]);
            base.getSiblings().add(claimTp);
            base.getSiblings().add(new MessageComponent(" "));
        }
        base.getSiblings().add(claimHandle);
        return base;
    }

    public static ReportIncoming readFrom(PacketBuffer buffer) {
        ReportIncoming result = new ReportIncoming();
        result.id = buffer.readInt();
        result.type = ReportOutgoing.ReportType.values()[buffer.readByte()];
        result.sender = buffer.readString();
        result.targets = new String[buffer.readInt()];
        for(int i = 0; i < result.targets.length; i++) result.targets[i] = buffer.readString();
        result.reasons = new String[buffer.readInt()];
        for(int i = 0; i < result.reasons.length; i++) result.reasons[i] = buffer.readString();
        result.claimer = buffer.readString();
        result.handled = buffer.readBoolean();
        return result;
    }

    private static TextButton getReplyButton(boolean reply, int id) {
        TextButton btn = new TextButton(reply ? "btn.report.reply.name" : "btn.report.contact.name",
            reply ? "btn.report.reply.desc" : "btn.report.contact.desc", "§e");
        btn.doSuggestCommand("/brm " + id + " ");
        return btn;
    }

    public static MessageComponent getWithButton(String message, int id) {
        MessageComponent base = new MessageComponent(Message.infoPrefix() + message);
        base.getSiblings().add(new MessageComponent(" "));
        base.getSiblings().add(getReplyButton(false, id));
        return base;
    }

    public static MessageComponent getChatActions(String[] targets, String sender, String message, int id) {
        MessageComponent base = new MessageComponent(Message.infoPrefix() + Beezig.api().translate("msg.report.chat", Color.accent()
            + StringUtils.localizedJoin(Arrays.asList(targets)) + Color.primary(), Color.accent() + sender + Color.primary(),
            Color.accent() + message));
        base.getSiblings().add(new MessageComponent(" "));
        base.getSiblings().add(getReplyButton(true, id));
        return base;
    }
}
