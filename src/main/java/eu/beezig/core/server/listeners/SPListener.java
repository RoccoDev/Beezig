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
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.server.modes.SP;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.GameState;
import eu.the5zig.mod.server.IPatternResult;

public class SPListener extends AbstractGameListener<SP> {

    @Override
    public Class<SP> getGameMode() {
        return SP.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return "sp".equals(s);
    }

    @Override
    public void onMatch(SP gameMode, String key, IPatternResult match) {
        if("sp.points".equals(key)) {
            String nick = ((ServerHive) Beezig.api().getActiveServer()).getNick();
            String player = match.get(0);
            if(!nick.equals(player)) gameMode.addPoints(10);
        }
        else if("sp.win".equals(key)) {
            gameMode.addPoints(170);
            gameMode.setWon();
        }
    }

    @Override
    public void onTitle(SP gameMode, String title, String subTitle) {
        if("§r§e➊§r".equals(title)) gameMode.setState(GameState.GAME);
    }
}
