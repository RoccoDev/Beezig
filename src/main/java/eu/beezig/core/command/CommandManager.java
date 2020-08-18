/*
 * Copyright (C) 2017-2020 Beezig Team
 *
 * This file is part of Beezig.
 *
 * Beezig is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beezig is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beezig.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.beezig.core.command;

import eu.beezig.core.Beezig;
import eu.beezig.core.command.commands.*;
import eu.beezig.core.util.text.Message;
import eu.the5zig.mod.event.ChatSendEvent;
import eu.the5zig.mod.event.EventHandler;

import java.util.*;

public class CommandManager {

    public static Set<Command> commandExecutors = new HashSet<>();

    private static void registerCommands() {
        commandExecutors.add(new BeezigCommand());
        commandExecutors.add(new PlayerStatsCommand());
        commandExecutors.add(new AutovoteCommand());
        commandExecutors.add(new SettingsCommand());
        commandExecutors.add(new CustomTestCommand());
        commandExecutors.add(new DoNotDisturbCommand());
        commandExecutors.add(new SayCommand());
        commandExecutors.add(new BroadcastReplyCommand());
        commandExecutors.add(new OnBeezigCommand());
        commandExecutors.add(new ToggleBeeCommand());
        commandExecutors.add(new ProfileCommand());
        commandExecutors.add(new BUpdateCommand());
        commandExecutors.add(new BlockstatsCommand());
        commandExecutors.add(new RecordsOverlayCommand());
        commandExecutors.add(new ReportCommand());
        commandExecutors.add(new ReportsCommand());
    }

    @EventHandler
    public void onClientChat(ChatSendEvent event) {
        String message = event.getMessage();
        if(!message.startsWith("/")) return;
        if(dispatchCommand(message)) event.setCancelled(true);
    }

    public static void init(Beezig plugin) {
        registerCommands();
        Beezig.api().getPluginManager().registerListener(plugin, new CommandManager());
    }

    /**
     * Dispatches a command.
     *
     * @return whether the command was found
     */
    // Public access is required for BeezigLaby.
    public static boolean dispatchCommand(String str) {
        String[] data = str.split(" ");
        String alias = data[0];
        Command cmdFound = null;
        for (Command cmd : commandExecutors) {
            for (String s : cmd.getAliases()) {
                if (s.equalsIgnoreCase(alias)) {
                    cmdFound = cmd;
                    break;
                }
            }
        }
        if (cmdFound == null) return false;

        List<String> dataList = new ArrayList<>(Arrays.asList(data));
        dataList.remove(0); // Remove alias

        try {
            if (!cmdFound.execute(dataList.toArray(new String[0]))) {
                return false; // Skip the command
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message.error(Beezig.api().translate("command.error", e.getClass().getName() + ": " + e.getLocalizedMessage()));
        }
        return true;
    }
}
