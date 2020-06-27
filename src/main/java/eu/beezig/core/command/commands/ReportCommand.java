package eu.beezig.core.command.commands;

import eu.beezig.core.command.Command;
import eu.beezig.core.report.ReportReason;
import eu.beezig.core.server.ServerHive;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportCommand implements Command {
    private static final Pattern REPORT_CMD_REGEX = Pattern.compile("(.+?)(?=(?<!,)\\s) (.+)$");

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/report"};
    }

    @Override
    public boolean execute(String[] args) {
        if(!ServerHive.isCurrent()) return true;
        if(args.length > 1) {
            String cmd = String.join(" ", args);
            Matcher m = REPORT_CMD_REGEX.matcher(cmd);
            if (m.matches()) {
                String gPlayers = m.group(1);
                String gReasons = m.group(2);
                String[] players = gPlayers.split(",\\s?");
                String[] reasonInputs = gReasons.split("\\s");
                EnumSet<ReportReason> reasons = ReportReason.getReasons(reasonInputs);
                if(reasons.size() == 0) {
                    return true;
                }

            }
        }
        sendUsage("/report [player1(, player2, player3)] [reason]");
        return true;
    }
}
