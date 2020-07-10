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
import eu.beezig.core.server.ServerHive;
import eu.beezig.core.server.modes.BED;
import eu.the5zig.mod.modules.GameModeItem;
import eu.the5zig.mod.server.GameState;

public class ModuleSummoners extends GameModeItem<BED> {
    public ModuleSummoners() {
        super(BED.class);
    }

    @Override
    public void registerSettings() {
        getProperties().addSetting("showcolors", true);
        getProperties().addSetting("shortened", false);
    }

    @Override
    protected Object getValue(boolean b) {
        BED mode;
        if(b) return "No data";
        if (!ServerHive.isCurrent() || (mode = getGameMode()) == null || !Beezig.api().isInWorld()) return "Not in world";
        boolean colors = (boolean) getProperties().getSetting("showcolors").get();
        boolean shortened = (boolean) getProperties().getSetting("shortened").get();
        StringBuilder builder = new StringBuilder();
        boolean notFirstEntry = false;
        if(mode.getIronSummoner() != 0 && (notFirstEntry = true)) builder.append(colors ? "§7" : "")
                .append(shortened ? "I" : "Iron").append(shortened ? " " : " Lv. ").append(mode.getIronSummoner()).append("§r");
        if(notFirstEntry && mode.getGoldSummoner() != 0) builder.append(" / ");
        if(mode.getGoldSummoner() != 0 && (notFirstEntry = true)) builder.append(colors ? "§6" : "")
                .append(shortened ? "G" : "Gold").append(shortened ? " " : " Lv. ").append(mode.getGoldSummoner()).append("§r");
        if(notFirstEntry && mode.getDiamondSummoner() != 0) builder.append(" / ");
        if(mode.getDiamondSummoner() != 0) builder.append(colors ? "§b" : "")
                .append(shortened ? "D" : "Diamond").append(shortened ? " " : " Lv. ").append(mode.getDiamondSummoner()).append("§r");
        return builder.toString();
    }

    @Override
    public boolean shouldRender(boolean dummy) {
        if(dummy) return true;
        return super.shouldRender(false) && Beezig.api().isInWorld() && getGameMode().getState() == GameState.GAME;
    }

    @Override
    public String getTranslation() {
        return "beezig.module.bed.summoners";
    }
}
