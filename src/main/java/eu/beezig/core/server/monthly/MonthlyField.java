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

package eu.beezig.core.server.monthly;

import eu.beezig.core.util.text.Message;
import eu.beezig.hiveapi.wrapper.monthly.MonthlyProfile;
import eu.beezig.hiveapi.wrapper.monthly.PvPMonthlyProfile;

import java.util.function.Function;

public enum MonthlyField {
    POINTS("modules.item.hive_points", MonthlyProfile::getPoints),
    KILLS("modules.item.hive_kills", p -> ((PvPMonthlyProfile)p).getKills()),
    DEATHS("modules.item.hive_deaths", p -> ((PvPMonthlyProfile)p).getDeaths()),
    KD("stat.kdr", p -> {
       PvPMonthlyProfile pvp = (PvPMonthlyProfile)p;
       return pvp.getKills() / (double) pvp.getDeaths();
    });

    private final Function<MonthlyProfile, Number> callback;
    private final String translationKey;

    MonthlyField(String key, Function<MonthlyProfile, Number> callback) {
        this.callback = callback;
        this.translationKey = key;
    }

    public Number get(MonthlyProfile in) {
        return callback.apply(in);
    }

    public String getDisplayName() {
        return Message.translate(translationKey);
    }
}
