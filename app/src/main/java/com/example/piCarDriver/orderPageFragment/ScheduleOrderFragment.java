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
import com.example.piCarDriver.MainActivity;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.GroupOrder;
import com.example.piCarDriver.model.LongTermOrder;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.model.SingleOrder;
import com.example.piCarDriver.orderAdapter.OrderAdapter;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScheduleOrderFragment extends Fragment {
    private final static String TAG = "ScheduleOrderFragment";
    private MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_schedule_order, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        Driver driver = activity.driverCallBack();
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getScheduledOrder");
            jsonObject.addProperty("driverID", driver.getDriverID());
            List<OrderAdapterType> orderAdapterTypes = getSingleOrder(new CommonTask().execute("/singleOrderApi", jsonObject.toString()).get());
            orderAdapterTypes.addAll(getGroupOrder(new CommonTask().execute("/groupOrderApi", jsonObject.toString()).get()));
            orderAdapterTypes.sort(Comparator.comparing(ot -> ot.getOrder().getStartTime()));
            recyclerView.setAdapter(new OrderAdapter(orderAdapterTypes, driver, activity));
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }

    private List<OrderAdapterType> getSingleOrder(String jsonIn) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
        JsonObject jsonObjIn = gson.fromJson(jsonIn, JsonObject.class);
        List<OrderAdapterType> orderAdapterTypes = new ArrayList<>();
        List<SingleOrder> singleOrders = gson.fromJson(jsonObjIn.get("singleOrder"), new TypeToken<List<SingleOrder>>(){}.getType());
        if (singleOrders != null)
            singleOrders.stream()
                        .map(o -> new OrderAdapterType(o, OrderAdapterType.SINGLE_ORDER))
                        .forEach(orderAdapterTypes::add);

        List<LongTermOrder> longTermOrders = gson.fromJson(jsonObjIn.get("longTermOrder"), new TypeToken<List<LongTermOrder>>(){}.getType());
        if (longTermOrders != null)
            longTermOrders.stream()
                          .map(o -> new OrderAdapterType(o, OrderAdapterType.LONG_TERM_ORDER))
                          .forEach(orderAdapterTypes::add);

        return orderAdapterTypes;
    }

    private List<OrderAdapterType> getGroupOrder(String jsonIn) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
        JsonObject jsonObjIn = gson.fromJson(jsonIn, JsonObject.class);
        List<OrderAdapterType> orderAdapterTypes = new ArrayList<>();
        List<GroupOrder> groupOrders = gson.fromJson(jsonObjIn.get("groupOrder"), new TypeToken<List<GroupOrder>>(){}.getType());
        if (groupOrders != null)
            groupOrders.stream()
                       .map(o -> new OrderAdapterType(o, OrderAdapterType.GROUP_ORDER))
                       .forEach(orderAdapterTypes::add);

        List<GroupOrder> longTermGroupOrders = gson.fromJson(jsonObjIn.get("longTermGroupOrder"), new TypeToken<List<GroupOrder>>(){}.getType());
        if (longTermGroupOrders != null)
            longTermGroupOrders.stream()
                               .map(o -> new OrderAdapterType(o, OrderAdapterType.LONG_TERM_GROUP_ORDER))
                               .forEach(orderAdapterTypes::add);

        return orderAdapterTypes;
    }
}
