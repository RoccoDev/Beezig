package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketReport;

public class HandleReportCommand implements Command {
    @Override
    public String getName() {
        return "handle";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/bhandle", "/brh"};
    }

    @Override
    public boolean execute(String[] args) {
        if(args.length == 0) {
            sendUsage("/bhandle [id]");
            return true;
        }
        int id = Integer.parseInt(args[0], 10);
        Beezig.net().getHandler().sendPacket(PacketReport.handle(id));
        return true;
    }
}
