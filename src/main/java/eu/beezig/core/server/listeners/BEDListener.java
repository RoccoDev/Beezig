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

import eu.beezig.core.server.modes.BED;
import eu.the5zig.mod.server.AbstractGameListener;
import eu.the5zig.mod.server.IPatternResult;
import eu.the5zig.util.minecraft.ChatColor;

public class BEDListener extends AbstractGameListener<BED> {
    @Override
    public Class<BED> getGameMode() {
        return BED.class;
    }

    @Override
    public boolean matchLobby(String s) {
        return "bed".equals(s);
    }

    @Override
    public void onMatch(BED gameMode, String key, IPatternResult match) {
        if("bed.points".equals(key)) {
            int points = Integer.parseInt(match.get(0), 10);
            gameMode.addPoints(points);
            if(match.size() > 1) {
                String action = match.get(1);
                if(action.startsWith("Killing")) {
                    gameMode.addKills(1);
                }
                else if(action.startsWith("Destroying")) {
                    gameMode.setBedsDestroyed(gameMode.getBedsDestroyed() + 1);
                }
            }
        }
        else if("bed.kill.farm".equals(key)) gameMode.addKills(1);
        else if("bed.win".equals(key)) gameMode.addPoints(100);
        else if("bed.summoner".equals(key)) gameMode.upgradeSummoner(match.get(0));
    }

    @Override
    public void onTitle(BED gameMode, String rawTitle, String rawSubTitle) {
        if(rawTitle == null) return;
        if(rawSubTitle == null) return;
        String title = ChatColor.stripColor(rawTitle);
        String sub = ChatColor.stripColor(rawSubTitle);
        if("➋".equals(title) && "Respawning in 2 seconds".equals(sub)) gameMode.addDeaths(1);
    }
}
