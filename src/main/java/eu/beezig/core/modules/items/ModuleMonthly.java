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

package eu.beezig.core.modules.items;

import eu.beezig.core.modules.Modules;
import eu.beezig.core.server.HiveMode;
import eu.beezig.core.server.monthly.IMonthly;
import eu.beezig.core.server.monthly.MonthlyField;
import eu.beezig.core.server.monthly.MonthlyService;
import eu.the5zig.mod.modules.GameModeItem;

public class ModuleMonthly extends GameModeItem<HiveMode> {

    public ModuleMonthly() {
        super(HiveMode.class);
    }

    @Override
    public void registerSettings() {
        getProperties().addSetting("field", MonthlyField.POINTS, MonthlyField.class);
    }

    @Override
    protected Object getValue(boolean b) {
        if(b) return "#1 | 12,345 Points";
        MonthlyField field = (MonthlyField) getProperties().getSetting("field").get();
        MonthlyService service = getGameMode().getMonthlyProfile();
        return service.getStat(field);
    }

    @Override
    public String getTranslation() {
        return "beezig.module.monthly";
    }

    @Override
    public boolean shouldRender(boolean dummy) {
        return dummy || Modules.render() && getGameMode() instanceof IMonthly && getGameMode().getMonthlyProfile() != null;
    }
}
