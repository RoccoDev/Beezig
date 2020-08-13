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

import eu.beezig.core.config.Settings;
import eu.beezig.core.util.text.Message;
import org.json.simple.JSONObject;

public class PercentRatioStatistic extends RatioRecordsStatistic {

    public PercentRatioStatistic(String key, String apiKey1, String apiKey2) {
        super(key, apiKey1, apiKey2);
    }

    public PercentRatioStatistic(String key, String apiKey1, String apiKey2, Settings setting) {
        super(key, apiKey1, apiKey2, setting);
    }


    @Override
    public Object getValue(JSONObject o) {
        return Message.ratio(super.getValueRaw(o) * 100) + "%";
    }
}
