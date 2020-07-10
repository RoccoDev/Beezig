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

package eu.beezig.core.server.monthly;

import eu.beezig.core.server.HiveMode;
import eu.beezig.core.util.text.Message;
import eu.beezig.hiveapi.wrapper.monthly.MonthlyProfile;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class MonthlyService {
    public static Set<Class<? extends HiveMode>> ignoredModes = new HashSet<>();

    private final MonthlyProfile profile;
    private final Set<MonthlyField> supportedFields;

    public MonthlyService(MonthlyProfile profile, MonthlyField... supportedFields) {
        this.profile = profile;
        this.supportedFields = EnumSet.of(MonthlyField.POINTS, supportedFields);
    }

    public String getStat(MonthlyField field) {
        if(!supportedFields.contains(field)) field = MonthlyField.POINTS;
        Number stat = field.get(profile);
        return String.format("#%d  ▏ %s %s", profile.getPlace(),
                stat instanceof Double ? Message.ratio(stat) : Message.formatNumber(stat.longValue()), field.getDisplayName());
    }
}
