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

package eu.beezig.core.modules;

import eu.beezig.core.Beezig;
import eu.beezig.core.modules.items.*;
import eu.beezig.core.modules.items.bed.ModuleResources;
import eu.beezig.core.modules.items.bed.ModuleSummoners;
import eu.beezig.core.modules.items.timv.ModuleGameKarma;
import eu.beezig.core.modules.items.timv.ModuleTraitorsDiscovered;
import eu.beezig.core.server.ServerHive;
import eu.the5zig.mod.ModAPI;

public class Modules {

    private static final String HIVE = "serverhivemc";

    public static void register(Beezig plugin, ModAPI api) {
        api.registerModuleItem(plugin, "hive_tokens", ModuleTokens.class, HIVE);
        api.registerModuleItem(plugin, "hive_points", ModulePoints.class, HIVE);
        api.registerModuleItem(plugin, "hive_kills", ModuleKills.class, HIVE);
        api.registerModuleItem(plugin, "hive_deaths", ModuleDeaths.class, HIVE);
        api.registerModuleItem(plugin, "hive_map", ModuleMap.class, HIVE);
        api.registerModuleItem(plugin, "hive_kdr", ModuleKDRChange.class, HIVE);
        api.registerModuleItem(plugin, "hive_game", ModuleGameProgress.class, HIVE);
        api.registerModuleItem(plugin, "hive_medals", ModuleMedals.class, HIVE);
        api.registerModuleItem(plugin, "hive_lobby", ModuleLobby.class, HIVE);
        api.registerModuleItem(plugin, "hive_daily", ModuleDaily.class, HIVE);
        api.registerModuleItem(plugin, "hive_session", ModuleSession.class, HIVE);
        api.registerModuleItem(plugin, "hive_monthly", ModuleMonthly.class, HIVE);

        // Trouble in Mineville
        api.registerModuleItem(plugin, "timv_traitors", ModuleTraitorsDiscovered.class, HIVE);
        api.registerModuleItem(plugin, "timv_gamekarma", ModuleGameKarma.class, HIVE);

        // Bedwars
        api.registerModuleItem(plugin, "bed_resources", ModuleResources.class, HIVE);
        api.registerModuleItem(plugin, "bed_summoners", ModuleSummoners.class, HIVE);
    }

    public static boolean render() {
        return ServerHive.isCurrent();
    }
}
