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

package eu.beezig.core.data;

import eu.beezig.core.Beezig;
import eu.beezig.core.server.modes.*;
import eu.beezig.core.util.FileUtils;
import eu.beezig.hiveapi.wrapper.utils.download.Downloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class GameTitles {
    public static final Class[] modes = new Class[] {BED.class, TIMV.class, SKY.class, HIDE.class, DR.class, GRAV.class, BP.class, SP.class};
    private File titlesFolder;

    GameTitles(File parent) {
        this.titlesFolder = new File(parent, "titles");
        if(!titlesFolder.exists()) titlesFolder.mkdirs();
    }

    public HiveTitle[] getTitles(String modeId) throws IOException {
        File f = new File(titlesFolder, modeId.toUpperCase(Locale.ROOT) + ".json");
        if(!f.exists()) return null;
        String json = FileUtils.readToString(f);
        return Beezig.gson.fromJson(json, HiveTitle[].class);
    }

    void downloadUpdate() throws Exception {
        Beezig.logger.info("Updating titles...");
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for(Class cls : modes) {
            String id = (String) cls.getMethod("getIdentifier").invoke(cls.newInstance());
            File dest = new File(titlesFolder, id.toUpperCase(Locale.ROOT) + ".json");
            futures.add(Downloader.getJsonArray(new URL(String.format(DataUrls.TITLES_FORMAT, id.toUpperCase(Locale.ROOT))))
            .thenAcceptAsync(json -> {
                try {
                    FileUtils.writeJson(json.getInput(), dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Beezig.logger.info("Titles updated successfully.");
    }
}
