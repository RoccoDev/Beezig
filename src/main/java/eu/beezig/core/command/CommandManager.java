/*
 * Copyright (C) 2017-2021 Beezig Team
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

import com.google.common.base.Splitter;
import eu.beezig.core.Beezig;
import eu.beezig.core.command.commands.*;
import eu.beezig.core.command.commands.record.DrPbCommand;
import eu.beezig.core.command.commands.record.DrWrCommand;
import eu.beezig.core.command.commands.record.GravPbCommand;
import eu.beezig.core.command.commands.record.GravWrCommand;
import eu.beezig.core.config.Settings;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.modes.GRAV;
import eu.beezig.core.util.ActiveGame;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.text.Message;
import eu.the5zig.mod.event.ChatSendEvent;
import eu.the5zig.mod.event.EventHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandManager {
    private static final Splitter SPACE = Splitter.on(' ');
    public static Set<Command> commandExecutors = new HashSet<>();
    private static long lastTeleportCommand = 0L;

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
        commandExecutors.add(new ClaimReportCommand());
        commandExecutors.add(new ReportChatCommand());
        commandExecutors.add(new HandleReportCommand());
        commandExecutors.add(new SkipMessageCommand());
        commandExecutors.add(new LeaderboardCommand());
        commandExecutors.add(new MonthlyCommand());
        commandExecutors.add(new DrPbCommand());
        commandExecutors.add(new DrWrCommand());
        commandExecutors.add(new GravPbCommand());
        commandExecutors.add(new GravWrCommand());
        commandExecutors.add(new TokensCommand());
        commandExecutors.add(new MedalsCommand());
        commandExecutors.add(new BestGameCommand());
        commandExecutors.add(new SpeedrunCommand());
        commandExecutors.add(new WinstreakCommand());
        commandExecutors.add(new MessageOverlayCommand());
    }

    /**
     * Timestamp of the last teleport command (e.g. /ng)
     */
    public static long lastTeleportCommand () {
        return lastTeleportCommand;
    }

    @EventHandler
    public void onClientChat(ChatSendEvent event) {
        String message = event.getMessage();
        if(!message.startsWith("/")) return;
        if(dispatchCommand(message)) event.setCancelled(true);
        else if (message.matches("^/(?:ng|newgame|hub|q|queue|jf|joinfriend|modtp|server).*")) {
            HiveMode mode = ActiveGame.get();
            if (Settings.GRAV_CONFIRM_DISCONNECT.get().getBoolean() && mode instanceof GRAV
                && ((GRAV) mode).confirmDisconnect()) {
                Message.info(Beezig.api().translate("msg.grav.confirm_disconnect"));
                event.setCancelled(true);
                return;
            }
            lastTeleportCommand = System.currentTimeMillis();
        }
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
        List<String> data = new ArrayList<>(SPACE.splitToList(str));
        String alias = data.get(0);
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
        data.remove(0); // Remove alias
        try {
            if (!cmdFound.execute(data.toArray(new String[0]))) {
                return false; // Skip the command
            }
        } catch (Exception e) {
            ExceptionHandler.catchException(e);
            Message.error(Beezig.api().translate("command.error", e.getClass().getName() + ": " + e.getLocalizedMessage()));
        }
        return true;
    }
}
