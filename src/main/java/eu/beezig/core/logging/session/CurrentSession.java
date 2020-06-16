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

package eu.beezig.core.logging.session;

import eu.beezig.core.Beezig;
import eu.beezig.core.logging.TemporaryPointsManager;
import eu.beezig.core.server.HiveMode;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CurrentSession {
    private Deque<SessionItem> items = new ArrayDeque<>();
    private long sessionStart, sessionEnd;
    private transient long lastItemEnd;
    private Map<String, SessionService> services = new HashMap<>();

    public CurrentSession() {
        sessionStart = System.currentTimeMillis();
        lastItemEnd = sessionStart;
    }

    public void pushItem(SessionItem item) {
        items.push(createHubItem(lastItemEnd, item.getGameStart()));
        items.push(item);
        lastItemEnd = item.getGameEnd();
    }

    public Map<String, SessionService> getServices() {
        return services;
    }

    public Deque<SessionItem> getItems() {
        return items;
    }

    public long getSessionEnd() {
        return sessionEnd;
    }

    private SessionItem createHubItem(long start, long end) {
        return new SessionItem.Builder("HUB")
                .gameStart(start)
                .custom("end", Long.toString(end, 10))
                .build();
    }

    public SessionService getService(HiveMode mode) {
        String id = mode.getIdentifier();
        SessionService service = services.get(mode.getIdentifier());
        if(service == null) {
            service = new SessionService();
            services.put(id, service);
        }
        return service;
    }

    public void closeSession() throws IOException {
        sessionEnd = System.currentTimeMillis();
        items.push(createHubItem(lastItemEnd, sessionEnd));
        File sessionFile = new File(Beezig.get().getBeezigDir(),
                String.format("sessions/%s/%s.json", TemporaryPointsManager.dateFormatter.format(new Date()), sessionStart));
        if(!sessionFile.exists()) {
            sessionFile.getParentFile().mkdirs();
            sessionFile.createNewFile();
        }
        String json = Beezig.gson.toJson(this);
        FileUtils.write(sessionFile, json, StandardCharsets.UTF_8);
    }

    public long getSessionStart() {
        return sessionStart;
    }
}
