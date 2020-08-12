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

package eu.beezig.core.server.modes;

import eu.beezig.core.Beezig;
import eu.beezig.core.advrec.AdvRecUtils;
import eu.beezig.core.config.Settings;
import eu.beezig.core.logging.session.SessionItem;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.monthly.IMonthly;
import eu.beezig.core.server.monthly.MonthlyService;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.beezig.hiveapi.wrapper.player.games.GravStats;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GRAV extends HiveMode implements IMonthly {

    private boolean won;
    // Keeps tracked of the other players' stage completions, to assign points based on the place
    private Map<Integer, Integer> stageCompletions = new HashMap<>();
    private int currentStage;

    public GRAV()
    {
        statsFetcher.setApiComputer(name -> {
            GravStats api = Profiles.grav(name).join();
            GlobalStats stats = new GlobalStats();
            stats.setPoints((int) api.getPoints());
            stats.setPlayed((int) api.getGamesPlayed());
            stats.setVictories((int) api.getVictories());
            stats.setTitle(getTitleService().getTitle(api.getTitle()));
            return stats;
        });
        statsFetcher.setScoreboardComputer(lines -> {
            GlobalStats stats = new GlobalStats();
            stats.setPoints(lines.get("Points"));
            stats.setPlayed(lines.get("Games Played"));
            stats.setVictories(lines.get("Victories"));
            Profiles.grav(UUIDUtils.strip(Beezig.user().getId()))
                    .thenAccept(api -> stats.setTitle(getTitleService().getTitle(api.getTitle())));
            return stats;
        });
        getAdvancedRecords().setExecutor(this::recordsExecutor);
        getAdvancedRecords().setSlowExecutor(this::slowRecordsExecutor);
        logger.setHeaders("Points", "Victory?", "Timestamp");
        setGameID(Long.toString(System.currentTimeMillis(), 10));
    }

    private void recordsExecutor() {
        if(Settings.ADVREC_WINRATE.get().getBoolean()) {
            int victories = Message.getNumberFromFormat(getAdvancedRecords().getMessage("Victories")).intValue();
            int played = Message.getNumberFromFormat(getAdvancedRecords().getMessage("Games Played")).intValue();
            double wr = played == 0 ? 0 : victories * 100D / (double) played;
            getAdvancedRecords().getAdvancedMessages().add(new ImmutablePair<>("Win Rate", Message.ratio(wr) + "%"));
        }
    }

    private void slowRecordsExecutor() {
        int points = Message.getNumberFromFormat(getAdvancedRecords().getMessage("Points")).intValue();
        if (AdvRecUtils.needsAPI()) {
            AdvRecUtils.announceAPI();
            GravStats api = Profiles.grav(getAdvancedRecords().getTarget()).join();
            getAdvancedRecords().setOrAddAdvanced(0, new ImmutablePair<>("Points",
                    getAdvancedRecords().getMessages().get(0).getRight() +
                            AdvRecUtils.getTitle(getTitleService(), api.getTitle(), points)));
        }
    }

    public void setWon() {
        this.won = true;
    }

    public void stageDone() {
        if(currentStage == 0) {
            currentStage++;
            return;
        }
        int place = stageCompletions.getOrDefault(currentStage, 1) - 1;
        System.out.println("Place: " + place + " Awarding pts: " + Math.max(10, currentStage == 5 ? 70 - 12 * place : 20 - 2 * place));
        if(currentStage == 5) addPoints(Math.max(10, 70 - 12 * place));
        else addPoints(Math.max(10, 20 - 2 * place));
        currentStage++;
    }

    public void addCompletion(int stage) {
        stageCompletions.put(stage, stageCompletions.getOrDefault(stage, 0) + 1);
    }

    @Override
    public void end() {
        super.end();
        logger.log(getPoints(), won, System.currentTimeMillis());
        if(getSessionService() != null)
            Beezig.get().getTemporaryPointsManager().getCurrentSession().pushItem(new SessionItem.Builder(getIdentifier())
                    .points(getPoints()).victory(won).gameStart(gameStart).build());
    }

    @Override
    public String getIdentifier() {
        return "grav";
    }

    @Override
    public String getName() {
        return "Gravity";
    }

    @Override
    public CompletableFuture<? extends MonthlyService> loadProfile() {
        return new GravStats(null).getMonthlyProfile(UUIDUtils.strip(Beezig.user().getId()))
            .thenApplyAsync(MonthlyService::new);
    }
}
