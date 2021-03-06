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

package eu.beezig.core.logging;

import com.csvreader.CsvWriter;
import eu.beezig.core.Beezig;
import eu.beezig.core.util.ExceptionHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class GameLogger {

    private File file;
    private String[] headers;

    public GameLogger(String modeName) {
        this.file = new File(Beezig.get().getBeezigDir(), String.format("%s/games.csv", modeName));
    }

    public File getFile() {
        return file;
    }

    public void setHeaders(String... headers) {
        this.headers = headers;
    }

    public void log(Object... toLog) {
        CsvWriter csv = null;
        try {
            boolean writeHeaders = false;
            if (!file.exists()) {
                writeHeaders = true;
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                csv = new CsvWriter(writer, ',');

            if (writeHeaders) {
                for (String s : headers) csv.write(s);
                csv.endRecord();
            }

            for (Object s : toLog) {
                if(s instanceof Boolean) csv.write((boolean)s ? "Yes" : "No");
                else if (s != null) csv.write(s.toString());
                else csv.write("Unknown");
            }
            csv.endRecord();
        } catch (Exception e) {
            ExceptionHandler.catchException(e);
        } finally {
            if(csv != null) csv.close();
        }
    }
}
