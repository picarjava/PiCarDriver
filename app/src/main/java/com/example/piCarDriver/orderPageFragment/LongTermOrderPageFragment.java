package com.example.piCarDriver.orderPageFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.piCarDriver.R;
import com.example.piCarDriver.SingleOrder;
import com.example.piCarDriver.Util;
import com.example.piCarDriver.orderAdapter.LongTermOrderAdapter;
import com.example.piCarDriver.task.OrderTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LongTermOrderPageFragment extends Fragment {
    private final static String TAG = "LongTermOrderPageFragment";
    private List<List<SingleOrder>> orders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order_page, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        try {
            String jsonIn = new OrderTask(getActivity()).execute(Util.URL + "/singleOrderApi", "getLongTermSingleOrder").get();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            Type type =  new TypeToken<List<List<SingleOrder>>>(){}.getType();
            orders = gson.fromJson(jsonIn, type);
            recyclerView.setAdapter(new LongTermOrderAdapter(orders));
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return view;
    }
}