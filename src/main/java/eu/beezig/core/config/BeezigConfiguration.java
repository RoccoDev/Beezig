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
import eu.beezig.core.util.Color;
import eu.beezig.core.util.text.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeezigConfiguration {
    private HashMap<Settings, Setting> config;
    private File file;

    public void load(File file) throws IOException, ParseException {
        this.file = file;
        if (!file.exists()) {
            config = Stream.of(Settings.values()).map(key -> new HashMap.SimpleEntry<>(key, new Setting(key.getDefaultValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
            save();
            return;
        }
        try (BufferedReader buffer = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(buffer);
            HashMap<Object, Object> map = (HashMap<Object, Object>) json;
            config = map.entrySet().stream().map(e -> {
                try {
                    Settings key = Settings.valueOf(e.getKey().toString());
                    Setting value = new Setting(castValue(key.getSettingType(), (String) e.getValue()));
                    return new HashMap.SimpleEntry<>(key, value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
        }
        Color.refreshCache();
    }

    Setting get(Settings key) {
        return config.getOrDefault(key, new Setting(key.getDefaultValue()));
    }

    private Setting getOrPutDefault(Settings key) {
        Setting value = config.get(key);
        if (value == null) {
            Setting def = new Setting(key.getDefaultValue());
            config.put(key, def);
            return def;
        }
        return value;
    }

    public boolean set(Settings key, String newValue) {
        try {
            Object casted = castValue(key.getSettingType(), newValue);
            if (casted == null) return false;
            getOrPutDefault(key).setValue(casted);
            onSettingsChange(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Object castValue(Class cls, String value) throws Exception {
        if (cls == Boolean.class) {
            if("on".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) return true;
            if("off".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) return false;
            return Boolean.parseBoolean(value);
        }
        if (cls == Integer.class) return Integer.parseInt(value, 10);
        if (cls == Double.class) return Double.parseDouble(value);
        if (cls == Float.class) return Float.parseFloat(value);
        if (cls == Long.class) return Long.parseLong(value, 10);
        if (Enum.class.isAssignableFrom(cls)) {
            try {
                return cls.getMethod("valueOf", String.class).invoke(null, value.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                e.printStackTrace();
                if (!(e.getCause() instanceof IllegalArgumentException)) return null;
                Object[] valuesRaw = (Object[]) cls.getMethod("values").invoke(null);
                Method name = valuesRaw[0].getClass().getMethod("name");
                String possibleValues = Stream.of(valuesRaw).map(o -> {
                    try {
                        return (String) name.invoke(o);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }).collect(Collectors.joining(", "));
                Message.error(Beezig.api().translate("error.enum", "§6" + possibleValues));
            }
            return null;
        } else return cls.cast(value);
    }

    public void save() throws IOException {
        JSONObject configJson = new JSONObject();
        for (Map.Entry<Settings, Setting> e : config.entrySet()) {
            configJson.put(e.getKey().name(), e.getValue().toString());
        }
        try (BufferedWriter buffer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            buffer.write(configJson.toJSONString());
        }
    }

    private void onSettingsChange(Settings key) {
        if(key == Settings.COLOR_ACCENT || key == Settings.COLOR_PRIMARY) Color.refreshCache();
    }
}
