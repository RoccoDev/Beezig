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

package eu.beezig.core.advrec.anywhere.util;

import eu.beezig.core.advrec.anywhere.statistic.PercentRatioStatistic;
import eu.beezig.core.advrec.anywhere.statistic.RecordsStatistic;
import eu.beezig.hiveapi.wrapper.player.GameStats;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ArcadeGamemodeBuilder extends GamemodeBuilder {

    public ArcadeGamemodeBuilder(String gamemode, Function<String, CompletableFuture<? extends GameStats>> producer,
                                 String pointsApiKey, String victoriesApiKey, String playedApiKey) {
        super(gamemode, producer);
        addStatistic(new RecordsStatistic("Points", pointsApiKey));
        if (victoriesApiKey != null)
            addStatistic(new RecordsStatistic("Victories", victoriesApiKey));
        if (playedApiKey != null)
            addStatistic(new RecordsStatistic("Games Played", playedApiKey));
        if (victoriesApiKey != null)
            addStatistic(new PercentRatioStatistic("Win Rate", victoriesApiKey, playedApiKey));
    }
}
