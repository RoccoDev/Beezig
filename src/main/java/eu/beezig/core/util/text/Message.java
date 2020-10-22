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

package eu.beezig.core.util.text;

import eu.beezig.core.Beezig;
import eu.beezig.core.Constants;
import eu.beezig.core.api.BeezigForge;
import eu.beezig.core.config.Settings;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.ExceptionHandler;
import eu.the5zig.mod.The5zigAPI;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {
    private static final String error = "§7▏ §cBeezig§7 ▏ §c";
    private static final String bar = "    §7§m                                                                                    ";
    private static final DecimalFormat bigintFormatter = new DecimalFormat("#,###");
    private static final DecimalFormat ratioFormatter = new DecimalFormat("#,###.##");
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    static List<String> toSendQueue = new ArrayList<>();

    public static String getUserAgent() {
        String framework = Beezig.get().isLaby() ? "LabyMod" : "5zig";
        return "Beezig/" + Constants.VERSION  + " (" + framework + "/" + The5zigAPI.getAPI().getModVersion() + " on "
                + The5zigAPI.getAPI().getMinecraftVersion() + "; Forge=" + The5zigAPI.getAPI().isForgeEnvironment()
                + "; BeezigForge=" + BeezigForge.isSupported() + ")";
    }

    public static String translate(String key) {
        return Beezig.api().translate(key);
    }

    public static String formatNumber(long l) {
        return Settings.THOUSANDS_SEPARATOR.get().getBoolean()
                ? bigintFormatter.format(l).replaceAll("\u00A0", " ") // Replace nbsp with space
                : Long.toString(l);
    }

    public static String date(Date date) {
        return dateFormatter.format(date);
    }

    public static String ratio(Number d) {
        return ratioFormatter.format(d);
    }

    public static void addToSendQueue(String message) {
        toSendQueue.add(message);
    }

    public static Number getNumberFromFormat(String format) {
        try {
            return NumberFormat.getInstance().parse(format.replace(" ", "\u00a0")); // Replace space with nbsp
        } catch (ParseException e) {
            ExceptionHandler.catchException(e);
        }
        return null;
    }

    public static String infoPrefix() {
        return String.format("§7▏ §aBeezig§7 ▏ %s", Color.primary());
    }

    public static void info(String text) {
        Beezig.api().messagePlayer(infoPrefix() + text);
    }

    public static void error(String text) {
        Beezig.api().messagePlayer(error + text);
    }

    public static void bar() {
        Beezig.api().messagePlayer(bar);
    }
}
