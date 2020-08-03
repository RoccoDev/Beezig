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

import com.google.common.reflect.TypeToken;
import eu.beezig.core.Beezig;
import eu.beezig.core.advrec.AdvRecUtils;
import eu.beezig.core.data.DataPath;
import eu.beezig.core.logging.session.SessionItem;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.IAutovote;
import eu.beezig.core.server.monthly.IMonthly;
import eu.beezig.core.server.monthly.MonthlyField;
import eu.beezig.core.server.monthly.MonthlyService;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.beezig.hiveapi.wrapper.player.games.DrStats;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DR extends HiveMode implements IAutovote, IMonthly {

    private Map<String, MapData> maps;
    private MapData currentMapData;
    private int lastSbPoints;
    private int lastSbKills;
    private String time;
    /**
     * Cached profile, used to load personal bests
     */
    private DrStats profile;

    // Personal Best
    private long pbSecs;
    private String pb;

    public String getEndTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void initMapData() {
        try {
            maps = Beezig.get().getData().getDataMap(DataPath.DR_MAPS, new TypeToken<Map<String, MapData>>() {});
            if(maps == null) {
                Message.error("error.data_read");
                Beezig.logger.error("Tried to fetch maps but file wasn't found.");
            }
        } catch (Exception e) {
            Message.error("error.data_read");
            e.printStackTrace();
        }
    }

    @Override
    public String getIdentifier() {
        return "dr";
    }

    @Override
    public String getName() {
        return "DeathRun";
    }

    @Override
    public int getMaxVoteOptions() {
        return 6;
    }

    @Override
    public boolean isLastRandom() {
        return true;
    }

    @Override
    public void setMap(String map) {
        super.setMap(map);
        String normalized = StringUtils.normalizeMapName(map);
        MapData data = maps.get(normalized);
        if(data == null) {
            Message.error("error.map_not_found");
            return;
        }
        currentMapData = data;
        if(profile != null) {
            Long record = profile.getMapRecords().get(currentMapData.hive);
            if(record != null) {
                pbSecs = record;
                pb = DurationFormatUtils.formatDuration(pbSecs * 1000, "m:ss");
            }
        }
    }

    public DR ()
    {
        statsFetcher.setApiComputer(name -> {
            DrStats api = Profiles.dr(name).join();
            profile = api;
            GlobalStats stats = new GlobalStats();
            stats.setPoints((int) api.getPoints());
            stats.setKills((int) api.getKills());
            stats.setPlayed((int) api.getGamesPlayed());
            stats.setDeaths((int) api.getDeaths());
            stats.setVictories((int) api.getVictories());
            stats.setTitle(getTitleService().getTitle(api.getTitle()));
            return stats;
        });
        statsFetcher.setScoreboardComputer(lines -> {
            GlobalStats stats = new GlobalStats();
            stats.setPoints(lines.get("Points"));
            stats.setKills(lines.get("Kills"));
            stats.setPlayed(lines.get("Games Played"));
            stats.setDeaths(lines.get("Deaths"));
            stats.setVictories(lines.get("Victories"));
            Profiles.dr(UUIDUtils.strip(Beezig.user().getId()))
                    .thenAcceptAsync(api -> {
                        profile = api;
                        stats.setTitle(getTitleService().getTitle(api.getTitle()));
                    });
            return stats;
        });
        getAdvancedRecords().setExecutor(this::recordsExecutor);
        getAdvancedRecords().setSlowExecutor(this::slowRecordsExecutor);
        logger.setHeaders("Points", "Map", "Kills", "Deaths", "GameID", "Timestamp", "Time");
        setGameID(Long.toString(System.currentTimeMillis(), 10));
    }

    private void recordsExecutor() {
        AdvRecUtils.addPvPStats(getAdvancedRecords());
    }

    private void slowRecordsExecutor() {
        int points = Message.getNumberFromFormat(getAdvancedRecords().getMessage("Points")).intValue();
        if (AdvRecUtils.needsAPI()) {
            AdvRecUtils.announceAPI();
            DrStats api = Profiles.dr(getAdvancedRecords().getTarget()).join();
            getAdvancedRecords().setOrAddAdvanced(0, new ImmutablePair<>("Points",
                    getAdvancedRecords().getMessages().get(0).getRight() +
                            AdvRecUtils.getTitle(getTitleService(), api.getTitle(), points)));
        }
    }

    @Override
    public void end() {
        super.end();
        logger.log(getPoints(), getMap(), getKills(), getDeaths(), getGameID(), System.currentTimeMillis(), time);
        if(getSessionService() != null)
            Beezig.get().getTemporaryPointsManager().getCurrentSession().pushItem(new SessionItem.Builder(getIdentifier())
                    .points(getPoints()).map(getMap()).custom("time", time).gameStart(gameStart).kills(getKills()).deaths(getDeaths()).build());
    }

    public void tryUpdatePoints(String newAmount) {
        int num = Integer.parseInt(newAmount, 10);
        if(lastSbPoints == num) return;
        addPoints(num - lastSbPoints);
        lastSbPoints = num;
    }

    public void tryUpdateKills(String newAmount) {
        int num = Integer.parseInt(newAmount, 10);
        if(lastSbKills == num) return;
        addKills(num - lastSbKills);
        lastSbKills = num;
    }

    @Override
    public CompletableFuture<? extends MonthlyService> loadProfile() {
        return new DrStats(null).getMonthlyProfile(UUIDUtils.strip(Beezig.user().getId()))
                .thenApplyAsync(m -> new MonthlyService(m, MonthlyField.KILLS, MonthlyField.DEATHS, MonthlyField.KD));
    }

    public String getPersonalBest() {
        return pb;
    }

    private static class MapData {
        public String speedrun, hive;
        public int checkpoints;
    }
}
