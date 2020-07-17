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
import eu.beezig.core.data.DataPath;
import eu.beezig.core.logging.session.SessionItem;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.IAutovote;
import eu.beezig.core.server.monthly.IMonthly;
import eu.beezig.core.server.monthly.MonthlyService;
import eu.beezig.core.util.CollectionUtils;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.beezig.hiveapi.wrapper.player.games.TimvStats;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TIMV extends HiveMode implements IAutovote, IMonthly {

    private List<MapData> maps;
    private MapData currentMapData;
    private int traitorsDiscovered, traitorsMax, detectivesDiscovered, detectivesMax, deadTraitors, rolePoints, citizens;
    private Role role;
    private String pass = "No";

    public int getTraitorsDiscovered() {
        return traitorsDiscovered;
    }

    public int getTraitorsMax() {
        return traitorsMax;
    }

    public void setTraitorsDiscovered(int traitorsDiscovered) {
        this.traitorsDiscovered = traitorsDiscovered;
    }

    public void setDeadTraitors(int deadTraitors) {
        this.deadTraitors = deadTraitors;
    }

    public int getDeadTraitors() {
        return deadTraitors;
    }

    public void setDetectivesDiscovered(int detectivesDiscovered) {
        this.detectivesDiscovered = detectivesDiscovered;
    }

    public int getDetectivesDiscovered() {
        return detectivesDiscovered;
    }

    private void calculateRoles() {
        this.citizens = Beezig.api().getServerPlayers().size();
        this.traitorsMax = citizens / 4;
        this.traitorsMax = traitorsMax == 0 ? 1 : traitorsMax;
        this.detectivesMax = citizens / 8;
        this.detectivesMax = detectivesMax == 0 ? 1 : detectivesMax;
    }

    public int getMaxKarma() {
        if(role == null) return -1;
        switch(role) {
            case INNOCENT:
                return 20 * (traitorsMax - traitorsDiscovered);
            case DETECTIVE:
                return 25 * (traitorsMax - traitorsDiscovered);
            case TRAITOR:
                return 20 * (detectivesMax - detectivesDiscovered)
                        + 10 * (citizens - (traitorsMax - deadTraitors) - (detectivesMax - detectivesDiscovered));
            default:
                return -1;
        }
    }

    public void setCitizens(int citizens) {
        this.citizens = citizens;
    }

    public void setPass(String role) {
        this.pass = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(String role) {
        if(role.startsWith("TRAITOR")) this.role = Role.TRAITOR;
        else if(role.startsWith("DETECTIVE")) this.role = Role.DETECTIVE;
        else this.role = Role.INNOCENT;
        calculateRoles();
    }

    public TIMV() {
        statsFetcher.setApiComputer(name -> {
            TimvStats api = Profiles.timv(name).join();
            GlobalStats stats = new GlobalStats();
            stats.setPoints((int) api.getPoints());
            stats.setTitle(getTitleService().getTitle(api.getTitle()));
            return stats;
        });
        statsFetcher.setScoreboardComputer(lines -> {
            GlobalStats stats = new GlobalStats();
            stats.setPoints(lines.get("Karma"));
            Profiles.timv(UUIDUtils.strip(Beezig.user().getId()))
                    .thenAcceptAsync(api -> stats.setTitle(getTitleService().getTitle(api.getTitle())));
            return stats;
        });
        getAdvancedRecords().setExecutor(this::recordsExecutor);
        logger.setHeaders("Role", "Karma", "Map", "Role Points", "Innocent Points", "Detective Points",
                "Traitor Points", "GameID", "Passed?", "Timestamp");
    }

    private void recordsExecutor() {
        List<Pair<String, String>> messages = getAdvancedRecords().getMessages();
        // Move "Karma" to the top
        Collections.swap(messages, 0, CollectionUtils.indexOf(messages, p -> "Karma".equals(p.getLeft())));
        int rolePts = Message.getNumberFromFormat(getAdvancedRecords().getMessage("Role Points")).intValue();
        int karma = Message.getNumberFromFormat(messages.get(0).getRight()).intValue();
        if(Settings.TIMV_ADVREC_KRR.get().getBoolean()) {
            double krr = karma / (double) rolePts;
            messages.add(new ImmutablePair<>("Karma/Role Points", Message.ratio(krr)));
        }
        if(Settings.TIMV_ADVREC_TRATIO.get().getBoolean()) {
            int tIndex = CollectionUtils.indexOf(messages, p -> "Traitor Points".equals(p.getLeft()));
            String rawPts = messages.get(tIndex).getRight();
            int tPts = Message.getNumberFromFormat(rawPts).intValue();
            double ratio = tPts * 100D / (double) rolePts;
            messages.set(tIndex, new ImmutablePair<>("Traitor Points",
                    String.format("%s (%s%%%s)", rawPts, (ratio > 30 ? "§c" : "") + Message.ratio(ratio), Color.accent())));
        }
        boolean record = Settings.TIMV_ADVREC_RECORD.get().getBoolean();
        if(AdvRecUtils.needsAPI() || record) {
            AdvRecUtils.announceAPI();
            try {
                TimvStats api = Profiles.timv(getAdvancedRecords().getTarget()).get();
                messages.set(0, new ImmutablePair<>("Karma",
                        messages.get(0).getRight() + AdvRecUtils.getTitle(getTitleService(), api.getTitle(), karma)));
                if(record) messages.add(new ImmutablePair<>("Karma Record", getAdvancedRecords().modifyValue((int) api.getMostPoints())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setMap(String map) {
        super.setMap(map);
        String normalized = StringUtils.normalizeMapName(map);
        Optional<MapData> data = maps.stream().filter(m -> normalized.equals(m.map)).findAny();
        if(!data.isPresent()) {
            Message.error("error.map_not_found");
            return;
        }
        currentMapData = data.get();
    }

    public void initMapData() {
        try {
            maps = Beezig.get().getData().getDataList(DataPath.TIMV_MAPS, MapData[].class);
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
    public String getName() {
        return "Trouble in Mineville";
    }

    @Override
    public void end() {
        super.end();
        logger.log(role == null ? "None" : org.apache.commons.lang3.StringUtils.capitalize(role.name().toLowerCase()), getPoints(),
                getMap(), rolePoints, 0, 0, 0, getGameID(), pass, System.currentTimeMillis());
        if(getSessionService() != null)
            Beezig.get().getTemporaryPointsManager().getCurrentSession().pushItem(new SessionItem.Builder(getIdentifier())
                .points(getPoints()).map(getMap()).gameStart(gameStart).custom("role", role == null ? null : role.name()).build());
    }

    @Override
    public String getIdentifier() {
        return "timv";
    }

    @Override
    public int getMaxVoteOptions() {
        return 6;
    }

    @Override
    public boolean isLastRandom() {
        return true;
    }

    public void setWon() {
        rolePoints += 20;
    }

    public void addKarma(int karma) {
        addPoints(karma);
        rolePoints += karma / 10;
    }

    @Override
    public CompletableFuture<? extends MonthlyService> loadProfile() {
        return new TimvStats(null).getMonthlyProfile(UUIDUtils.strip(Beezig.user().getId()))
                .thenApplyAsync(m -> new MonthlyService(m));
    }

    private static class MapData {
        public String map;
        public int enderchests;
    }

    public enum Role {
        INNOCENT, DETECTIVE, TRAITOR
    }
}
