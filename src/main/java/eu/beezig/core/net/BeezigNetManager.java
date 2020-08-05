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

package eu.beezig.core.net;

import eu.beezig.core.Beezig;
import eu.beezig.core.net.handler.Connection;
import eu.beezig.core.net.handler.NetworkDecoder;
import eu.beezig.core.net.handler.NetworkEncoder;
import eu.beezig.core.net.profile.OwnProfile;
import eu.beezig.core.net.profile.ProfilesCache;
import eu.beezig.core.net.session.NetSessionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class BeezigNetManager {

    private OwnProfile profile;
    private Protocol protocol;
    private boolean reconnecting;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private static final int reconnectAdd = ThreadLocalRandom.current().nextInt(30);
    private static int reconnectTries;
    private static final int MAX_RECONNECT_TIME = 400;
    private Connection handler;
    private NetSessionManager sessionManager;
    private static ProfilesCache profilesCache = new ProfilesCache();

    public NetSessionManager getSessionManager() {
        return sessionManager;
    }

    public ProfilesCache getProfilesCache() {
        return profilesCache;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public BeezigNetManager() {
        protocol = new Protocol();
        sessionManager = new NetSessionManager();
    }

    public void connect() {
        Beezig.get().getAsyncExecutor().execute(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized OwnProfile getProfile() {
        return profile;
    }

    public synchronized void setProfile(OwnProfile profile) {
        this.profile = profile;
    }

    public AtomicBoolean isConnected() {
        return connected;
    }

    private void start() throws InterruptedException {
        reconnecting = false;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap client = new Bootstrap();
        client.group(group);
        client.channel(NioSocketChannel.class);
        client.remoteAddress(new InetSocketAddress(Beezig.DEBUG ? "localhost" : "tcp.beezig.eu", 20726));
        client.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                handler = new Connection();
                ch.pipeline().addLast(
                        new NetworkDecoder(), new NetworkEncoder(), handler);
                Beezig.api().getPluginManager().registerListener(Beezig.get(), handler);
                handler.init();
            }
        });
        client.connect().syncUninterruptibly();
    }

    public void reconnect() {
        reconnect(Beezig.DEBUG ? 10 : 30);
    }

    /**
     * Reconnects to the server with a random amount of seconds added to the time.
     *
     * @param time After how many seconds the client should reconnect to the server.
     */
    private void reconnect(int time) {
        final int seconds = reconnectAdd + (int) (MAX_RECONNECT_TIME - (MAX_RECONNECT_TIME - time) * Math.pow(Math.E, -0.1 * reconnectTries));
        reconnectIn(seconds);
    }

    private void reconnectIn(int seconds) {
        if (reconnecting)
            return;
        reconnecting = true;
        reconnectTries++;
        Beezig.logger.info(String.format("Reconnecting in %d seconds...", seconds));
        Beezig.get().getAsyncExecutor().execute(() -> {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            if (connected.get()) return;
            try {
                Beezig.net().start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public Connection getHandler() {
        return handler;
    }
}
