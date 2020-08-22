package eu.beezig.core.command.commands;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.Command;
import eu.beezig.core.net.packets.PacketReport;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;

import java.util.Arrays;

public class ReportChatCommand implements Command {
    @Override
    public String getName() {
        return "reportchat";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"/bchat", "/brm"};
    }

    @Override
    public boolean execute(String[] args) {
        if(args.length == 0) {
            sendUsage("/bchat [id] [message]");
            return true;
        }
        int id = Integer.parseInt(args[0], 10);
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Beezig.net().getHandler().sendPacket(PacketReport.chat(id, message));
        Message.info(Beezig.api().translate("msg.report.chat.self", Color.accent() + id + Color.primary(), Color.accent() + message));
        return true;
    }
}
