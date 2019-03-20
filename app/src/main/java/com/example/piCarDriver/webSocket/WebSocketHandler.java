package com.example.piCarDriver.webSocket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.model.OrderAdapterType;

public class WebSocketHandler extends Handler {
    private final static String TAG = "WebSocketHandler";
    final static int SINGLE_ORDER_RECEIVED = 0;
    final static int GET_IN_SUCCEED = 1;

    public interface WebSocketCallBack {
        void drawDirectionCallBack(OrderAdapterType orderAdapterType);
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
                webSocketCallBack.drawDirectionCallBack(new OrderAdapterType((Order) message.obj, OrderAdapterType.SINGLE_ORDER));
                break;
            case GET_IN_SUCCEED:
                webSocketCallBack.getInSuccessCallBack();
                break;
        }
    }
}
