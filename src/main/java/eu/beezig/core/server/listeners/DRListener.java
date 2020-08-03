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
import eu.beezig.core.server.modes.DR;
import eu.the5zig.mod.gui.ingame.Scoreboard;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.IPatternResult;
import eu.the5zig.util.minecraft.ChatColor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DRListener extends AbstractGameListener<DR> {
    private static Pattern POINTS_REGEX = Pattern.compile("Points: (\\d+)");
    private static Pattern KILLS_REGEX = Pattern.compile("Kills: (\\d+)");

    @Override
    public Class<DR> getGameMode() {
        return DR.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return "dr".equals(s);
    }

    @Override
    public void onGameModeJoin(DR gameMode) {
        gameMode.initMapData();
    }

    @Override
    public void onMatch(DR gameMode, String key, IPatternResult match) {
        if("dr.death".equals(key)) gameMode.addDeaths(1);
        else if("dr.checkpoint".equals(key)) {
            ServerHive server = (ServerHive) Beezig.api().getActiveServer();
            server.addTokens(Integer.parseInt(match.get(0), 10));
        }
        else if("dr.pbnew".equals(key) || "dr.pb".equals(key) || "dr.nopb".equals(key)) {
            gameMode.setTime(match.get(0));
        }
    }

    @Override
    public void onTick(DR gameMode) {
        Scoreboard sb = Beezig.api().getSideScoreboard();
        if(sb == null) return;
        for(Map.Entry<String, Integer> entry : sb.getLines().entrySet()) {
            if(entry.getValue() <= 6 && entry.getValue() >= 4) {
                String s = ChatColor.stripColor(entry.getKey());
                Matcher m = POINTS_REGEX.matcher(s);
                if(m.matches()) {
                    gameMode.tryUpdatePoints(m.group(1));
                }
                Matcher m2 = KILLS_REGEX.matcher(s);
                if(m2.matches()) {
                    gameMode.tryUpdateKills(m.group(1));
                }
            }
        }
    }
}
