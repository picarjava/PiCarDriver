package com.example.piCarDriver.orderAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.piCarDriver.Driver;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.GroupOrder;
import com.example.piCarDriver.model.LongTermOrder;
import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.model.SingleOrder;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "OrderAdapter";
    private List<OrderAdapterType> orders;
    private Driver driver;
    private ExecuteSchedule activity;

    public interface ExecuteSchedule {
        void executeSchedule(OrderAdapterType orderAdapterType);
    }

    public OrderAdapter(List<OrderAdapterType> orders, Driver driver) {
        this.orders = orders;
        this.driver = driver;
    }

    public OrderAdapter(List<OrderAdapterType> orders, Driver driver, ExecuteSchedule activity) {
        this.orders = orders;
        this.driver = driver;
        this.activity = activity;
    }

    class OrderHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        TextView startLoc;
        TextView endLoc;
        TextView startTime;
        TextView endTime;
        TextView time;
        TextView people;
        TextView amount;
        Button btnAccept;

        private OrderHolder(@NonNull View view) {
            super(view);
            container = view.findViewById(R.id.container);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
            endTime = view.findViewById(R.id.endTime);
            time = view.findViewById(R.id.time);
            people = view.findViewById(R.id.people);
            amount = view.findViewById(R.id.amount);
            btnAccept = view.findViewById(R.id.acceptOrder);
        }
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                                  .inflate(R.layout.view_order, viewGroup, false);
        return new OrderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        OrderHolder orderHolder = (OrderHolder) viewHolder;
        DateFormat dateFormat = new SimpleDateFormat("MM/dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        int viewType = orders.get(position).getViewType();
        Order order = orders.get(position).getOrder();
        switch (viewType) {
            case OrderAdapterType.SINGLE_ORDER:
                orderHolder.endTime.setVisibility(View.GONE);
                orderHolder.people.setVisibility(View.GONE);
                break;
            case OrderAdapterType.LONG_TERM_ORDER:
                orderHolder.endTime.setText(dateFormat.format(order.getEndTime()));
                orderHolder.people.setVisibility(View.GONE);
                break;
            case OrderAdapterType.GROUP_ORDER:
                orderHolder.endTime.setVisibility(View.GONE);
                orderHolder.people.setText(String.valueOf(((GroupOrder) order).getPeople()));
                break;
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                orderHolder.people.setText(String.valueOf(((GroupOrder) order).getPeople()) + "人");
                orderHolder.endTime.setText(dateFormat.format(order.getEndTime()));
                break;
        }
        orderHolder.startLoc.setText(order.getStartLoc());
        orderHolder.endLoc.setText(order.getEndLoc());
        orderHolder.startTime.setText(dateFormat.format(order.getStartTime()));
        Log.d(TAG, order.getStartTime().toString());
        orderHolder.time.setText(timeFormat.format(order.getStartTime()));
        if (activity == null) {
            orderHolder.amount.setText(String.valueOf(order.getTotalAmount()) + "元");
            orderHolder.btnAccept.setOnClickListener((View view) -> {
                try {
                    view.getContext();
                    int positionNow = orderHolder.getAdapterPosition();
                    String url = null;
                    String action = null;
                    JsonObject jsonOut = new JsonObject();
                    Order orderNow = orders.get(positionNow).getOrder();
                    switch (viewType) {
                        case OrderAdapterType.SINGLE_ORDER:
                            url = "/singleOrderApi";
                            action = "takeSingleOrder";
                            jsonOut.addProperty("orderID", ((SingleOrder) orderNow).getOrderID());
                            break;
                        case OrderAdapterType.LONG_TERM_ORDER:
                            url = "/singleOrderApi";
                            action = "takeLongTermOrder";
                            jsonOut.addProperty("orderID", new Gson().toJson(((LongTermOrder) orderNow).getOrderIDs()));
                            break;
                        case OrderAdapterType.GROUP_ORDER:
                        case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                            action = "takeGroupOrder";
                            url = "/groupOrderApi";
                            jsonOut.addProperty("groupID", ((GroupOrder) orderNow).getGroupID());
                            break;
                    }

                    jsonOut.addProperty("action", action);
                    jsonOut.addProperty("driverID", driver.getDriverID());
                    new CommonTask().execute(url, jsonOut.toString()).get();
                    Log.d(TAG, jsonOut.toString());
                    orders.remove(positionNow);
                    notifyItemRemoved(positionNow);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else {
            orderHolder.amount.setVisibility(View.GONE);
            orderHolder.btnAccept.setVisibility(View.GONE);
            orderHolder.container.setOnClickListener(v -> showExecuteDialog(v.getContext(), orders.get(orderHolder.getAdapterPosition())));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return orders.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void showExecuteDialog(Context context, OrderAdapterType orderAdapterType) {
        Order order = orderAdapterType.getOrder();
        String stringBuilder = "執行從 " +
                               order.getStartLoc() +
                               " 到 " +
                               order.getEndLoc() +
                               "\n開始時間: " +
                               new SimpleDateFormat("MM-dd HH:mm");
        new AlertDialog.Builder(context)
                .setTitle("執行訂單")
                .setMessage(stringBuilder)
                .setNegativeButton("不", (d, i)-> d.cancel())
                .setPositiveButton("好喔", (d, i)->{
                    activity.executeSchedule(orderAdapterType);
                    d.dismiss();
                }).show();
    }
}
