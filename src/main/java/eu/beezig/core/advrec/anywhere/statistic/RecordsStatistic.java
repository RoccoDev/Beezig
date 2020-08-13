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

public class RecordsStatistic {

    private String key;
    private String apiKey;
    private Settings setting;

    public RecordsStatistic(String key, String apiKey) {
        this.key = key;
        this.apiKey = apiKey;
    }

    public RecordsStatistic(String key, String apiKey, Settings setting) {
        this(key, apiKey);
        this.setting = setting;
    }

    public String getKey() {
        return key;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Object getValue(JSONObject o) {
        return Settings.THOUSANDS_SEPARATOR.get().getBoolean() ? Message.formatNumber((long) getValueRaw(o)) : (int) getValueRaw(o);
    }

    public double getValueRaw(JSONObject o) {
        return (long) o.get(apiKey);
    }

    public Settings getSetting() {
        return setting;
    }
}
