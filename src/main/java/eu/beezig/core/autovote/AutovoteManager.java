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

package eu.beezig.core.autovote;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.IAutovote;
import eu.beezig.core.util.Color;
import eu.beezig.core.util.FileUtils;
import eu.beezig.core.util.text.Message;
import eu.beezig.core.util.text.StringUtils;
import eu.the5zig.mod.server.IPatternResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AutovoteManager {

    private HashMap<String, AutovoteMap> maps;
    private HiveMode mode;

    public AutovoteManager(HiveMode mode) {
        this.maps = new HashMap<>();
        this.mode = mode;
    }

    public void parse(IPatternResult messageIn) {
        if(!(mode instanceof IAutovote)) {
            Beezig.logger.warn(String.format("Mode class %s tried to autovote, but it doesn't implement IAutovote.", mode.getClass().getName()));
            return;
        }
        if(mode.hasVoted()) return;
        String index = messageIn.get(0);
        String mapName = messageIn.get(1);
        String votes = messageIn.get(2);
        int idx = Integer.parseInt(index, 10);
        IAutovote voteHandle = (IAutovote) mode;
        boolean last = idx == voteHandle.getMaxVoteOptions();
        if(last && voteHandle.isLastRandom()) maps.put("/random/", new AutovoteMap(mapName, idx, Integer.parseInt(votes, 10)));
        else maps.put(StringUtils.normalizeMapName(mapName), new AutovoteMap(mapName, idx, Integer.parseInt(votes, 10)));
        if(last) run();
    }

    private void run() {
        if(mode.hasVoted()) return;
        mode.setVoted(true);
        Beezig.logger.debug(String.format("[Autovote] Parsed maps: %s", String.join(", ", maps.keySet())));
        String modeName = mode.getIdentifier();
        ArrayList<String> savedMaps;
        try {
            savedMaps = getFavoriteMaps(modeName);
        } catch (Exception e) {
            Message.error(Message.translate("error.data_read"));
            e.printStackTrace();
            return;
        }
        savedMaps.retainAll(maps.keySet());
        if(savedMaps.size() == 0) {
            if(Settings.AUTOVOTE_RANDOM.get().getBoolean()) {
                AutovoteMap random = maps.get("/random/");
                if(random == null) return;
                Beezig.api().sendPlayerMessage("/v " + random.getIndex());
                Message.info(Beezig.api().translate("msg.autovote", Color.accent() + random.getName()));
            }
        }
        else {
            AutovoteMap map = maps.get(savedMaps.get(0));
            Beezig.api().sendPlayerMessage("/v " + map.getIndex());
            Message.info(Beezig.api().translate("msg.autovote", Color.accent() + map.getName()));
        }
    }

    public ArrayList<String> getFavoriteMaps(String modeId) throws Exception {
        File f = new File(Beezig.get().getBeezigDir(), "autovote.json");
        if(!f.exists()) {
            f.createNewFile();
            FileUtils.writeJson(new JSONObject(), f);
            return new ArrayList<>();
        }
        JSONObject json = FileUtils.readJson(f);
        if(!json.containsKey(modeId)) return new ArrayList<>();
        JSONArray arr = (JSONArray) json.get(modeId);
        return arr;
    }
}
