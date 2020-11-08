package eu.beezig.core.command.commands;

import com.google.common.base.Splitter;
import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketReport;
import eu.beezig.core.report.ReportOutgoing;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.hiveapi.wrapper.mojang.UsernameToUuid;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        return new String[] {"/breport", "/brep"};
    }

    @Override
    public boolean execute(String[] args) {
        if(!ServerHive.isCurrent()) return false;
        if(args.length > 1) {
            String cmd = String.join(" ", args);
            Matcher m = REPORT_CMD_REGEX.matcher(cmd);
            if (m.matches()) {
                String gPlayers = m.group(1);
                String gReasons = m.group(2);
                List<String> players = Splitter.onPattern(",\\s?").splitToList(gPlayers);
                List<String> reasons = Splitter.onPattern("\\s").splitToList(gReasons);
                String pDisplay = Color.accent() + StringUtils.localizedJoin(players) + Color.primary();
                String rDisplay = Color.accent() + StringUtils.localizedJoin(reasons) + Color.primary();
                Message.info(Beezig.api().translate("msg.report.submit", pDisplay, rDisplay));
                CompletableFuture.allOf(players.stream().map(this::checkUsername).toArray(CompletableFuture[]::new))
                    .thenAcceptAsync(v -> Beezig.net().getHandler()
                        .sendPacket(PacketReport.newReport(new ReportOutgoing(ReportOutgoing.ReportType.PLAYER, players, reasons))))
                    .exceptionally(e -> {
                        Message.error(Message.translate("error.report.username"));
                        return null;
                    });
            }
            return true;
        }
        sendUsage("/breport [player1(, player2, player3)] [reason]");
        return true;
    }

    private CompletableFuture<Void> checkUsername(String name) {
        return UsernameToUuid.getUUID(name).thenAcceptAsync(s -> {});
    }
}
