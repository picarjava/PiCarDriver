package com.example.piCarDriver.task;

import android.os.AsyncTask;

import com.example.piCarDriver.ProgressDialogFragment;
import com.example.piCarDriver.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommonTask extends AsyncTask<String, Void, String> {
    private final static String TAG = "CommonTask";
    private ProgressDialogFragment fragment;

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        String jsonIn = null;
        try {
            jsonIn = getRemoteData(Constants.URL + url, strings[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonIn;
    }

    private String getRemoteData(String url, String jsonOut) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", "charset=utf-8;");
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bufferedWriter.write(jsonOut);
        bufferedWriter.close();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonIn = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            jsonIn.append(line);
        }

        bufferedReader.close();
        connection.disconnect();
        return jsonIn.toString();
    }
}
