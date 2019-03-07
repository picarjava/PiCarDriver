package com.example.piCarDriver;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class LocationWebSocket extends WebSocketClient {
    private final static String TAG = "LocationWebSocket";

    public LocationWebSocket(URI serverUri) {
        super(serverUri, new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.d(TAG, handshakeData.getHttpStatus() + " " + handshakeData.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
