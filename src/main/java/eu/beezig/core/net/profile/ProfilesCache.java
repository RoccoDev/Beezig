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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;
import eu.beezig.core.Beezig;
import eu.beezig.core.net.packets.PacketOnlineUsers;
import eu.the5zig.mod.util.NetworkPlayerInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProfilesCache {
    private Cache<UUID, Optional<UserProfile>> profilesCache;
    private Queue<FutureTask> tasks = new ArrayDeque<>();
    private AtomicInteger lastRequestID;
    private AtomicBoolean updating;
    private Set<UUID> cachedPlayers;

    public ProfilesCache() {
        profilesCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(15, TimeUnit.MINUTES).build();
        lastRequestID = new AtomicInteger(0);
        updating = new AtomicBoolean(false);
        cachedPlayers = new HashSet<>();
        Beezig.api().getPluginManager().registerListener(Beezig.get(), new ProfileBadgeListener());
    }

    public long getSize() {
        return profilesCache.estimatedSize();
    }

    public void putAll(int requestId, Set<UserProfile> profiles) {
        if(profiles.size() == 0) {
            noResults(requestId);
            return;
        }
        for(UserProfile profile : profiles) {
            profilesCache.put(profile.getUuid(), Optional.of(profile));
        }
        FutureTask task = getTask(requestId);
        task.consumer.accept(profiles);
        Set<UUID> uuidsFound = profiles.stream().map(UserProfile::getUuid).collect(Collectors.toSet());
        task.uuids.removeAll(uuidsFound);
        for(UUID uuid : task.uuids) profilesCache.put(uuid, Optional.empty());
        tasks.remove(task);
    }

    private FutureTask getTask(int requestId) {
        for(FutureTask task : tasks) {
            if(task.requestId == requestId) return task;
        }
        return null;
    }

    public void tryUpdateList() {
        Collection<NetworkPlayerInfo> players = Beezig.api().getServerPlayers();
        if(players.size() == cachedPlayers.size()) return;
        Set<UUID> ids = players.stream().map(info -> info.getGameProfile().getId()).collect(Collectors.toSet());
        Set<UUID> diff = new HashSet<>(ids);
        diff.removeAll(cachedPlayers);
        diff.removeIf(id -> profilesCache.getIfPresent(id) != null);
        updateUnconditionally(diff);
        this.cachedPlayers = ids;
    }

    public Optional<UserProfile> getNowOrSubmit(UUID id, Collection<UUID> ids) {
        Optional<UserProfile> temp = profilesCache.getIfPresent(id);
        if(temp != null) return temp;
        if(!updating.get()) {
            updateUnconditionally(ids);
        }
        return Optional.empty();
    }

    public Optional<UserProfile> getIfPresent(UUID id) {
        return profilesCache.getIfPresent(id);
    }

    private void updateUnconditionally(Collection<UUID> ids) {
        updating.set(true);
        int id = lastRequestID.getAndIncrement();
        tasks.add(new FutureTask(id, new HashSet<>(ids), (profiles) -> updating.set(false)));
        Beezig.get().getAsyncExecutor().execute(() -> Beezig.net().getHandler().sendPacket(new PacketOnlineUsers(id, ids)));
    }

    private void noResults(int requestId) {
        for(Iterator<FutureTask> iter = tasks.iterator(); iter.hasNext();) {
            FutureTask task = iter.next();
            if(task.requestId == requestId) {
                task.consumer.accept(Sets.newHashSet());
                for(UUID uuid : task.uuids) profilesCache.put(uuid, Optional.empty());
                tasks.remove(task);
            }
        }
    }

    public CompletableFuture<Optional<UserProfile>> getProfile(UUID uuid) {
        CompletableFuture<Optional<UserProfile>> future = new CompletableFuture<>();
        Optional<UserProfile> temp = profilesCache.getIfPresent(uuid);
        if(temp != null) future.complete(temp);
        else {
            int id = lastRequestID.getAndIncrement();
            tasks.add(new FutureTask(id, Sets.newHashSet(uuid),
                    profiles -> future.complete(profiles.isEmpty() ? Optional.empty() : Optional.of(profiles.toArray(new UserProfile[0])[0]))));
            Beezig.get().getAsyncExecutor().execute(() -> Beezig.get().getNetworkManager().getHandler()
                    .sendPacket(new PacketOnlineUsers(id, uuid)));
        }
        return future;
    }

    private static class FutureTask {
        private int requestId;
        private Set<UUID> uuids;
        private Consumer<Set<UserProfile>> consumer;

        FutureTask(int requestId, Set<UUID> uuids, Consumer<Set<UserProfile>> consumer) {
            this.requestId = requestId;
            this.uuids = uuids;
            this.consumer = consumer;
        }
    }
}
