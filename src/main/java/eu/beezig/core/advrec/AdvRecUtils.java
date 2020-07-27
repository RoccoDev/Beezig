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

package eu.beezig.core.advrec;

import eu.beezig.core.config.Settings;
import eu.beezig.core.data.HiveTitle;
import eu.beezig.core.server.TitleService;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class AdvRecUtils {
    public static void addPvPStats(AdvancedRecords mgr) {
        addPvPStats(mgr, "Kills", "Deaths");
    }

    public static void addPvPStats(AdvancedRecords mgr, String killsKey, String deathsKey) {
        if(s(Settings.ADVREC_KD)) {
            int kills = Message.getNumberFromFormat(mgr.getMessage(killsKey)).intValue();
            int deaths = Message.getNumberFromFormat(mgr.getMessage(deathsKey)).intValue();
            double kd = deaths == 0 ? Double.POSITIVE_INFINITY : kills / (double) deaths;
            mgr.getAdvancedMessages().add(new ImmutablePair<>("K/D", Message.ratio(kd)));
        }
        if(s(Settings.ADVREC_WINRATE)) {
            int victories = Message.getNumberFromFormat(mgr.getMessage("Victories")).intValue();
            int played = Message.getNumberFromFormat(mgr.getMessage("Games Played")).intValue();
            double wr = played == 0 ? 0 : victories * 100D / (double) played;
            mgr.getAdvancedMessages().add(new ImmutablePair<>("Win Rate", Message.ratio(wr) + "%"));
        }
        if(s(Settings.ADVREC_KPG)) {
            int kills = Message.getNumberFromFormat(mgr.getMessage(killsKey)).intValue();
            int played = Message.getNumberFromFormat(mgr.getMessage("Games Played")).intValue();
            double kpg = played == 0 ? Double.POSITIVE_INFINITY : kills / (double) played;
            mgr.getAdvancedMessages().add(new ImmutablePair<>("Kills Per Game",
                    Message.ratio(kpg)));
        }
        if(s(Settings.ADVREC_PPG)) {
            int points = Message.getNumberFromFormat(mgr.getMessage("Points")).intValue();
            int played = Message.getNumberFromFormat(mgr.getMessage("Games Played")).intValue();
            double ppg = played == 0 ? Double.POSITIVE_INFINITY : points / (double) played;
            mgr.getAdvancedMessages().add(new ImmutablePair<>("Points Per Game",
                    Message.ratio(ppg)));
        }
    }

    public static void announceAPI() {
        Message.info(Message.translate("advrec.running"));
    }

    public static boolean needsAPI() {
        return s(Settings.ADVREC_RANK);
    }

    public static String getTitle(TitleService mgr, String rank, int points) {
        if(!s(Settings.ADVREC_RANK)) return "";
        Pair<Integer, HiveTitle> title = mgr.getTitle(rank);
        if(!s(Settings.ADVREC_TONEXT)) return title.getRight().getColoredName();
        String next = mgr.getToNext(title.getLeft(), points, Color.accent());
        return String.format(" (%s %s/ %s%s)", title.getRight().getColoredName(), Color.accent(), next, Color.accent());
    }

    private static boolean s(Settings key) {
        return key.get().getBoolean();
    }
}
