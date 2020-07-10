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

package eu.beezig.core.server;

import eu.beezig.core.Beezig;
import eu.beezig.core.advrec.AdvancedRecords;
import eu.beezig.core.autovote.AutovoteManager;
import eu.beezig.core.data.HiveTitle;
import eu.beezig.core.logging.DailyService;
import eu.beezig.core.logging.GameLogger;
import eu.beezig.core.logging.TemporaryPointsManager;
import eu.beezig.core.logging.session.SessionService;
import eu.beezig.core.server.monthly.IMonthly;
import eu.beezig.core.server.monthly.MonthlyService;
import eu.beezig.core.util.text.Message;
import eu.the5zig.mod.server.GameMode;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public abstract class HiveMode extends GameMode {
    private int points;
    private String map;
    /**
     * The player's stats, but updated with the game's stats
     */
    private GlobalStats global;
    protected StatsFetcher statsFetcher;
    /**
     * The player's stats at the start of the game
     */
    private GlobalStats cachedGlobal;
    private AutovoteManager autovoteManager;
    private AdvancedRecords advancedRecords;
    private TitleService titleService;
    protected GameLogger logger;
    private boolean hasVoted;
    private String gameID;
    private DailyService dailyService;
    private SessionService sessionService;
    private File modeDir;
    protected long gameStart;
    private MonthlyService monthlyProfile;

    public HiveMode() {
        global = new GlobalStats();
        autovoteManager = new AutovoteManager(this);
        cachedGlobal = new GlobalStats();
        statsFetcher = new StatsFetcher(getClass());
        statsFetcher.getJob().thenAcceptAsync(this::setGlobal).exceptionally(e -> {
            Message.error(Message.translate("error.stats_fetch"));
            Beezig.logger.error(e);
            return null;
        });
        advancedRecords = new AdvancedRecords();
        try {
            titleService = new TitleService(getIdentifier());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger = new GameLogger(getIdentifier().toLowerCase(Locale.ROOT));
        modeDir = new File(Beezig.get().getBeezigDir(), getIdentifier().toLowerCase(Locale.ROOT));
        TemporaryPointsManager temporaryPointsManager = Beezig.get().getTemporaryPointsManager();
        if (temporaryPointsManager != null) {
            dailyService = temporaryPointsManager.getDailyForMode(this);
            if(temporaryPointsManager.getCurrentSession() != null)
                sessionService = temporaryPointsManager.getCurrentSession().getService(this);
        }
        gameStart = System.currentTimeMillis();
        if(this instanceof IMonthly) {
            if(!MonthlyService.ignoredModes.contains(getClass())) {
                ((IMonthly) this).loadProfile().exceptionally(e -> {
                    MonthlyService.ignoredModes.add(HiveMode.this.getClass());
                    return null;
                }).thenAcceptAsync(this::setMonthlyProfile);
            }
        }
    }

    private void setMonthlyProfile(MonthlyService monthlyProfile) {
        this.monthlyProfile = monthlyProfile;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public boolean hasVoted() {
        return hasVoted;
    }

    public void setVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public AdvancedRecords getAdvancedRecords() {
        return advancedRecords;
    }

    public StatsFetcher getStatsFetcher() {
        return statsFetcher;
    }

    public GlobalStats getCachedGlobal() {
        return cachedGlobal;
    }

    public TitleService getTitleService() {
        return titleService;
    }

    public DailyService getDailyService() {
        return dailyService;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public File getModeDir() {
        return modeDir;
    }

    public MonthlyService getMonthlyProfile() {
        return monthlyProfile;
    }

    /**
     * Called when the user returns to the lobby.
     */
    protected void end() {
        try {
            dailyService.submitGamePoints(getPoints(), gameID);
            dailyService.save();
        } catch (IOException e) {
            Beezig.logger.error("Couldn't save daily points", e);
        }
    }

    public abstract String getIdentifier();

    public AutovoteManager getAutovoteManager() {
        return autovoteManager;
    }

    public GameLogger getLogger() {
        return logger;
    }

    public int getPoints() {
        return points;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public void addPoints(int points) {
        this.points += points;
        if(global.points != null) global.points += points;
        if(dailyService != null) dailyService.addPoints(points);
        if(sessionService != null) sessionService.addPoints(points);
    }

    public void addKills(int kills) {
        this.kills += kills;
        if(global.kills != null) global.kills += kills;
    }

    public void addDeaths(int deaths) {
        this.deaths += deaths;
        if(global.deaths != null) global.deaths += deaths;
    }

    public GlobalStats getGlobal() {
        return global;
    }

    public void setGlobal(GlobalStats global) {
        this.global = global;
        try {
            this.cachedGlobal = global.clone();
        } catch (CloneNotSupportedException e) {
            Beezig.logger.error("Couldn't save global stats");
            Beezig.logger.error(e);
        }
    }

    public static class GlobalStats implements Cloneable {
        private Integer points;
        private Integer kills;
        private Integer deaths;
        private Integer victories;
        private Integer played;
        private Pair<Integer, HiveTitle> title;

        public Integer getPlayed() {
            return played;
        }

        public Integer getPoints() {
            return points;
        }

        public Integer getKills() {
            return kills;
        }

        public Integer getDeaths() {
            return deaths;
        }

        public Integer getVictories() {
            return victories;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public void setKills(Integer kills) {
            this.kills = kills;
        }

        public void setDeaths(Integer deaths) {
            this.deaths = deaths;
        }

        public void setVictories(Integer victories) {
            this.victories = victories;
        }

        public void setPlayed(Integer played) {
            this.played = played;
        }

        public Pair<Integer, HiveTitle> getTitle() {
            return title;
        }

        public void setTitle(Pair<Integer, HiveTitle> title) {
            this.title = title;
        }

        @Override
        protected GlobalStats clone() throws CloneNotSupportedException {
            return (GlobalStats) super.clone();
        }
    }
}
