package com.example.piCarDriver.task;

import android.os.AsyncTask;

import com.example.piCarDriver.MainActivity;
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

public class LoginTask extends AsyncTask<String, Void, String> {
    private WeakReference<MainActivity> context;
    private ProgressDialogFragment fragment;
    public LoginTask(MainActivity context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity mainActivity = context.get();
        if (mainActivity != null) {
            fragment = new ProgressDialogFragment();
            fragment.show(mainActivity.getSupportFragmentManager(), "TTT");
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        String account = strings[1];
        String password = strings[2];
        String jsonIn = null;
        try {
            jsonIn = getRemoteData(url, account, password);
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

    private String getRemoteData(String url, String account, String password) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", "charset=utf-8;");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "login");
        jsonObject.addProperty("account", account);
        jsonObject.addProperty("password", password);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bufferedWriter.write(jsonObject.toString());
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