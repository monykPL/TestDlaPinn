/*
 * Copyright (c) 2022 DupliCAT
 * GNU Lesser General Public License v3.0
 */

package dev.melonclient.feature.discord;

import com.google.gson.JsonObject;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Minimalny klient protokołu Discord IPC - bez JNA i bez żadnych natywnych
 * bibliotek, żeby nie kolidować ze starą wersją JNA (3.4.0) już używaną przez
 * LWJGL/Forge w tym projekcie.
 *
 * Discord po stronie lokalnej wystawia named pipe (Windows) albo unix socket
 * (Linux/Mac). Java 8 nie ma wbudowanego wsparcia dla unix socketów (dodano
 * to dopiero w Javie 16), ale named pipe na Windowsie można otworzyć zwykłym
 * RandomAccessFile jak plik - stąd ta implementacja działa tylko na Windows.
 */
public class DiscordRPC {

    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;

    private final String clientId;
    private RandomAccessFile pipe;
    private boolean connected;

    public DiscordRPC(String clientId) {
        this.clientId = clientId;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Próbuje połączyć się z Discordem, sprawdzając pipe'y discord-ipc-0..9
     * (wyższe numery bywają zajęte przez inne aplikacje używające Rich Presence).
     * Zwraca false jeśli Discord w ogóle nie jest uruchomiony - to nie błąd,
     * po prostu nie ma z kim się połączyć, spróbujemy ponownie za chwilę.
     */
    public boolean connect() {
        for (int i = 0; i < 10; i++) {
            try {
                pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");

                JsonObject handshake = new JsonObject();
                handshake.addProperty("v", 1);
                handshake.addProperty("client_id", clientId);
                writeFrame(OP_HANDSHAKE, handshake.toString());

                String response = readFrame();
                if (response != null) {
                    connected = true;
                    return true;
                }
            } catch (Exception ignored) {
                // ten konkretny pipe nie istnieje albo jest zajęty - próbujemy kolejny numer
            }
        }
        connected = false;
        return false;
    }

    /**
     * Ustawia obecną aktywność (Rich Presence) widoczną na profilu Discorda.
     *
     * @param details    główny tytuł (pogrubiona pierwsza linijka) - u nas zawsze "MelonClient"
     * @param state      podtytuł (druga linijka) - "Singleplayer" albo adres serwera
     * @param largeImage klucz grafiki wgranej w Discord Developer Portal (Rich Presence > Art Assets)
     * @param largeText  tekst pokazywany po najechaniu na dużą ikonkę
     * @param startTime  unix millis - od kiedy liczyć widoczny licznik czasu ("for X minutes")
     */
    public void setActivity(String details, String state, String largeImage, String largeText, long startTime) {
        if (!connected) {
            return;
        }
        try {
            JsonObject timestamps = new JsonObject();
            timestamps.addProperty("start", startTime);

            JsonObject assets = new JsonObject();
            assets.addProperty("large_image", largeImage);
            assets.addProperty("large_text", largeText);

            JsonObject activity = new JsonObject();
            activity.addProperty("details", details);
            activity.addProperty("state", state);
            activity.add("timestamps", timestamps);
            activity.add("assets", assets);

            JsonObject args = new JsonObject();
            args.addProperty("pid", getPid());
            args.add("activity", activity);

            JsonObject payload = new JsonObject();
            payload.addProperty("cmd", "SET_ACTIVITY");
            payload.add("args", args);
            payload.addProperty("nonce", UUID.randomUUID().toString());

            writeFrame(OP_FRAME, payload.toString());
            readFrame(); // odczytaj odpowiedź, żeby nie zapychać bufora pipe'a
        } catch (Exception e) {
            connected = false;
        }
    }

    public void close() {
        try {
            if (pipe != null) {
                writeFrame(OP_CLOSE, "{}");
                pipe.close();
            }
        } catch (Exception ignored) {
        } finally {
            connected = false;
            pipe = null;
        }
    }

    private void writeFrame(int opcode, String json) throws Exception {
        byte[] data = json.getBytes("UTF-8");
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(opcode);
        header.putInt(data.length);
        pipe.write(header.array());
        pipe.write(data);
    }

    private String readFrame() throws Exception {
        byte[] header = new byte[8];
        int read = pipe.read(header);
        if (read < 8) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        buf.getInt();
        int length = buf.getInt();
        byte[] payload = new byte[length];
        pipe.readFully(payload);
        return new String(payload, "UTF-8");
    }

    private int getPid() {
        try {
            String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            return Integer.parseInt(jvmName.split("@")[0]);
        } catch (Exception e) {
            return 0;
        }
    }
}
