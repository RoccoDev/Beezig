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

package eu.beezig.core.autogg;

import eu.beezig.core.Beezig;
import eu.beezig.core.data.DataPath;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.util.ActiveGame;
import eu.the5zig.mod.event.ChatEvent;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.event.TitleEvent;
import eu.the5zig.util.minecraft.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoGGManager {

    Map<String, Trigger> triggers;

    public AutoGGManager() {
        triggers = new HashMap<>();
    }

    private void handleEvent(String message, Trigger.Type... types) {
        HiveMode game = ActiveGame.get();
        if (game == null)
            return;

        Trigger trigger = triggers.get(game.getIdentifier());
        if (trigger != null) {
            for (Trigger.Type t : types) {
                if (trigger.doesTrigger(ChatColor.stripColor(message), t)) {
                    // FIXME We're testing this module with party chat. I don't want to get muted for spamming in chat ofc.
                    Beezig.api().sendPlayerMessage("/p gg");
                }
            }
        }
    }

    public void initTriggers() {
        try {
            List<TriggerModeConnection> triggers = Beezig.get().getData().getDataList(DataPath.AUTOGG_TRIGGERS,
                    TriggerModeConnection[].class);
            triggers.forEach(t -> this.triggers.put(t.mode, t.trigger));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onMessage(ChatEvent event) {
        handleEvent(event.getMessage(), Trigger.Type.CHAT);
    }

    @EventHandler
    public void onTitle(TitleEvent event) {
        // We're calling handleEvent for TITLE and SUBTITLE as this event triggers
        // for both types.
        handleEvent(event.getTitle(), Trigger.Type.TITLE, Trigger.Type.SUBTITLE);
    }

    private static class TriggerModeConnection {
        public String mode;
        public Trigger trigger;
    }

}
