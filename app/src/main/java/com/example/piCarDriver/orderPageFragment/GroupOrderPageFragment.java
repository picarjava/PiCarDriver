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

import com.example.piCarDriver.DriverCallBack;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.orderAdapter.OrderAdapter;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GroupOrderPageFragment extends Fragment {
    private final static String TAG = "LongTermOrderPageFragment";
    private DriverCallBack driverCallBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        driverCallBack = (DriverCallBack) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order_page, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getGroupOrder");
            String jsonIn = new CommonTask().execute("/groupOrderApi", jsonObject.toString()).get();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            Type type =  new TypeToken<List<List<Order>>>(){}.getType();
            List<List<Order>> orders = gson.fromJson(jsonIn, type);
            List<OrderAdapterType> orderAdapterTypes = orders.stream()
                                                             .map(o -> new OrderAdapterType(o, OrderAdapterType.GROUP_ORDER))
                                                             .collect(Collectors.toList());
            recyclerView.setAdapter(new OrderAdapter(orderAdapterTypes, driverCallBack.driverCallBack()));
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }
}
