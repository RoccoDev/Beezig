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

import eu.beezig.core.server.modes.GRAV;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.GameState;
import eu.the5zig.mod.server.IPatternResult;

public class GRAVListener extends AbstractGameListener<GRAV> {

    @Override
    public Class<GRAV> getGameMode() {
        return GRAV.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return "grav".equals(s);
    }

    @Override
    public void onMatch(GRAV gameMode, String key, IPatternResult match) {
        if("grav.completion".equals(key)) gameMode.addCompletion(Integer.parseInt(match.get(0), 10));
        else if("grav.finish_other".equals(key)) gameMode.addCompletion(5);
        else if("grav.map".equals(key) || "grav.finish".equals(key)) {
            gameMode.setState(GameState.GAME);
            gameMode.stageDone();
        }
        if("grav.finish".equals(key)) gameMode.setWon();
    }
}
