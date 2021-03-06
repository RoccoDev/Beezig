/*
 * Copyright (C) 2017-2021 Beezig Team
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

package eu.beezig.core.autovote;

import eu.beezig.core.Beezig;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.FileUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AutovoteConfig {
    private JSONObject currentConfig;
    private File configFile;

    public AutovoteConfig() {
        this.configFile = new File(Beezig.get().getBeezigDir(), "autovote.json");
        try {
            this.currentConfig = FileUtils.readJson(configFile);
        } catch (Exception e) {
            Message.error(Message.translate("error.data_read"));
            ExceptionHandler.catchException(e);
        }
    }

    public void addMapToMode(String mode, String map) {
        map = StringUtils.normalizeMapName(map);
        JSONArray modeMaps = (JSONArray) currentConfig.getOrDefault(mode.toLowerCase(Locale.ROOT), new JSONArray());
        modeMaps.add(map);
        currentConfig.put(mode, modeMaps);
    }

    public void removeMapForMode(String mode, String map) {
        Object mm = currentConfig.get(mode.toLowerCase(Locale.ROOT));
        if(!(mm instanceof JSONArray)) return;
        map = StringUtils.normalizeMapName(map);
        JSONArray modeMaps = (JSONArray) mm;
        modeMaps.remove(map);
        currentConfig.put(mode, modeMaps);
    }

    public void setPlace(String mode, String map, int place) {
        Object mm = currentConfig.get(mode.toLowerCase(Locale.ROOT));
        if(!(mm instanceof JSONArray)) return;
        map = StringUtils.normalizeMapName(map);
        JSONArray modeMaps = (JSONArray) mm;
        int index = modeMaps.indexOf(map);
        if(index == -1) {
            return;
        }
        if(place < 1) place = 1;
        Collections.swap(modeMaps, index, place - 1);
    }

    public ArrayList<String> getMaps(String mode) {
        Object mm = currentConfig.get(mode.toLowerCase(Locale.ROOT));
        if(!(mm instanceof JSONArray)) return new ArrayList<>();
        return (JSONArray) mm;
    }

    public Map<String, List<String>> getAll() {
        Map<String, List<String>> result = new HashMap<>();
        for(Object o : currentConfig.entrySet()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) o;
            if(entry.getValue() instanceof JSONArray) result.compute(entry.getKey().toLowerCase(Locale.ROOT), ($, v) -> {
                if(v == null) v = (JSONArray) entry.getValue();
                else v.addAll((JSONArray) entry.getValue());
                return v;
            });
        }
        return result;
    }

    public void setMaps(String key, List<String> maps) {
        JSONArray array = new JSONArray();
        array.addAll(maps);
        currentConfig.put(key.toLowerCase(Locale.ROOT), array);
        save();
    }

    public void save() {
        try {
            FileUtils.writeJson(currentConfig, configFile);
        } catch (IOException e) {
            Message.error(Message.translate("error.data_read"));
            ExceptionHandler.catchException(e);
        }
    }
}
