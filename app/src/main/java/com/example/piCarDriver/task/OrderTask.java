package com.example.piCarDriver.task;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.piCarDriver.ProgressDialogFragment;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class OrderTask extends AsyncTask<String, Void, String> {
    private final static String TAG = "OrderTask";
    private WeakReference<FragmentActivity> context;
    private ProgressDialogFragment fragment;
    public OrderTask(FragmentActivity context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        FragmentActivity mainActivity = context.get();
        if (mainActivity != null) {
            fragment = new ProgressDialogFragment();
            fragment.show(mainActivity.getSupportFragmentManager(), "TTT");
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        String jsonIn = null;
        try {
            jsonIn = getRemoteData(url, strings[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonIn;
    }

    @Override
    protected void onPostExecute(String jsonIn) {
        fragment.dismiss();
        super.onPostExecute(jsonIn);
    }

    private String getRemoteData(String url, String action) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", "charset=utf-8;");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bufferedWriter.write(jsonObject.toString());
        bufferedWriter.close();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonIn = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            jsonIn.append(line);
            Log.d(TAG, line);
        }

        bufferedReader.close();
        connection.disconnect();
        return jsonIn.toString();
    }
}