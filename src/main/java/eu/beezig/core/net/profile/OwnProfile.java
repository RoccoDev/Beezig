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

package eu.beezig.core.net.profile;

import eu.beezig.core.net.profile.role.DefaultUserRoles;
import eu.beezig.core.net.profile.role.RoleService;
import eu.beezig.core.net.profile.role.UserRole;

import java.util.Date;

public class OwnProfile {
    private int id;
    private UserRole role;
    private Date firstLogin;
    private Region region;

    public OwnProfile(int id, byte role, long firstLogin, String regionId, String regionName) {
        this.id = id;
        this.role = DefaultUserRoles.USER;
        RoleService.getRole(role).thenAcceptAsync(r -> this.role = r);
        this.firstLogin = new Date(firstLogin);
        if(regionId != null) {
            this.region = new Region(regionId, regionName);
        }
    }

    public int getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public Region getRegion() {
        return region;
    }
}
