package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketReport;
import eu.beezig.core.report.ReportIncoming;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;

import java.util.Locale;

public class ReportsCommand implements Command {
    @Override
    public String getName() {
        return "reports";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/breports"};
    }

    @Override
    public boolean execute(String[] args) {
        boolean includeClaimed = (args.length == 1 && args[0].toLowerCase(Locale.ROOT).startsWith("c"))
            || (args.length == 2 && args[1].toLowerCase(Locale.ROOT).startsWith("c"));
        byte page = args.length == 1 || args.length == 2 ? Byte.parseByte(args[0], 10) : 1;
        Beezig.net().getHandler().sendPacket(PacketReport.request(includeClaimed, page));
        return true;
    }

    public static void sendResult(ReportIncoming[] reports, ReportIncoming[] claimed, byte page) {
        boolean modDisplay = Beezig.get().isMod();
        Beezig.api().messagePlayer(StringUtils.linedCenterText(Color.primary(), Color.accent() + Message.translate("msg.reports")));
        for(ReportIncoming report : reports) Beezig.api().messagePlayerComponent(report.getForList(modDisplay), false);
        if(claimed.length != 0) {
            Beezig.api().messagePlayer(StringUtils.linedCenterText(Color.primary(), Color.accent() + Message.translate("msg.reports.claimed")));
            for(ReportIncoming report : claimed) Beezig.api().messagePlayerComponent(report.getForList(true), false);
        }
        Beezig.api().messagePlayer(StringUtils.linedCenterText(Color.primary(), Color.accent() + Beezig.api().translate("msg.page", page)));
    }
}
