package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketReport;
import eu.beezig.core.report.ReportOutgoing;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.hiveapi.wrapper.exception.ProfileNotFoundException;
import eu.beezig.hiveapi.wrapper.utils.download.Downloader;
import eu.beezig.hiveapi.wrapper.utils.json.JObject;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ReportCommand implements Command {
    private static final Pattern REPORT_CMD_REGEX = Pattern.compile("(.+?)(?=(?<!,)\\s) (.+)$");

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/breport", "/br"};
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
                String[] reasons = gReasons.split("\\s");
                String pDisplay = Color.accent() + StringUtils.localizedJoin(Arrays.asList(players)) + Color.primary();
                String rDisplay = Color.accent() + StringUtils.localizedJoin(Arrays.asList(reasons)) + Color.primary();
                Message.info(Beezig.api().translate("msg.report.submit", pDisplay, rDisplay));
                CompletableFuture.allOf(Stream.of(players).map(this::checkUsername).toArray(CompletableFuture[]::new))
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
        CompletableFuture<Void> future = new CompletableFuture<>();
        Beezig.get().getAsyncExecutor().execute(() -> {
            try {
                JObject obj = Downloader.getJsonObject(new URL("https://api.minetools.eu/uuid/" + name)).join();
                if("OK".equals(obj.getString("status"))) future.complete(null);
                else future.completeExceptionally(new ProfileNotFoundException());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
