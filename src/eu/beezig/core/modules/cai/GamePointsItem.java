/*
 * Copyright (C) 2019 Beezig (RoccoDev, ItsNiklass)
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

package eu.beezig.core.modules.cai;

import eu.beezig.core.games.CAI;
import eu.the5zig.mod.modules.GameModeItem;

public class GamePointsItem extends GameModeItem<CAI> {

    public GamePointsItem() {
        super(CAI.class);
    }

    @Override
    protected Object getValue(boolean dummy) {
        try {
            return CAI.gamePoints;

        } catch (Exception e) {
            e.printStackTrace();
            return "Server error";
        }
    }

    @Override
    public String getTranslation() {
        return "beezig.module.game";
    }

    @Override
    public boolean shouldRender(boolean dummy) {
        try {
            if (getGameMode() == null)
                return false;
            if (CAI.gamePoints == 0)
                return false;
            return dummy || (CAI.shouldRender(getGameMode().getState()));
        } catch (Exception e) {
            return false;
        }
    }

}
