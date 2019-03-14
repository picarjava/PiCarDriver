package com.example.piCarDriver.webSocket;

import android.os.Handler;
import android.util.Log;

import com.example.piCarDriver.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class LocationWebSocket extends WebSocketClient {
    private final static String TAG = "LocationWebSocket";
    private android.os.Handler handler;

    public LocationWebSocket(Handler handler, URI serverUri) {
        super(serverUri, new Draft_6455());
        this.handler = handler;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.d(TAG, handshakeData.getHttpStatus() + " " + handshakeData.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        JsonObject jsonObject = new GsonBuilder().setDateFormat("yyyy-MM-dd mm:ss").create().fromJson(message, JsonObject.class);
        if (jsonObject.has("singleOrder"))
            Log.d(TAG, jsonObject.get("singleOrder").getAsString());
        handler.obtainMessage(Constants.ON_WEB_SOCKET_RECEIVED, message).sendToTarget();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
