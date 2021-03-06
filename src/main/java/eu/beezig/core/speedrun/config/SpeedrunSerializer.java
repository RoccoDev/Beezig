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

package eu.beezig.core.speedrun.config;

import eu.beezig.core.Beezig;
import eu.beezig.core.api.SettingInfo;
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.server.modes.DR;
import eu.beezig.core.speedrun.Run;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.FileUtils;
import eu.beezig.core.util.text.Message;
import eu.the5zig.mod.server.GameMode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SpeedrunSerializer {
    private static void save(SpeedrunConfig config) throws IOException {
        JSONObject json = new JSONObject();
        for(SpeedrunConfigValues value : SpeedrunConfigValues.values()) {
            json.put(value.name(), serialize(value.get(config)));
        }
        FileUtils.writeJson(json, getFile());
    }

    private static Object serialize(Object in) {
        if(in instanceof String[]) {
            JSONArray array = new JSONArray();
            Collections.addAll(array, (String[]) in);
            return array;
        }
        if(in instanceof Enum) {
            return in.toString();
        }
        return in;
    }

    private static Object deserialize(Object in) {
        if(in instanceof JSONArray) {
            return ((JSONArray) in).stream().map(Object::toString).toArray(String[]::new);
        }
        return in;
    }

    public static void read(SpeedrunConfig config) throws IOException, ParseException {
        File file = getFile();
        if(!file.exists()) return;
        JSONObject json = FileUtils.readJson(file);
        for(Object o : json.entrySet()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) o;
            SpeedrunConfigValues.valueOf(entry.getKey()).setValue(config, deserialize(entry.getValue()));
        }
    }

    public static void saveAndCopy(List<SettingInfo> settings) {
        GameMode gm;
        Run currentRun = null;
        SpeedrunConfig config;
        if(ServerHive.isCurrent()
            && (gm = Beezig.api().getActiveServer().getGameListener().getCurrentGameMode()) instanceof DR
            && (currentRun = ((DR) gm).getCurrentRun()) != null) {
            config = currentRun.getConfig();
        } else config = new SpeedrunConfig(null);
        config.setAll(settings);
        if(currentRun != null) currentRun.reloadConfig();
        try {
            save(config);
        } catch (IOException e) {
            Message.error(Message.translate("error.speedrun.config.save"));
            ExceptionHandler.catchException(e, "Speedrun config save");
        }
    }

    private static File getFile() {
        return new File(Beezig.get().getBeezigDir(), "dr/speedrun.json");
    }
}
