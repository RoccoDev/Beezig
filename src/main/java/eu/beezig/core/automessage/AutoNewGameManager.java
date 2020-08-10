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

package eu.beezig.core.automessage;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Setting;
import eu.beezig.core.config.Settings;
import eu.beezig.core.data.DataPath;
import eu.beezig.core.server.ServerHive;

public class AutoNewGameManager extends AutoMessageManager{

    public AutoNewGameManager() {
        super(false);
    }

    @Override
    public Setting getEnabledSetting() {
        return Settings.AUTONEWGAME.get();
    }

    @Override
    public String getMessage() {
        return "/newgame";
    }

    @Override
    public Setting getDelaySetting() {
        return Settings.AUTONEWGAME_DELAY.get();
    }

    @Override
    public DataPath getTriggersPath() {
        return DataPath.AUTONEWGAME_TRIGGERS;
    }

    @Override
    public boolean shouldFire() {
        return !((ServerHive) Beezig.api().getActiveServer()).getInParty() || Settings.AUTONEWGAME_IN_PARTIES.get().getBoolean();
    }
}
