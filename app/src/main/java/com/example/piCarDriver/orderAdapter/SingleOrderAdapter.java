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
import com.example.piCarDriver.Constants;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class SingleOrderAdapter extends RecyclerView.Adapter<SingleOrderAdapter.ViewHolder> {
    private List<SingleOrder> orders;
    private Driver driver;

    public SingleOrderAdapter(List<SingleOrder> orders, Driver driver) {
        this.orders = orders;
        this.driver = driver;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLoc;
        TextView endLoc;
        TextView startTime;
        TextView amount;
        Button btnAccept;

        private ViewHolder(@NonNull View view) {
            super(view);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
            amount = view.findViewById(R.id.amount);
            btnAccept = view.findViewById(R.id.acceptOrder);
        }
    }

    @NonNull
    @Override
    public SingleOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                                  .inflate(R.layout.view_single_order, viewGroup, false);
        return new SingleOrderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SingleOrderAdapter.ViewHolder viewHolder, int i) {
        SingleOrder order = orders.get(viewHolder.getAdapterPosition());
        viewHolder.startLoc.setText(order.getStartLoc());
        viewHolder.endLoc.setText(order.getEndLoc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        viewHolder.startTime.setText(simpleDateFormat.format(order.getStartTime()));
        viewHolder.amount.setText(String.valueOf(order.getTotalAmount()) + "元");
        viewHolder.btnAccept.setOnClickListener((View view) -> {
            try {
                JsonObject jsonOut = new JsonObject();
                int position = viewHolder.getAdapterPosition();
                jsonOut.addProperty("action", "takeSingleOrder");
                jsonOut.addProperty("driverID", driver.getDriverID());
                jsonOut.addProperty("orderID", orders.get(position).getOrderID());
                new CommonTask().execute(Constants.URL + "/singleOrderApi", jsonOut.toString()).get();
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
