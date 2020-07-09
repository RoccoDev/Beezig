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

package eu.beezig.core.modules.items.bed;

import eu.beezig.core.Beezig;
import eu.beezig.core.server.modes.BED;
import eu.the5zig.mod.modules.GameModeItem;
import eu.the5zig.mod.server.GameState;

public class ModuleResources extends GameModeItem<BED> {
    public ModuleResources() {
        super(BED.class);
    }

    @Override
    public void registerSettings() {
        getProperties().addSetting("showcolors", true);
        getProperties().addSetting("shortened", false);
    }

    @Override
    protected Object getValue(boolean b) {
        if (!Beezig.api().isInWorld()) return "Not in world";
        int iron = b ? 5 : Beezig.api().getItemCount("minecraft:iron_ingot");
        int gold = b ? 30 : Beezig.api().getItemCount("minecraft:gold_ingot");
        int diamonds = b ? 1 : Beezig.api().getItemCount("minecraft:diamond");
        int emeralds = b ? 4 : Beezig.api().getItemCount("minecraft:emerald");
        boolean colors = (boolean) getProperties().getSetting("showcolors").get();
        boolean shortened = (boolean) getProperties().getSetting("shortened").get();
        String suffixIron = shortened ? " I§r" : " Iron§r";
        String suffixGold = shortened ? " G§r" : " Gold§r";
        String suffixDiamonds = shortened ? " D§r" : " Diamonds§r";
        String suffixEmeralds = shortened ? " E§r" : " Emeralds§r";
        StringBuilder builder = new StringBuilder();
        boolean notFirstEntry = false;
        if(iron != 0 && (notFirstEntry = true)) builder.append(colors ? "§7" : "").append(iron).append(suffixIron);
        if(notFirstEntry && gold != 0) builder.append(" / ");
        if(gold != 0 && (notFirstEntry = true)) builder.append(colors ? "§6" : "").append(gold).append(suffixGold);
        if(notFirstEntry && diamonds != 0) builder.append(" / ");
        if(diamonds != 0 && (notFirstEntry = true)) builder.append(colors ? "§b" : "").append(diamonds).append(suffixDiamonds);
        if(notFirstEntry && emeralds != 0) builder.append(" / ");
        if(emeralds != 0) builder.append(colors ? "§a" : "").append(emeralds).append(suffixEmeralds);
        return builder.toString();
    }

    @Override
    public boolean shouldRender(boolean dummy) {
        if(dummy) return true;
        return super.shouldRender(false) && Beezig.api().isInWorld() && getGameMode().getState() == GameState.GAME;
    }

    @Override
    public String getTranslation() {
        return "beezig.module.bed.resources";
    }
}
