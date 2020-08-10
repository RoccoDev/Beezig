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

package eu.beezig.core.server;

import eu.beezig.core.Beezig;
import eu.beezig.core.api.BeezigForge;
import eu.beezig.core.automessage.AutoMessageManager;
import eu.beezig.core.server.listeners.*;
import eu.beezig.core.util.UUIDUtils;
import eu.beezig.core.util.task.WorldTask;
import eu.beezig.core.util.text.Message;
import eu.beezig.hiveapi.wrapper.player.HivePlayer;
import eu.beezig.hiveapi.wrapper.player.Profiles;
import eu.the5zig.mod.server.*;

import java.util.Locale;

public class ServerHive extends ServerInstance {

    private long tokens;
    private int medals;
    private String lobby;
    private HivePlayer profile;
    private boolean inParty;
    private boolean inPartyChat;
    private ListenerHive listener;

    public long getTokens() {
        return tokens;
    }

    public int getMedals() {
        return medals;
    }

    public String getLobby() {
        return lobby;
    }

    public void setLobby(String lobby) {
        this.lobby = lobby;
        if(getGameListener().getCurrentGameMode() == null) {
            String id = lobby.replaceAll("\\d", "").toLowerCase(Locale.ROOT);
            if("hub".equals(id) || "premhub".equals(id)) return;
            listener.updateLobby(id);
        }
    }

    public HivePlayer getProfile() {
        return profile;
    }

    public boolean getInParty() {
        return inParty;
    }

    public boolean getInPartyChat() {
        return inPartyChat;
    }

    public void addTokens(int tokens) {
        this.tokens += tokens;
    }

    @Override
    public void registerListeners() {
        GameListenerRegistry registry = getGameListener();
        registry.registerListener(listener = new ListenerHive());
        registry.registerListener(new TIMVListener());
        registry.registerListener(new BEDListener());
        registry.registerListener(new SKYListener());
        registry.registerListener(new HIDEListener());
        registry.registerListener(new DRListener());
        registry.registerListener(new SHUListener());
        registry.registerListener(new GRAVListener());
    }

    @Override
    public String getName() {
        return "The Hive";
    }

    @Override
    public String getConfigName() {
        return "hive";
    }

    private void onServerJoin() {
        Profiles.global(UUIDUtils.strip(Beezig.user().getId())).thenAcceptAsync(profile -> {
            this.profile = profile;
            this.tokens = profile.getTokens();
            this.medals = (int) profile.getMedals();
            Beezig.logger.info(String.format("Loaded profile for current user. Data as of %s", Message.date(profile.getCachedAt())));
        }).exceptionally(e -> {
            Message.error(Message.translate("error.token_fetch"));
            Beezig.logger.error(e);
            return null;
        });
        if(Beezig.get().getTemporaryPointsManager() != null)
            Beezig.get().getTemporaryPointsManager().startSession();
        Beezig.get().getData().getAutoGGManager().initTriggers();
        Beezig.get().getData().getAutoGLManager().initTriggers();
        Beezig.get().getData().getAutoNewGameManager().initTriggers();
        if(BeezigForge.isSupported()) BeezigForge.get().setOnHive(true);
    }

    @Override
    public boolean handleServer(String host, int port) {
        if(host.toLowerCase(Locale.ROOT).contains("hivemc.") || host.toLowerCase(Locale.ROOT).endsWith("hive.sexy")
                || host.toLowerCase(Locale.ROOT).contains(".j2o")) {
            onServerJoin();
            return true;
        }
        return false;
    }

    public static boolean isCurrent() {
        return Beezig.api().getActiveServer() instanceof ServerHive;
    }

    private class ListenerHive extends AbstractGameListener<GameMode> {
        private boolean readyForLobby = true;

        @Override
        public Class<GameMode> getGameMode() {
            return null;
        }

        @Override
        public boolean matchLobby(String s) {
            return false;
        }

        void updateLobby(String id) {
            getGameListener().switchLobby(id);
            if(BeezigForge.isSupported()) BeezigForge.get().setCurrentGame(id);
            if(getGameListener().getCurrentGameMode() instanceof HiveMode)
                ((HiveMode) getGameListener().getCurrentGameMode()).onModeJoin();
        }

        @Override
        public void onMatch(GameMode gameMode, String key, IPatternResult match) {
            if(key == null) return;
            Beezig.logger.debug(String.format("[ServerHive] Matched key %s, %d groups", key, match.size()));

            if(gameMode == null && key.startsWith("join.")) {
                String id = key.replace("join.", "");
                updateLobby(id);
            }
            Beezig.get().getNotificationManager().onMatch(key, match);
            Beezig.get().getAntiSniper().onMatch(key, match);
            if("tokens".equals(key)) ServerHive.this.tokens = Integer.parseInt(match.get(1), 10);
            else if("tokens.boost".equals(key)) ServerHive.this.tokens += Integer.parseInt(match.get(0), 10);
            else if("medals".equals(key)) ServerHive.this.medals = Integer.parseInt(match.get(0), 10);
            else if("lobby".equals(key)) {
                readyForLobby = true;
                ServerHive.this.setLobby(match.get(0));
            }
            else if("gameid".equals(key) && gameMode instanceof HiveMode) ((HiveMode)gameMode).setGameID(match.get(0));
            else if("map".equals(key) && gameMode instanceof HiveMode) ((HiveMode)gameMode).setMap(match.get(0));
            else if("autovote.map".equals(key) && gameMode instanceof HiveMode) ((HiveMode)gameMode).getAutovoteManager().parse(match);
            else if("partychat.enable".equals(key)) inPartyChat = true;
            else if("partychat.disable".equals(key)) {
                inPartyChat = false;
                // Send queued AutoGG and AutoGL messages
                AutoMessageManager.sendQueuedMessages();
            }
            else if("party.join".equals(key) || "party.create".equals(key)) inParty = true;
            else if("party.leave".equals(key) || "party.disband".equals(key)) {
                inParty = false;
                inPartyChat = false;
            }
            else if(key.endsWith(".setstate")) {
                gameMode.setState(GameState.GAME);
                WorldTask.submit(() -> Beezig.api().sendPlayerMessage("/gameid"));
            }
            // Advanced Records
            if(gameMode instanceof HiveMode) match.ignoreMessage(((HiveMode) gameMode).getAdvancedRecords().parseMessage(key, match));
        }

        @Override
        public void onServerConnect(GameMode gameMode) {
            if(gameMode instanceof HiveMode) {
                if(gameMode.getState() != GameState.LOBBY) ((HiveMode)gameMode).end();
            }
            if(readyForLobby) {
                readyForLobby = false;
                WorldTask.submit(() -> Beezig.api().sendPlayerMessage("/whereami"));
            }
            getGameListener().switchLobby(null);
            if(BeezigForge.isSupported()) BeezigForge.get().setCurrentGame(null);
        }

        @Override
        public void onServerDisconnect(GameMode gameMode) {
            if(gameMode instanceof HiveMode && gameMode.getState() != GameState.LOBBY) {
                ((HiveMode)gameMode).end();
            }
            if(Beezig.get().getTemporaryPointsManager() != null)
                Beezig.get().getTemporaryPointsManager().endSession();
            if(BeezigForge.isSupported()) BeezigForge.get().setOnHive(false);
        }

        @Override
        public void onTick(GameMode gameMode) {
            if(gameMode instanceof HiveMode) {
                HiveMode mode = (HiveMode) gameMode;
                mode.getStatsFetcher().attemptCompute(mode, Beezig.api().getSideScoreboard());
            }
            Beezig.net().getProfilesCache().tryUpdateList();
        }
    }
}
