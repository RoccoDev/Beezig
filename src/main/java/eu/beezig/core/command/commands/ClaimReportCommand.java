package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketReport;

import java.util.Arrays;
import java.util.List;

public class ClaimReportCommand implements Command {
    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/bclaim", "/brc"};
    }

    @Override
    public boolean execute(String[] args) {
        int id = Integer.parseInt(args[0], 10);
        List<String> list = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
        Beezig.net().getHandler().sendPacket(PacketReport.claim(id));
        if(list.contains("-h")) Beezig.net().getHandler().sendPacket(PacketReport.handle(id));
        int tpIdx;
        if((tpIdx = list.indexOf("-tp")) != -1) {
            String name = list.get(tpIdx + 1);
            Beezig.api().sendPlayerMessage("/modtp " + name);
        }
        return true;
    }
}
