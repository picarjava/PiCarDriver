package com.example.piCarDriver.orderAdapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.piCarDriver.Driver;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.SingleOrder;
import com.example.piCarDriver.Util;
import com.example.piCarDriver.task.OrderTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LongTermOrderAdapter extends RecyclerView.Adapter<LongTermOrderAdapter.ViewHolder> {
    private List<List<SingleOrder>> orders;
    private Driver driver;

    public LongTermOrderAdapter(List<List<SingleOrder>> orders, Driver driver) {
        this.orders = orders;
        this.driver = driver;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLoc;
        TextView endLoc;
        TextView startTime;
        TextView endTime;
        TextView amount;
        Button btnAccept;

        private ViewHolder(@NonNull View view) {
            super(view);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
            endTime = view.findViewById(R.id.endTime);
            amount = view.findViewById(R.id.amount);
            btnAccept = view.findViewById(R.id.acceptOrder);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_longterm_order, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        int position = viewHolder.getAdapterPosition();
        List<SingleOrder> ordersNow = orders.get(position);
        SingleOrder startOrder = ordersNow.get(0);
        SingleOrder endOrder = ordersNow.get(ordersNow.size() - 1);
        viewHolder.startLoc.setText(startOrder.getStartLoc());
        viewHolder.endLoc.setText(startOrder.getEndLoc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        viewHolder.startTime.setText(simpleDateFormat.format(startOrder.getStartTime()));
        viewHolder.endTime.setText(simpleDateFormat.format(endOrder.getStartTime()));
        int amount = 0;
        for (SingleOrder order: ordersNow)
            amount += order.getTotalAmount();

        viewHolder.amount.setText(String.valueOf(amount));
        viewHolder.btnAccept.setOnClickListener((view)->{
            List<String> orderIDs = new ArrayList<>();
            for (SingleOrder singleOrder: ordersNow)
                orderIDs.add(singleOrder.getOrderID());

            JsonObject jsonOut = new JsonObject();
            jsonOut.addProperty("action", "takeLongTermOrder");
            jsonOut.addProperty("driverID", driver.getDriverID());
            jsonOut.addProperty("orderID", new Gson().toJson(orderIDs));
            try {
                new OrderTask(view.getContext()).execute(Util.URL + "/singleOrderApi", jsonOut.toString()).get();
                orders.remove(position);
                notifyItemRemoved(position);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
