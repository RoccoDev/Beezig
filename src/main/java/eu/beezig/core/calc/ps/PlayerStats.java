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

package eu.beezig.core.calc.ps;

import com.google.common.collect.ImmutableMap;
import eu.beezig.core.advrec.anywhere.AdvancedRecordsAnywhere;
import eu.beezig.core.advrec.anywhere.GamemodeStatistics;
import eu.beezig.core.advrec.anywhere.statistic.RecordsStatistic;
import eu.beezig.core.util.text.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class PlayerStats {
    HashMap<String, PlayerStatsMode> modes = new HashMap<>();
    private final Map<String, String> conversionTable = ImmutableMap.<String, String>builder().put("Points", "points").put("Games Played", "games")
        .put("Victories", "victories").put("Kills", "kills").put("Deaths", "deaths").put("K/D", "kd").put("W/L", "wl").put("Karma", "points").build();
    private final Map<String, String> modeConversions = ImmutableMap.of("BED[SDTX]?", "bed", "GNTM?", "gnt");

    PlayerStats() {
        loadFromAdvRec();
    }

    private void loadFromAdvRec() {
        for(GamemodeStatistics gm : AdvancedRecordsAnywhere.getGamemodes()) {
            String mode = modeConversions.getOrDefault(gm.getGamemode(), gm.getGamemode());
            HashMap<String, String> table = new HashMap<>();
            for(RecordsStatistic stat : gm.getStatistics()) {
                table.put(conversionTable.getOrDefault(stat.getKey(), StringUtils.normalizeMapName(stat.getKey())), stat.getApiKey());
            }
            modes.put(mode.toLowerCase(Locale.ROOT), new PlayerStatsMode(gm.getProducer(), table));
        }
    }
}
