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

package eu.beezig.core.logging.reports;

import eu.beezig.core.Beezig;
import eu.beezig.core.logging.session.CurrentSession;
import eu.beezig.core.logging.session.SessionService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyReport {
    private File file;
    private Configuration ftl;
    private List<CurrentSession> sessions;
    private Map<String, Integer> dailyPoints = new HashMap<>();

    public DailyReport(List<CurrentSession> sessions, String date) {
        this.sessions = sessions;
        file = new File(Beezig.get().getBeezigDir(), String.format("reports/%s.html", date));
        if(!file.exists()) file.getParentFile().mkdirs();
    }

    public List<CurrentSession> getSessions() {
        return sessions;
    }

    public Map<String, Integer> getDailyPoints() {
        return dailyPoints;
    }

    void init() {
        ftl = new Configuration(Configuration.VERSION_2_3_30);
        ftl.setClassForTemplateLoading(Beezig.class, "/templates/");
        ftl.setDefaultEncoding("UTF-8");
        ftl.setLogTemplateExceptions(false);
        ftl.setWrapUncheckedExceptions(true);
        calculateDailyPoints();
    }

    private void calculateDailyPoints() {
        for(CurrentSession session : sessions) {
            for(Map.Entry<String, SessionService> entry : session.getServices().entrySet()) {
                Integer pts = dailyPoints.get(entry.getKey());
                if(pts == null) pts = entry.getValue().getPoints();
                pts += entry.getValue().getPoints();
                dailyPoints.put(entry.getKey(), pts);
            }
        }
    }

    public void writeToFile() throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("report", this);
        Template template = ftl.getTemplate("daily_report.ftl");
        try(Writer out = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            template.process(root, out);
        }
    }

    public URI getOpenURI() {
        return file.toURI();
    }
}
