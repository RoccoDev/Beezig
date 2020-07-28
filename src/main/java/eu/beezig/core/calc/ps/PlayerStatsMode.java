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

import eu.beezig.core.server.TitleService;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.hiveapi.wrapper.player.GameStats;
import eu.beezig.hiveapi.wrapper.player.Titleable;
import eu.beezig.hiveapi.wrapper.utils.json.JObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PlayerStatsMode {
    private HashMap<String, String> apiTable;
    private Function<String, CompletableFuture<? extends GameStats>> producer;
    private TitleService titleService;

    PlayerStatsMode(HashMap<String, String> apiTable) {
        this.apiTable = apiTable;
    }

    PlayerStatsMode(Function<String, CompletableFuture<? extends GameStats>> producer,
                    PlayerStatsMode from, HashMap<String, String> extra) {
        // The reason we first assign our table to a clone of the template one and not the extra one is
        // because it allows the manager to overwrite values, as they get put last.
        this.apiTable = (HashMap<String, String>) from.apiTable.clone();
        this.apiTable.putAll(extra);
        this.producer = producer;
    }

    PlayerStatsMode(Function<String, CompletableFuture<? extends GameStats>> producer,
                    HashMap<String, String> apiTable) {
        this(apiTable);
        this.producer = producer;
    }

    String getApiKey(String commonName) {
        return apiTable.get(commonName.toLowerCase(Locale.ROOT));
    }

    public CompletableFuture<? extends GameStats> get(UUID uuid) {
        return producer.apply(UUIDUtils.strip(uuid));
    }

    public void setTitleService(TitleService titleService) {
        this.titleService = titleService;
    }

    PlayerStatsProfile getProfile(HashMap<String, String> displayNames, GameStats source, String key, String stat) {
        Number value = null;
        JObject src = source.getSource();
        if("kd".equalsIgnoreCase(stat)) {
            String apiKills = getApiKey("kills");
            String apiDeaths = getApiKey("deaths");
            if(src.getInput().get(apiKills) == null || src.getInput().get(apiDeaths) == null) return null;
            value = src.getLong(getApiKey("kills")) / (double) src.getLong(getApiKey("deaths"));
        }
        else if("wl".equalsIgnoreCase(stat)) {
            String apiWins = getApiKey("victories");
            String apiGames = getApiKey("played");
            if(src.getInput().get(apiWins) == null || src.getInput().get(apiGames) == null) return null;
            value = src.getLong(getApiKey("victories")) * 100D / (double) src.getLong(getApiKey("played"));
        }
        if(value == null) {
            if(source.getSource().getInput().get(key) == null) return null;
            value = source.getSource().getInt(key);
        }
        PlayerStatsProfile profile = new PlayerStatsProfile(displayNames.get(source.getUUID()), value);
        if(source instanceof Titleable && titleService != null && titleService.isValid())
            profile.setTitle(titleService.getTitle(((Titleable)source).getTitle()).getRight());
        return profile;
    }

    Set<String> getAvailableStats() {
        return apiTable.keySet();
    }
}
