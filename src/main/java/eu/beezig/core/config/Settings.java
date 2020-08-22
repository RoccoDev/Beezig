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

package eu.beezig.core.config;

import eu.beezig.core.Beezig;
import eu.beezig.core.advrec.AdvancedRecords;
import eu.beezig.core.notification.MessageIgnoreLevel;
import eu.beezig.core.util.text.Message;
import eu.the5zig.util.minecraft.ChatColor;

import java.util.Locale;

public enum Settings {
    // Colors
    COLOR_PRIMARY(ChatColor.DARK_AQUA),
    COLOR_ACCENT(ChatColor.AQUA),

    // Misc
    THOUSANDS_SEPARATOR(true),
    MSG_PING(true),
    MSG_DND_MODE(MessageIgnoreLevel.IGNORE_ALERT),
    MSG_DND_ALERT("I'm currently on Do Not Disturb mode. I'll get back to you later!"),

    // Autovote
    AUTOVOTE(true),
    AUTOVOTE_RANDOM(true),
    AUTOVOTE_SHUFFLE(true),

    // Advanced Records
    ADVANCED_RECORDS(true),
    ADVREC_MODE(AdvancedRecords.Mode.NORMAL),
    ADVREC_RANK(true),
    ADVREC_TONEXT(true),
    ADVREC_KD(true),
    ADVREC_WINRATE(true),
    ADVREC_PPG(true),
    ADVREC_KPG(true),

    // Anti Sniper
    SNIPE_TYPO(true),
    SNIPE_PMS(true),

    // Trouble in Mineville
    TIMV_ADVREC_KRR(true),
    TIMV_ADVREC_TRATIO(true),
    TIMV_ADVREC_RECORD(true),
    TIMV_ADVREC_KPV(true),

    // DrawIt
    DRAW_AUTOGUESS(true),

    // Misc
    CHAT_LINKS(true),
    AUTOGG(false),
    AUTOGG_MESSAGE("gg"),
    AUTOGG_DELAY(0L),
    AUTOGL(false),
    AUTOGL_MESSAGE("gl & hf"),
    AUTOGL_DELAY(0L),
    AUTONEWGAME(false),
    AUTONEWGAME_DELAY(500L),
    AUTONEWGAME_IN_PARTIES(false),
    RECORD_DND(false),
    CHAT_PARTY(true),
    CHAT_FRIENDS(true),
    REPORTS_NOTIFY(true);

    private final Object defaultValue;
    private final Class settingType;

    Settings(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.settingType = defaultValue.getClass();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Class getSettingType() {
        return settingType;
    }

    public String getName() {
        return Message.translate("setting." + name().toLowerCase(Locale.ROOT) + ".name");
    }

    public String getDescription() {
        return Message.translate("setting." + name().toLowerCase(Locale.ROOT) + ".desc");
    }

    public Setting get() {
        return Beezig.cfg().get(this);
    }

}
