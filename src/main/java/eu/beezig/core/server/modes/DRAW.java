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
import eu.beezig.hiveapi.wrapper.player.games.arcade.DrawStats;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.concurrent.CompletableFuture;

public class DRAW extends HiveMode implements IMonthly {
    private boolean won;
    private String currentDrawer;

    public DRAW() {
        statsFetcher.setApiComputer(name -> {
            DrawStats api = Profiles.draw(name).join();
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
            Profiles.draw(UUIDUtils.strip(Beezig.user().getId()))
                .thenAcceptAsync(api -> stats.setTitle(getTitleService().getTitle(api.getTitle())));
            return stats;
        });
        getAdvancedRecords().setExecutor(this::recordsExecutor);
        getAdvancedRecords().setSlowExecutor(this::slowRecordsExecutor);
        logger.setHeaders("Points", "Victory?", "Timestamp");
        setGameID(Long.toString(System.currentTimeMillis(), 10));
    }

    public String getCurrentDrawer() {
        return currentDrawer;
    }

    public void setCurrentDrawer(String currentDrawer) {
        this.currentDrawer = currentDrawer;
    }

    public void setWon() {
        this.won = true;
    }

    @Override
    public void end() {
        super.end();
        logger.log(getPoints(), won, System.currentTimeMillis());
        if(getSessionService() != null)
            Beezig.get().getTemporaryPointsManager().getCurrentSession().pushItem(new SessionItem.Builder(getIdentifier())
                .points(getPoints()).victory(won).gameStart(gameStart).build());
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
            DrawStats api = Profiles.draw(getAdvancedRecords().getTarget()).join();
            getAdvancedRecords().setOrAddAdvanced(0, new ImmutablePair<>("Points",
                getAdvancedRecords().getMessages().get(0).getRight() +
                    AdvRecUtils.getTitle(getTitleService(), api.getTitle(), points)));
        }
    }

    @Override
    public String getIdentifier() {
        return "draw";
    }

    @Override
    public String getName() {
        return "DrawIt";
    }

    @Override
    public CompletableFuture<? extends MonthlyService> loadProfile() {
        return new DrawStats(null).getMonthlyProfile(UUIDUtils.strip(Beezig.user().getId()))
            .thenApplyAsync(MonthlyService::new);
    }
}
