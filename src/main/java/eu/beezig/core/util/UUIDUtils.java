/*
 * Copyright (C) 2017-2021 Beezig Team
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

package eu.beezig.core.util;

import eu.beezig.core.Beezig;
import eu.beezig.core.config.Settings;
import eu.beezig.core.net.profile.UserProfile;
import eu.beezig.hiveapi.wrapper.exception.ProfileNotFoundException;
import eu.beezig.hiveapi.wrapper.mojang.UsernameToUuid;
import eu.beezig.hiveapi.wrapper.player.HivePlayer;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.the5zig.mod.util.NetworkPlayerInfo;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.util.minecraft.ChatColor;
import org.json.simple.parser.ParseException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UUIDUtils {
    public static CompletableFuture<String> getNameWithOptionalRank(String uuid, String rawName, HivePlayer profile) {
        if (!Settings.HIVE_RANK.get().getBoolean()) return CompletableFuture.completedFuture(Color.accent() + rawName);
        if (profile == null)
            return Profiles.global(uuid).thenApplyAsync(p -> Color.RankColor.safeGet(p.getRank().getEnumName()) + p.getUsername())
                .exceptionally(e -> {
                    if (!(e.getCause() instanceof ProfileNotFoundException) && !(e.getCause() instanceof ParseException))
                        ExceptionHandler.catchException(e, "UUIDUtils username query");
                    return Color.RankColor.REGULAR + rawName;
                });
        return CompletableFuture.completedFuture(Color.RankColor.safeGet(profile.getRank().getEnumName()) + profile.getUsername());
    }

    public static String strip(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    public static String getDisplayName(NetworkPlayerInfo info) {
        return info.getDisplayName() != null ? info.getDisplayName() : info.getGameProfile().getName();
    }

    public static MessageComponent getUserRole(UUID id) {
        if (id == null) return new MessageComponent("");
        Optional<UserProfile> profile = Beezig.net().getProfilesCache().getNowOrSubmit(id, Collections.singletonList(id));
        return profile.map(userProfile -> userProfile.getRoleContainer().getRole().getDisplayComponent()).orElseGet(() -> new MessageComponent(""));
    }

    public static String getShortRole(UUID id) {
        if (id == null) return "";
        Optional<UserProfile> profile = Beezig.net().getProfilesCache().getIfPresent(id);
        if (profile == null) return "";
        return profile.map(userProfile -> userProfile.getRoleContainer().getRole().getShortName()).orElse("");
    }

    public static UUID getLocalUUID(String name) {
        for (NetworkPlayerInfo info : Beezig.api().getServerPlayers()) {
            if (name.equalsIgnoreCase(ChatColor.stripColor(getDisplayName(info)))) {
                return info.getGameProfile().getId();
            }
        }
        return null;
    }

    public static CompletableFuture<UUID> getUUID(String name) {
        for (NetworkPlayerInfo info : Beezig.api().getServerPlayers()) {
            if (name.equalsIgnoreCase(ChatColor.stripColor(getDisplayName(info)))) {
                return CompletableFuture.completedFuture(info.getGameProfile().getId());
            }
        }
        return UsernameToUuid.getUUID(name).thenApplyAsync(s -> UUID.fromString(s.replaceAll(
            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
            "$1-$2-$3-$4-$5")));
    }
}
