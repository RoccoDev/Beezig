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

package eu.beezig.core.net.handler;

import com.google.common.collect.Queues;
import com.mojang.authlib.exceptions.AuthenticationException;
import eu.beezig.core.Beezig;
import eu.beezig.core.net.Packet;
import eu.beezig.core.net.packets.PacketAuthentication;
import eu.beezig.core.net.packets.PacketIdentification;
import eu.beezig.core.util.ExceptionHandler;
import eu.beezig.core.util.text.Message;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.event.TickEvent;
import eu.the5zig.util.minecraft.ChatColor;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.GenericFutureListener;

import java.security.NoSuchAlgorithmException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection extends SimpleChannelInboundHandler<Packet> {

    private final Queue<QueuedPacket> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private boolean disconnected;
    private String disconnectReason;
    private int handshakeSecret;
    private AtomicBoolean authenticated = new AtomicBoolean(false);

    public void setHandshakeSecret(int handshakeSecret) {
        this.handshakeSecret = handshakeSecret;
    }

    public void init() {
        sendPacket(new PacketIdentification());
    }

    public void authenticate() {
        Beezig.logger.info("Attempting to authenticate...");
        try {
            Beezig.get().getNetworkManager().getSessionManager().sendAuthRequest(handshakeSecret);
            sendPacket(new PacketAuthentication());
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            ExceptionHandler.catchException(e, "Couldn't authenticate connection");
        }
    }

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public void setAuthenticated() {
        authenticated.set(true);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        if (isChannelOpen()) {
            packet.handle(this);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        disconnect();
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        this.channel = channelHandlerContext.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ExceptionHandler.catchException(cause, "An Internal Exception occurred");
        if (cause instanceof ReadTimeoutException)
            disconnect(Message.translate("connection.timed_out"));
        else
            disconnect(Message.translate("connection.internal_error"));
    }

    private void disconnect() {
        disconnect(Message.translate("connection.closed"));
    }

    public void disconnect(String disconnectReason) {
        if (isChannelOpen()) {
            closeChannel();
            this.disconnectReason = disconnectReason;
        }
    }

    public boolean disconnectSilently() {
        if(isChannelOpen() && !this.disconnected) {
            this.disconnected = true;
            closeChannel();
            Beezig.net().setConnected(false);
            return true;
        }
        return false;
    }

    private boolean checkDisconnected() {
        if (!hasNoChannel() && !this.isChannelOpen() && !this.disconnected) {
            this.disconnected = true;
            if(disconnectReason == null) disconnectReason = Message.translate("connection.internal_error");
            Beezig.logger.info("Disconnected: " + disconnectReason);
            Beezig.api().createOverlay().displayMessageAndSplit(ChatColor.YELLOW + Beezig.api().translate("connection.disconnected", disconnectReason));
            Beezig.net().setConnected(false);
            Beezig.net().reconnect();
            return true;
        }
        return false;
    }

    /**
     * Checks timeouts and processes all packets received
     */
    @EventHandler
    public void tick(TickEvent event) {
        this.flushOutboundQueue();
        if (isChannelOpen())
            this.channel.flush();

        checkDisconnected();
    }

    /**
     * Will iterate through the outboundPacketQueue and dispatch all Packets
     */
    private void flushOutboundQueue() {
        if (isChannelOpen()) {
            while (!outboundPacketsQueue.isEmpty()) {
                QueuedPacket packet = outboundPacketsQueue.poll();
                dispatchPacket(packet.packet, packet.listeners);
            }
        }
    }

    public void sendPacket(Packet packet, GenericFutureListener... listeners) {
        if (isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packet, listeners);
        } else {
            outboundPacketsQueue.add(new QueuedPacket(packet, listeners));
        }
    }

    private void dispatchPacket(final Packet packet, final GenericFutureListener[] listeners) {
        if (checkDisconnected())
            return;
        if (this.channel.eventLoop().inEventLoop()) {
            ChannelFuture future = channel.writeAndFlush(packet);
            if (listeners != null) {
                future.addListeners(listeners);
            }
            future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            this.channel.eventLoop().execute(() -> {
                ChannelFuture future = channel.writeAndFlush(packet);
                if (listeners != null) {
                    future.addListeners(listeners);
                }
                future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }

    private boolean isChannelOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    private boolean hasNoChannel() {
        return this.channel == null;
    }

    /**
     * Closes the channel
     */
    private void closeChannel() {
        if (this.channel.isOpen()) {
            this.channel.close().awaitUninterruptibly();
        }
        Beezig.api().getPluginManager().unregisterListener(Beezig.get(), this);
    }

    private static class QueuedPacket {

        private Packet packet;
        private GenericFutureListener[] listeners;

        QueuedPacket(Packet packet, GenericFutureListener[] listeners) {
            this.packet = packet;
            this.listeners = listeners;
        }
    }
}
