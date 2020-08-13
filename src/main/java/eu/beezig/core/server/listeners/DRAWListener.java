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

package eu.beezig.core.server.listeners;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.server.modes.DRAW;
import eu.the5zig.mod.event.ChatSendEvent;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.GameMode;
import eu.the5zig.mod.server.GameState;
import eu.the5zig.mod.server.IPatternResult;

public class DRAWListener extends AbstractGameListener<DRAW> {

    @Override
    public Class<DRAW> getGameMode() {
        return DRAW.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return "draw".equals(s);
    }

    @Override
    public void onMatch(DRAW gameMode, String key, IPatternResult match) {
        if("draw.guess".equals(key)) {
            String nick = ((ServerHive)Beezig.api().getActiveServer()).getNick();
            if(gameMode.getCurrentDrawer().equals(nick)) {
                gameMode.addPoints(2);
                return;
            }
            String player = match.get(0);
            if(player.equals(nick)) {
                int points = Integer.parseInt(match.get(1), 10);
                gameMode.addPoints(points);
            }
        }
        else if("draw.win".equals(key)) gameMode.setWon();
        else if("draw.drawer".equals(key)) gameMode.setCurrentDrawer(match.get(0));
    }

    @EventHandler
    public void onChat(ChatSendEvent event) {
        if(!ServerHive.isCurrent()) return;
        GameMode mode;
        if(!Settings.DRAW_AUTOGUESS.get().getBoolean() || !((mode = Beezig.api().getActiveServer().getGameListener().getCurrentGameMode()) instanceof DRAW)) return;
        if(mode.getState() != GameState.GAME) return;
        if(event.getMessage().startsWith("/")) return;
        if(event.getMessage().startsWith("!")) {
            event.setCancelled(true);
            Beezig.api().sendPlayerMessage(event.getMessage().substring(1));
        }
        else {
            event.setCancelled(true);
            Beezig.api().sendPlayerMessage("/guess " + event.getMessage());
        }
    }

    @Override
    public void onTitle(DRAW gameMode, String title, String subTitle) {
        if("§r§a§lGame is Starting!§r".equals(title)) gameMode.setState(GameState.GAME);
        else if("§r§c§lGame. OVER!§r".equals(title)) gameMode.setState(GameState.ENDGAME);
    }
}
