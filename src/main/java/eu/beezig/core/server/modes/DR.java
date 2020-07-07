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
import eu.beezig.core.logging.session.SessionItem;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.IAutovote;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.beezig.hiveapi.wrapper.player.games.DrStats;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class DR extends HiveMode implements IAutovote {

    private int lastSbPoints;
    private int lastSbKills;
    private String time;

    public String getEndTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public DR ()
    {
        statsFetcher.setApiComputer(name -> {
            DrStats api = Profiles.dr(name).join();
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
                    .thenAccept(api -> stats.setTitle(getTitleService().getTitle(api.getTitle())));
            return stats;
        });
        getAdvancedRecords().setExecutor(this::recordsExecutor);
        logger.setHeaders("Points", "Map", "Kills", "Deaths", "GameID", "Timestamp", "Time");
    }

    private void recordsExecutor() {
        AdvRecUtils.addPvPStats(getAdvancedRecords());
        int points = Message.getNumberFromFormat(getAdvancedRecords().getMessage("Points")).intValue();
        if (AdvRecUtils.needsAPI()) {
            AdvRecUtils.announceAPI();
            DrStats api = Profiles.dr(getAdvancedRecords().getTarget()).join();
            getAdvancedRecords().getMessages().set(0, new ImmutablePair<>("Points",
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
}
