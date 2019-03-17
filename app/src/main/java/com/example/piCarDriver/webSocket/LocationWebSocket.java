package com.example.piCarDriver.webSocket;

import android.os.Handler;
import android.util.Log;

import com.example.piCarDriver.model.SingleOrder;
import com.google.gson.Gson;
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
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd mm:ss").create();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        String singleOrder = "singleOrder";
        String state = "state";
        if (jsonObject.has(singleOrder)) {
            SingleOrder vo = gson.fromJson(jsonObject.get(singleOrder).getAsString(), SingleOrder.class);
            handler.obtainMessage(WebSocketHandler.SINGLE_ORDER_RECEIVED, vo).sendToTarget();
        } else if (jsonObject.has(state)) {
            if ("OK".equals(jsonObject.get(state).getAsString()))
                handler.obtainMessage(WebSocketHandler.GET_IN_SUCCEED).sendToTarget();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
