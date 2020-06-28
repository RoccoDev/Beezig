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

import eu.beezig.core.config.Setting;
import eu.beezig.core.config.Settings;
import eu.beezig.core.data.DataPath;

public class AutoGGManager extends AutoMessageManager{
    @Override
    public Setting getEnabledSetting() {
        return Settings.AUTOGG.get();
    }

    @Override
    public Setting getMessageSetting() {
        return Settings.AUTOGG_MESSAGE.get();
    }

    @Override
    public Setting getDelaySetting() {
        return Settings.AUTOGG_DELAY.get();
    }

    @Override
    public DataPath getTriggersPath() {
        return DataPath.AUTOGG_TRIGGERS;
    }
}
