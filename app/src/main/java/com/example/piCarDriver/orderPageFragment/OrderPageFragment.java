package com.example.piCarDriver.orderPageFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.piCarDriver.Driver;
import com.example.piCarDriver.DriverCallBack;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.GroupOrder;
import com.example.piCarDriver.model.LongTermOrder;
import com.example.piCarDriver.model.SingleOrder;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.orderAdapter.OrderAdapter;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class OrderPageFragment extends Fragment {
    private final static String TAG = "OrderPageFragment";
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
        assert getArguments() != null;
        String action = getArguments().getString("action");
        String url = getArguments().getString("url");
        int orderAdapterType = getArguments().getInt("orderAdapterType");
        try {
            String jsonIn = new CommonTask().execute(url, action).get();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            Driver driver = driverCallBack.driverCallBack();
            Type type;
            List<OrderAdapterType> orderAdapterTypes = null;
            switch (orderAdapterType) {
                case OrderAdapterType.SINGLE_ORDER:
                    type = new TypeToken<List<SingleOrder>>() {}.getType();
                    List<SingleOrder> singleOrders = gson.fromJson(jsonIn, type);
                    orderAdapterTypes = singleOrders.stream()
                                              .map(o -> new OrderAdapterType(o, orderAdapterType))
                                              .collect(Collectors.toList());
                    break;
                case OrderAdapterType.LONG_TERM_ORDER:
                    type = new TypeToken<List<LongTermOrder>>(){}.getType();
                    List<LongTermOrder> lOrders = gson.fromJson(jsonIn, type);
                    orderAdapterTypes = lOrders.stream()
                                               .map(o -> new OrderAdapterType(o, orderAdapterType))
                                               .collect(Collectors.toList());
                    break;
                case OrderAdapterType.GROUP_ORDER:
                case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                    type = new TypeToken<List<GroupOrder>>(){}.getType();
                    List<GroupOrder> gOrders = gson.fromJson(jsonIn, type);
                    Log.d(TAG, gOrders.toString());
                    orderAdapterTypes = gOrders.stream()
                                               .map(o -> new OrderAdapterType(o, orderAdapterType))
                                               .collect(Collectors.toList());
                    break;
            }

            recyclerView.setAdapter(new OrderAdapter(orderAdapterTypes, driver));
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }

}
