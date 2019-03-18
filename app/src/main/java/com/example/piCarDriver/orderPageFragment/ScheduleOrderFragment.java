package com.example.piCarDriver.orderPageFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.piCarDriver.Driver;
import com.example.piCarDriver.DriverCallBack;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScheduleOrderFragment extends Fragment {
    private DriverCallBack activity;
    private List<Order> orders;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DriverCallBack) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order_page, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getNewSingleOrder");
            String jsonIn = new CommonTask().execute("/singleOrderApi", jsonObject.toString()).get();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            Type type =  new TypeToken<List<Order>>(){}.getType();
            orders = gson.fromJson(jsonIn, type);
            Driver driver = activity.driverCallBack();
//            recyclerView.setAdapter(new OrderAdapter(orders, driver));
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }
}