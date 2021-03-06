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

package eu.beezig.core.util.speedrun;

import eu.beezig.core.Beezig;
import eu.beezig.core.server.modes.DR;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.hiveapi.wrapper.utils.download.Downloader;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class DrWorldRecords {
    public static CompletableFuture<WorldRecord> getRecord(DR.MapData map) {
        String url = "https://web.beezig.eu/v1/proxy/speedrun/" + map.speedrun;
        try {
            return Downloader.getJsonObject(new URL(url)).thenApplyAsync(o -> Beezig.gson.fromJson(o.getInput().toJSONString(), WorldRecord.class));
        } catch (MalformedURLException e) {
            ExceptionHandler.catchException(e);
            return null;
        }
    }

    public static class WorldRecord {
        private String name;
        private int wr_millis;
        private transient String display;

        public String getDisplay() {
            if(display != null) return display;
            return display = String.format("%s (%s)", getTimeDisplay(), name);
        }

        public String getTimeDisplay() {
            return DurationFormatUtils.formatDuration(wr_millis, "m':'ss.SSS");
        }

        public int getMillis() {
            return wr_millis;
        }

        public String getName() {
            return name;
        }
    }
}
