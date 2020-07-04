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

package eu.beezig.core.api;

import eu.beezig.core.Beezig;
import eu.beezig.core.net.BeezigNetManager;

import java.util.Iterator;
import java.util.ServiceLoader;

public class BeezigServiceLoader {
    private ServiceLoader<IBeezigService> services = ServiceLoader.load(IBeezigService.class, getClass().getClassLoader().getParent());
    private IBeezigService mainService;

    public IBeezigService getMainService() {
        return mainService;
    }

    public void attemptLoad() {
        Iterator<IBeezigService> iter = services.iterator();
        if(iter.hasNext()) {
            mainService = iter.next();
            registerCallbacks();
        }
    }

    private void registerCallbacks() {
        mainService.registerUserIndicator(uuid -> {
            BeezigNetManager mgr = Beezig.get().getNetworkManager();
            if(mgr == null || mgr.getProfilesCache() == null) return 0;
            return Beezig.get().getNetworkManager().getProfilesCache().getIfPresent(uuid)
                    .map(user -> user.getRole().ordinal() + 1).orElse(0);
        });
    }
}
