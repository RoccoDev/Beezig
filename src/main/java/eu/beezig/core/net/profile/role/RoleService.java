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

package eu.beezig.core.net.profile.role;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.beezig.core.Beezig;
import eu.beezig.hiveapi.wrapper.utils.download.Downloader;
import eu.beezig.hiveapi.wrapper.utils.json.JObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class RoleService {
    private static AsyncCache<Integer, UserRole> roleCache = Caffeine.newBuilder().maximumSize(100).buildAsync();

    public static CompletableFuture<UserRole> getRole(int id) {
        UserRole role = DefaultUserRoles.fromIndex(id);
        if(role != null) return CompletableFuture.completedFuture(role);
        return roleCache.get(id, RoleService::getRemoteRole);
    }

    private static UserRole getRemoteRole(int id) {
        JObject json = null;
        try {
            json = Downloader.getJsonObject(new URL("https://web.beezig.eu/v1/server/roles/" + id)).join();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return Beezig.gson.fromJson(json.getInput().toJSONString(), RemoteUserRole.class);
    }
}
