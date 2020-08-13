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

public class RatioRecordsStatistic extends RecordsStatistic {

    private String ratio1, ratio2;

    public RatioRecordsStatistic(String key, String apiKey1, String apiKey2) {
        super(key, apiKey1);
        ratio1 = apiKey1;
        ratio2 = apiKey2;
    }

    public RatioRecordsStatistic(String key, String apiKey1, String apiKey2, Settings setting) {
        super(key, apiKey1, setting);
        ratio1 = apiKey1;
        ratio2 = apiKey2;
    }

    @Override
    public double getValueRaw(JSONObject o) {
        long val1 = (long) o.get(ratio1);
        long val2 = (long) o.get(ratio2);

        return val1 / (double) val2;
    }

    @Override
    public Object getValue(JSONObject o) {
        return Message.ratio(getValueRaw(o));
    }
}
