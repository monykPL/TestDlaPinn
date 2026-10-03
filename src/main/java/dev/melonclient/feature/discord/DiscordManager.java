/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.feature.discord;

import dev.melonclient.Melon;
import dev.melonclient.feature.option.Option;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DiscordManager {

    // UZUPEŁNIJ: Client ID Twojej aplikacji z https://discord.com/developers/applications
    private static final String CLIENT_ID = "1529402070275330059";

    // Klucze grafik wgranych w Discord Developer Portal -> Rich Presence -> Art Assets.
    // Nazwy kluczy w portalu MUSZĄ się zgadzać dokładnie z tymi stringami (małe litery).
    private static final String ASSET_SINGLEPLAYER = "minecraft";
    private static final String ASSET_MULTIPLAYER = "server";

    private final DiscordRPC rpc = new DiscordRPC(CLIENT_ID);
    private final Thread worker;

    private volatile String pendingState;
    private volatile String pendingAsset;
    private volatile String pendingAssetText;
    private volatile long pendingStartTime;
    private volatile boolean hasPendingUpdate;

    private String lastState = "";
    private int tickCounter;

    public DiscordManager() {
        worker = new Thread(this::workerLoop, "DiscordRPC");
        worker.setDaemon(true);
        worker.start();
    }

    private void workerLoop() {
        while (true) {
            try {
                if (!isEnabled()) {
                    if (rpc.isConnected()) {
                        rpc.close();
                    }
                    Thread.sleep(2000);
                    continue;
                }

                if (!rpc.isConnected()) {
                    rpc.connect();
                }

                if (rpc.isConnected() && hasPendingUpdate) {
                    rpc.setActivity("MelonClient", pendingState, pendingAsset, pendingAssetText, pendingStartTime);
                    hasPendingUpdate = false;
                }

                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                return;
            } catch (Exception e) {
                // ten wątek nigdy nie może umrzeć na cichym błędzie - po prostu czekamy i próbujemy dalej
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !isEnabled()) {
            return;
        }

        tickCounter++;
        if (tickCounter < 40) {
            return;
        }
        tickCounter = 0;

        String currentState;
        String state;
        String asset;
        String assetText;

        if (Melon.INSTANCE.mc.theWorld == null) {
            currentState = "menu";
            state = "In the main menu";
            asset = ASSET_SINGLEPLAYER;
            assetText = "MelonClient";
        } else if (Melon.INSTANCE.mc.isIntegratedServerRunning()) {
            currentState = "singleplayer";
            state = "Singleplayer";
            asset = ASSET_SINGLEPLAYER;
            assetText = "Singleplayer";
        } else {
            String serverIp = Melon.INSTANCE.mc.getCurrentServerData() != null
                    ? Melon.INSTANCE.mc.getCurrentServerData().serverIP
                    : "Multiplayer";
            currentState = "mp:" + serverIp;
            state = serverIp;
            asset = ASSET_MULTIPLAYER;
            assetText = serverIp;
        }

        if (!currentState.equals(lastState)) {
            lastState = currentState;
            pendingState = state;
            pendingAsset = asset;
            pendingAssetText = assetText;
            pendingStartTime = System.currentTimeMillis();
            hasPendingUpdate = true;
        }
    }

    private boolean isEnabled() {
        Option option = Melon.INSTANCE.optionManager.getOptionByName("Discord RPC");
        return option == null || option.isCheckToggled();
    }

    public void shutdown() {
        rpc.close();
        worker.interrupt();
    }
}
