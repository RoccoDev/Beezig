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

import eu.beezig.core.advrec.anywhere.GamemodeStatistics;
import eu.beezig.core.advrec.anywhere.statistic.RecordsStatistic;
import eu.beezig.hiveapi.wrapper.player.GameStats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GamemodeBuilder {
    private String gamemode;
    private List<RecordsStatistic> statistics = new ArrayList<>();
    private Function<String, CompletableFuture<? extends GameStats>> producer;

    public GamemodeBuilder(String gamemode, Function<String, CompletableFuture<? extends GameStats>> producer) {
        this.gamemode = gamemode;
        this.producer = producer;
    }

    public GamemodeBuilder addStatistic(RecordsStatistic stat) {
        statistics.add(stat);
        return this;
    }

    public GamemodeStatistics build() {
        return new GamemodeStatistics(gamemode, statistics, producer);
    }
}
