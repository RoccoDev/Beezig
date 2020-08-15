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
import eu.beezig.core.server.modes.SKY;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.IPatternResult;

public class SKYListener extends AbstractGameListener<SKY> {

    @Override
    public Class<SKY> getGameMode() {
        return SKY.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return s.matches("sky[dt]?");
    }

    @Override
    public void onMatch(SKY gameMode, String key, IPatternResult match) {
        if("sky.kill".equals(key) && match.get(0).equals(((ServerHive) Beezig.api().getActiveServer()).getNick())) {
            gameMode.addKills(1);
        }
        else if ("sky.win".equals(key)) {
            gameMode.setWon();
        }
    }
}
