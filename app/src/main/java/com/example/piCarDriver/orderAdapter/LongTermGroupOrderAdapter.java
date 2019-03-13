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
import com.example.piCarDriver.Constants;
import com.example.piCarDriver.model.GroupOrder;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LongTermGroupOrderAdapter extends RecyclerView.Adapter<LongTermGroupOrderAdapter.ViewHolder> {
private List<List<List<GroupOrder>>> ordersList;
private Driver driver;

public LongTermGroupOrderAdapter(List<List<List<GroupOrder>>> orders, Driver driver) {
        this.ordersList = orders;
        this.driver = driver;
        }

class ViewHolder extends RecyclerView.ViewHolder {
    TextView startLoc;
    TextView endLoc;
    TextView startTime;
    TextView endTime;
    TextView people;
    TextView amount;
    Button btnAccept;

    private ViewHolder(@NonNull View view) {
        super(view);
        startLoc = view.findViewById(R.id.startLoc);
        endLoc = view.findViewById(R.id.endLoc);
        startTime = view.findViewById(R.id.startTime);
        endTime = view.findViewById(R.id.endTime);
        people = view.findViewById(R.id.people);
        amount = view.findViewById(R.id.amount);
        btnAccept = view.findViewById(R.id.acceptOrder);
    }
}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_group_order, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        List<List<GroupOrder>> orders = ordersList.get(viewHolder.getAdapterPosition());
        List<GroupOrder> order = orders.get(0);
        viewHolder.startLoc.setText(order.get(0).getStartLoc());
        viewHolder.endLoc.setText(order.get(0).getEndLoc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        viewHolder.startTime.setText(simpleDateFormat.format(order.get(0).getStartTime()));
        viewHolder.endTime.setText(simpleDateFormat.format(orders.get(orders.size() - 1).get(0).getStartTime()));
        int amount = 0;
        int people = 0;
        for (GroupOrder o: order) {
            amount += o.getTotalAmount();
            people++;
        }

        viewHolder.people.setText(String.valueOf(people) + "人");
        viewHolder.amount.setText(String.valueOf(amount) + "元");
        viewHolder.btnAccept.setOnClickListener((View view) -> {
            try {
                JsonObject jsonOut = new JsonObject();
                int position = viewHolder.getAdapterPosition();
                jsonOut.addProperty("action", "takeSingleOrder");
                jsonOut.addProperty("driverID", driver.getDriverID());
                List<String> orderIDs = new ArrayList<>();
                for (GroupOrder o: order)
                    orderIDs.add(o.getgOrderID());

                jsonOut.addProperty("orderID", new Gson().toJson(orderIDs));
                new CommonTask().execute(Constants.URL + "/singleOrderApi", jsonOut.toString()).get();
                ordersList.remove(position);
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
        return ordersList.size();
    }
}
