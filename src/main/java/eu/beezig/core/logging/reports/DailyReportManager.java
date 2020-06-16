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
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailyReportManager {
    private List<CurrentSession> getSessionData(String date) throws IOException {
        File sessionFile = new File(Beezig.get().getBeezigDir(), String.format("sessions/%s", date));
        if(!sessionFile.exists() || !sessionFile.isDirectory()) return new ArrayList<>();
        File[] files = sessionFile.listFiles((file, s) -> s.toLowerCase(Locale.ROOT).endsWith(".json"));
        if(files == null) return new ArrayList<>();
        List<CurrentSession> result = new ArrayList<>(files.length);
        for(File file : files) {
            String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            result.add(Beezig.gson.fromJson(contents, CurrentSession.class));
        }
        return result;
    }

    public DailyReport generateDailyReport(String date) throws IOException, TemplateException {
        DailyReport report = new DailyReport(getSessionData(date), date);
        report.init();
        report.writeToFile();
        return report;
    }
}
