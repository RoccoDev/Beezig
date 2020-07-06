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
import eu.beezig.core.server.modes.HIDE;
import eu.the5zig.mod.gui.ingame.Scoreboard;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.IPatternResult;
import eu.the5zig.util.minecraft.ChatColor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HIDEListener extends AbstractGameListener<HIDE> {

    private static Pattern POINTS_REGEX = Pattern.compile("(\\d+) Points");

    @Override
    public Class<HIDE> getGameMode() {
        return HIDE.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return "hide".equals(s);
    }

    @Override
    public void onMatch(HIDE gameMode, String key, IPatternResult match) {
        if("hide.kill".equals(key)) {
            gameMode.addKills(1);
        }
        else if("hide.win".equals(key)) {
            gameMode.setWon();
            gameMode.addPoints(Integer.parseInt(match.get(0), 10));
        }
    }

    @Override
    public void onTick(HIDE gameMode) {
        Scoreboard sb = Beezig.api().getSideScoreboard();
        if(sb == null) return;
        for(Map.Entry<String, Integer> entry : sb.getLines().entrySet()) {
            if(entry.getValue() == 5 || entry.getValue() == 4) {
                Matcher m = POINTS_REGEX.matcher(ChatColor.stripColor(entry.getKey()));
                if(m.matches()) {
                    gameMode.tryUpdatePoints(m.group(1));
                }
            }
        }
    }
}
