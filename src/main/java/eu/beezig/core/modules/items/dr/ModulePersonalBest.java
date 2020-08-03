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

package eu.beezig.core.modules.items.dr;

import eu.beezig.core.server.modes.DR;
import eu.the5zig.mod.modules.GameModeItem;

public class ModulePersonalBest extends GameModeItem<DR> {
    public ModulePersonalBest() {
        super(DR.class);
    }

    @Override
    protected Object getValue(boolean b) {
        if(b) return "1:02";
        DR mode = getGameMode();
        return mode.getPersonalBest();
    }

    @Override
    public String getTranslation() {
        return "beezig.module.dr.pb";
    }

    @Override
    public boolean shouldRender(boolean dummy) {
        if(dummy) return true;
        return super.shouldRender(false) && getGameMode() != null && getGameMode().getPersonalBest() != null;
    }
}
