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
import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class LongTermOrderAdapter extends RecyclerView.Adapter<LongTermOrderAdapter.ViewHolder> {
    private List<List<Order>> orders;
    private Driver driver;

    public LongTermOrderAdapter(List<List<Order>> orders, Driver driver) {
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
        List<Order> ordersNow = orders.get(position);
        Order startOrder = ordersNow.get(0);
        Order endOrder = ordersNow.get(ordersNow.size() - 1);
        viewHolder.startLoc.setText(startOrder.getStartLoc());
        viewHolder.endLoc.setText(startOrder.getEndLoc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        viewHolder.startTime.setText(simpleDateFormat.format(startOrder.getStartTime()));
        viewHolder.endTime.setText(simpleDateFormat.format(endOrder.getStartTime()));
        int amount = (int) ordersNow.stream()
                                    .mapToDouble(Order::getTotalAmount)
                                    .reduce((acc, totalAmount) -> acc + totalAmount)
                                    .orElse(0);
        viewHolder.amount.setText(String.valueOf(amount));
        viewHolder.btnAccept.setOnClickListener((view)->{
            List<String> orderIDs = ordersNow.stream()
                                             .map(Order::getOrderID)
                                             .collect(Collectors.toList());
            JsonObject jsonOut = new JsonObject();
            jsonOut.addProperty("action", "takeLongTermOrder");
            jsonOut.addProperty("driverID", driver.getDriverID());
            jsonOut.addProperty("orderID", new Gson().toJson(orderIDs));
            new CommonTask().execute("/singleOrderApi", jsonOut.toString());
            orders.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
