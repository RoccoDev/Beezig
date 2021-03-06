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
import eu.beezig.core.data.GameTitles;
import eu.beezig.core.logging.session.CurrentSession;
import eu.beezig.core.logging.session.LogRotation;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.UUIDUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A manager class for daily and session points
 */
public class TemporaryPointsManager {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final Pattern DAILY_FILE_REGEX = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})(?:-(.+))?\\.txt");

    private CurrentSession currentSession;

    public CurrentSession getCurrentSession() {
        return currentSession;
    }

    public void startSession() {
        currentSession = new CurrentSession();
    }

    public void endSession() {
        if(currentSession != null) {
            try {
                currentSession.closeSession();
            } catch (IOException e) {
                ExceptionHandler.catchException(e, "Could not save session data");
            }
        }
    }

    public DailyService getDailyForMode(HiveMode mode) {
        String date = dateFormatter.format(Instant.now());
        String uuid = UUIDUtils.strip(Beezig.user().getId());
        File dailyFile = new File(mode.getModeDir(), String.format("dailyPoints/%s-%s.txt", date, uuid));
        DailyService service = new DailyService(mode.getIdentifier(), dailyFile);
        try {
            service.loadFromFile();
        } catch (IOException e) {
            ExceptionHandler.catchException(e, "Couldn't load daily service");
        }
        return service;
    }

    public void init() throws ReflectiveOperationException {
        initDailyServices();
        try {
            new LogRotation().rotateLogs();
        } catch (IOException e) {
            ExceptionHandler.catchException(e, "Couldn't rotate session logs");
        }
    }

    private void initDailyServices() throws ReflectiveOperationException {
        Beezig.logger.info("Migrating daily points...");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for(Class modeClass : GameTitles.modes) {
            HiveMode inst = (HiveMode) modeClass.getConstructor().newInstance();
            futures.add(migrateLogsForMode(inst));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Beezig.logger.info("Daily points migrated.");
    }

    private CompletableFuture<Void> migrateLogsForMode(HiveMode mode) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Beezig.get().getAsyncExecutor().execute(() -> {
            File dailyDir = new File(mode.getModeDir(), "dailyPoints");
            if(!dailyDir.exists() || !dailyDir.isDirectory()) {
                future.complete(null);
                return;
            }
            File[] files =  dailyDir.listFiles((file, s) -> s.toLowerCase(Locale.ROOT).endsWith(".txt"));
            if(files == null) {
                future.complete(null);
                return;
            }
            File dailyCsv = new File(mode.getModeDir(), "daily.csv");
            if(!dailyCsv.exists()) {
                dailyCsv.getParentFile().mkdirs();
                try {
                    dailyCsv.createNewFile();
                } catch (IOException e) {
                    future.completeExceptionally(e);
                    return;
                }
            }
            String today = dateFormatter.format(Instant.now());
            try(BufferedWriter writer = Files.newBufferedWriter(dailyCsv.toPath(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                CsvWriter csv = null;
                try {
                    csv = new CsvWriter(writer, ',');
                    for (File file : files) {
                        String name = file.getName();
                        Matcher matcher = DAILY_FILE_REGEX.matcher(name);
                        if(!matcher.matches()) continue;
                        String date = matcher.group(1);
                        if(date.equals(today)) continue;
                        String uuid = "Unknown";
                        if(matcher.groupCount() > 1) uuid = matcher.group(2);
                        String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                        if(contents.isEmpty()) contents = "0";
                        csv.write(date);
                        csv.write(uuid);
                        csv.write(contents);
                        csv.endRecord();
                        file.delete();
                    }
                }
                finally {
                    if(csv != null) csv.close();
                }
                future.complete(null);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
