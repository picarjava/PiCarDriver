package com.example.piCarDriver.webSocket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WebSocketHandler extends Handler {
    private final static String TAG = "WebSocketHandler";
    public interface DrawDirectionCallBack {
        void drawDirectionCallBack(String message);
    }

    private DrawDirectionCallBack drawDirectionCallBack;

    public WebSocketHandler(DrawDirectionCallBack drawDirectionCallBack) {
        this.drawDirectionCallBack = drawDirectionCallBack;
    }

    @Override
    public void handleMessage(Message message) {
        Log.d(TAG, "handleMessage");
        drawDirectionCallBack.drawDirectionCallBack((String) message.obj);
    }
}
