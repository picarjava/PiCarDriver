package com.example.piCarDriver.webSocket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.piCarDriver.model.Order;

public class WebSocketHandler extends Handler {
    private final static String TAG = "WebSocketHandler";
    final static int SINGLE_ORDER_RECEIVED = 0;
    final static int GET_IN_SUCCEED = 1;

    public interface WebSocketCallBack {
        void drawDirectionCallBack(Order order);
        void getInSuccessCallBack();
    }

    private WebSocketCallBack webSocketCallBack;

    public WebSocketHandler(WebSocketCallBack webSocketCallBack) {
        this.webSocketCallBack = webSocketCallBack;
    }

    @Override
    public void handleMessage(Message message) {
        Log.d(TAG, "handleMessage");
        switch (message.what) {
            case SINGLE_ORDER_RECEIVED:
                webSocketCallBack.drawDirectionCallBack((Order) message.obj);
                break;
            case GET_IN_SUCCEED:
                webSocketCallBack.getInSuccessCallBack();
                break;
        }
    }
}
