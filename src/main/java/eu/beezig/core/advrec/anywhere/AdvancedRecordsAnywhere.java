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

package eu.beezig.core.advrec.anywhere;

import eu.beezig.core.Beezig;
import eu.beezig.core.advrec.anywhere.statistic.PercentRatioStatistic;
import eu.beezig.core.advrec.anywhere.statistic.RatioRecordsStatistic;
import eu.beezig.core.advrec.anywhere.statistic.RecordsStatistic;
import eu.beezig.core.advrec.anywhere.statistic.TimeStatistic;
import eu.beezig.core.advrec.anywhere.util.ArcadeGamemodeBuilder;
import eu.beezig.core.advrec.anywhere.util.GamemodeBuilder;
import eu.beezig.core.config.Settings;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdvancedRecordsAnywhere {

    // A bunch of string constants
    private static final String PTS = "Points";
    private static final String GM = "Games Played";
    private static final String V = "Victories";
    private static final String K = "Kills";
    private static final String D = "Deaths";
    private static final String KD = "K/D";
    private static final String WR = "Win Rate";
    private static List<GamemodeStatistics> gamemodes = new ArrayList<>();

    public static List<GamemodeStatistics> getGamemodes() {
        return gamemodes;
    }

    private static GamemodeStatistics getGamemode(String query) {
        for (GamemodeStatistics gamemode : gamemodes) {
            if (query.matches("(?i:" + gamemode.getGamemode() + ")")) return gamemode;
        }
        return null;
    }

    public static void register() {
        gamemodes.add(new GamemodeBuilder("BED[SDTX]?", Profiles::bed)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "games_played"))
            .addStatistic(new RecordsStatistic(K, "kills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new RecordsStatistic("Beds Destroyed", "beds_destroyed"))
            .addStatistic(new RecordsStatistic("Teams Eliminated", "teams_eliminated"))
            .addStatistic(new RatioRecordsStatistic(KD, "kills", "deaths", Settings.ADVREC_KD))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "games_played", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("SKY", Profiles::sky)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "gamesplayed"))
            .addStatistic(new RecordsStatistic(K, "kills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new RatioRecordsStatistic(KD, "kills", "deaths", Settings.ADVREC_KD))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "gamesplayed", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("TIMV", Profiles::timv)
            .addStatistic(new RecordsStatistic("Karma", "total_points"))
            .addStatistic(new RecordsStatistic("Role Points", "role_points"))
            .addStatistic(new RecordsStatistic("Innocent Points", "i_points"))
            .addStatistic(new RecordsStatistic("Traitor Points", "t_points"))
            .addStatistic(new RecordsStatistic("Detective Points", "d_points"))
            .addStatistic(new RecordsStatistic("Karma Record", "most_points", Settings.TIMV_ADVREC_RECORD))
            .addStatistic(new RatioRecordsStatistic("Karma/Role Points", "total_points", "role_points", Settings.TIMV_ADVREC_KRR))
            .addStatistic(new PercentRatioStatistic("Traitor Share", "t_points", "role_points", Settings.TIMV_ADVREC_TRATIO))
            .build());

        gamemodes.add(new GamemodeBuilder("HIDE", Profiles::hide)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "gamesplayed"))
            .addStatistic(new RecordsStatistic("Kills as Hider", "hiderkills"))
            .addStatistic(new RecordsStatistic("Kills as Seeker", "seekerkills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new TimeStatistic("Time Alive", "timealive"))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "gamesplayed", Settings.ADVREC_WINRATE))
            .build());


        gamemodes.add(new GamemodeBuilder("CAI", Profiles::cai)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "gamesplayed"))
            .addStatistic(new RecordsStatistic("Captures", "captures"))
            .addStatistic(new RecordsStatistic("Captured", "captured"))
            .addStatistic(new RecordsStatistic("Catches", "catches"))
            .addStatistic(new RecordsStatistic("Caught", "caught"))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "gamesplayed", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("GNTM?", Profiles::gnt)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "games_played"))
            .addStatistic(new RecordsStatistic(K, "kills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new RecordsStatistic("Beasts Slain", "beasts_slain"))
            .addStatistic(new RatioRecordsStatistic(KD, "kills", "deaths", Settings.ADVREC_KD))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "games_played", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("DR", Profiles::dr)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "games_played"))
            .addStatistic(new RecordsStatistic(K, "kills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new RecordsStatistic("Checkpoints", "totalcheckpoints"))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "games_played", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("MIMV", Profiles::mimv)
            .addStatistic(new RecordsStatistic("Karma", "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "games_played"))
            .addStatistic(new RecordsStatistic(K, "kills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new RatioRecordsStatistic(KD, "kills", "deaths", Settings.ADVREC_KD))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "games_played", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("SGN", Profiles::sgn)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "games_played"))
            .addStatistic(new RecordsStatistic(K, "kills"))
            .addStatistic(new RecordsStatistic(D, "deaths"))
            .addStatistic(new RecordsStatistic("Crates Opened", "crates_opened"))
            .addStatistic(new RecordsStatistic("Points Record", "most_points"))
            .addStatistic(new RatioRecordsStatistic(KD, "kills", "deaths", Settings.ADVREC_KD))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "games_played", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("GRAV", Profiles::grav)
            .addStatistic(new RecordsStatistic(PTS, "points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "gamesplayed"))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "gamesplayed", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("LAB", Profiles::lab)
            .addStatistic(new RecordsStatistic("Atoms", "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic(GM, "gamesplayed"))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "gamesplayed", Settings.ADVREC_WINRATE))
            .build());

        gamemodes.add(new GamemodeBuilder("BP", Profiles::bp)
            .addStatistic(new RecordsStatistic(PTS, "total_points"))
            .addStatistic(new RecordsStatistic(V, "victories"))
            .addStatistic(new RecordsStatistic("Placings", "total_placing"))
            .addStatistic(new RecordsStatistic(GM, "games_played"))
            .addStatistic(new PercentRatioStatistic(WR, "victories", "games_played", Settings.ADVREC_WINRATE))
            .build());


        gamemodes.add(new ArcadeGamemodeBuilder("CR", Profiles::cr, "total_points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("MM", Profiles::mm, "points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("OITC", Profiles::oitc, "total_points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("RR", Profiles::rr, "points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("SLAP", Profiles::slap, "points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("HERO", Profiles::hero, "total_points", "victories", "games_played")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("SPL", Profiles::spl, "total_points", "victories", "games_played")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("HB", Profiles::hb, "points", null, null)
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("DRAW", Profiles::draw, "total_points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("SP", Profiles::sp, "points", "victories", "gamesplayed")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("BD", Profiles::bd, "total_points", null, "games_played")
            .build());
        gamemodes.add(new ArcadeGamemodeBuilder("EF", Profiles::ef, "points", "victories", "gamesplayed")
            .build());


    }

    public static void run(String player, String mode) {
        GamemodeStatistics gm = getGamemode(mode);
        if (gm == null) {
            Message.error(Message.translate("error.ps.mode_not_found"));
            return;
        }
        Message.info(Message.translate("advrec.running"));
        gm.produce(player).thenAcceptAsync(profile -> {
            JSONObject input = profile.getSource().getInput();
            Beezig.api().messagePlayer(StringUtils.linedCenterText(Color.primary(), Color.primary() + Beezig.api().translate("advrec.header", Color.accent() + player + Color.primary())));
            for (RecordsStatistic stat : gm.getStatistics()) {
                if (stat.getSetting() == null || stat.getSetting().get().getBoolean())
                    Beezig.api().messagePlayer(" " + Color.primary() + stat.getKey() + ": " + Color.accent() + stat.getValue(input));
            }
            Beezig.api().messagePlayer(String.format("                      %s§m                        §r                  ", Color.primary()));
        }).exceptionally(e -> {
            e.printStackTrace();
            Message.error(Message.translate("error.ara.player"));
            return null;
        });
    }
}
