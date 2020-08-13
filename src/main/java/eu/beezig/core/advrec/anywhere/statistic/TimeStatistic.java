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

package eu.beezig.core.advrec.anywhere.statistic;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.json.simple.JSONObject;

public class TimeStatistic extends RecordsStatistic {

    public TimeStatistic(String key, String apiKey) {
        super(key, apiKey);
    }

    @Override
    public Object getValue(JSONObject o) {
        return DurationFormatUtils.formatDuration((long) getValueRaw(o), "hh':'mm':'ss");
    }
}
