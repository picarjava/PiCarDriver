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
        TextView time;
        TextView amount;
        Button btnAccept;

        private OrderHolder(@NonNull View view) {
            super(view);
            container = view.findViewById(R.id.container);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
            time = view.findViewById(R.id.time);
            amount = view.findViewById(R.id.amount);
            btnAccept = view.findViewById(R.id.acceptOrder);
        }
    }

    class LongTermOrderHolder extends OrderHolder {
        TextView endTime;

        private LongTermOrderHolder(@NonNull View view) {
            super(view);
            endTime = view.findViewById(R.id.endTime);
        }
    }

    class GroupOrderHolder extends OrderHolder {
        TextView people;

        private GroupOrderHolder(@NonNull View view) {
            super(view);
            this.people = view.findViewById(R.id.people);
        }
    }

    class LongTermGroupOrderHolder extends GroupOrderHolder {
        TextView endTime;

        private LongTermGroupOrderHolder(@NonNull View view) {
            super(view);
            this.endTime = view.findViewById(R.id.endTime);
        }
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case OrderAdapterType.SINGLE_ORDER:
                view = LayoutInflater.from(viewGroup.getContext())
                                     .inflate(R.layout.view_single_order, viewGroup, false);
            return new OrderHolder(view);
            case OrderAdapterType.LONG_TERM_ORDER:
                view = LayoutInflater.from(viewGroup.getContext())
                                     .inflate(R.layout.view_longterm_order, viewGroup, false);
                return new LongTermOrderHolder(view);
            case OrderAdapterType.GROUP_ORDER:
                view = LayoutInflater.from(viewGroup.getContext())
                                     .inflate(R.layout.view_group_order, viewGroup, false);
                return new GroupOrderHolder(view);
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                Log.d(TAG, "lgOrder");
                view = LayoutInflater.from(viewGroup.getContext())
                                     .inflate(R.layout.view_longterm_group_order, viewGroup, false);
                return new LongTermGroupOrderHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        OrderHolder orderHolder = (OrderHolder) viewHolder;
        DateFormat dateFormat = new SimpleDateFormat("MM/dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        int viewType = orders.get(position).getViewType();
        Order order = orders.get(position).getOrder();
        switch (viewType) {
            case OrderAdapterType.LONG_TERM_ORDER:
                ((LongTermOrderHolder) orderHolder).endTime.setText(dateFormat.format(order.getEndTime()));
                break;
            case OrderAdapterType.GROUP_ORDER:
                ((GroupOrderHolder) orderHolder).people.setText(String.valueOf(((GroupOrder) order).getPeople()));
                break;
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                ((LongTermGroupOrderHolder) orderHolder).people.setText(String.valueOf(((GroupOrder) order).getPeople()) + "人");
                ((LongTermGroupOrderHolder) orderHolder).endTime.setText(dateFormat.format(order.getEndTime()));
                break;
        }
        orderHolder.startLoc.setText(order.getStartLoc());
        orderHolder.endLoc.setText(order.getEndLoc());
        orderHolder.startTime.setText(dateFormat.format(order.getStartTime()));
        Log.d(TAG, order.getStartTime().toString());
        orderHolder.time.setText(timeFormat.format(order.getStartTime()));
        orderHolder.amount.setText(String.valueOf(order.getTotalAmount()) + "元");
        if (activity == null) {
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
            orderHolder.btnAccept.setVisibility(View.GONE);
            orderHolder.container.setOnClickListener(v -> showDownloadDialog(v.getContext(), orders.get(orderHolder.getAdapterPosition())));
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

    private void showDownloadDialog(Context context, OrderAdapterType orderAdapterType) {
        new AlertDialog.Builder(context)
                .setTitle("找不到掃描器")
                .setMessage("請至Google play商店下載")
                .setPositiveButton("Yes", (d, i)->{
                    activity.executeSchedule(orderAdapterType);
                    d.dismiss();
                }).setNegativeButton("no", (d, i)-> d.cancel())
                .show();
    }
}
