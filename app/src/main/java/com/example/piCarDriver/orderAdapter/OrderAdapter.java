package com.example.piCarDriver.orderAdapter;

import android.support.annotation.NonNull;
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
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "OrderAdapter";
    private List<OrderAdapterType> orders;
    private Driver driver;

    public OrderAdapter(List<OrderAdapterType> orders, Driver driver) {
        this.orders = orders;
        this.driver = driver;
    }

    class OrderHolder extends RecyclerView.ViewHolder {
        TextView startLoc;
        TextView endLoc;
        TextView startTime;
        TextView amount;
        Button btnAccept;

        private OrderHolder(@NonNull View view) {
            super(view);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
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

        public LongTermGroupOrderHolder(@NonNull View view) {
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
                view = LayoutInflater.from(viewGroup.getContext())
                                     .inflate(R.layout.view_longterm_group_order, viewGroup, false);
                return new LongTermOrderHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        OrderHolder orderHolder = (OrderHolder) viewHolder;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        int viewType = orders.get(position).getViewType();
        int amount = 0;
        String startLoc = null;
        String endLoc = null;
        Timestamp startTime = null;
        switch (viewType) {
            case OrderAdapterType.SINGLE_ORDER:
                Order order = (Order) orders.get(position).getOrder();
                startLoc = order.getStartLoc();
                endLoc = order.getEndLoc();
                startTime = order.getStartTime();
                amount = order.getTotalAmount();
                break;
            case OrderAdapterType.LONG_TERM_ORDER:
                @SuppressWarnings("unchecked")
                LongTermOrder lOrder = (LongTermOrder) orders.get(position).getOrder();
                startLoc = lOrder.getStartLoc();
                endLoc = lOrder.getEndLoc();
                startTime = lOrder.getStartTime();
                amount = lOrder.getTotalAmount();
                ((LongTermOrderHolder) orderHolder).endTime.setText(simpleDateFormat.format(lOrder.getEndTime()));
                break;
            case OrderAdapterType.GROUP_ORDER:
                @SuppressWarnings("unchecked")
                GroupOrder gOrder = (GroupOrder) orders.get(position).getOrder();
                startLoc = gOrder.getStartLoc();
                endLoc = gOrder.getEndLoc();
                startTime = gOrder.getStartTime();
                amount = gOrder.getTotalAmount();
                ((GroupOrderHolder) orderHolder).people.setText(String.valueOf(gOrder.getPeople()));
                break;
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                @SuppressWarnings("unchecked")
                GroupOrder lgOrder = (GroupOrder) orders.get(position).getOrder();
                startLoc = lgOrder.getStartLoc();
                endLoc = lgOrder.getEndLoc();
                startTime =lgOrder.getStartTime();
                amount = lgOrder.getTotalAmount();
                ((LongTermGroupOrderHolder) orderHolder).people.setText(String.valueOf(lgOrder.getPeople()));
                ((LongTermGroupOrderHolder) orderHolder).endTime.setText(simpleDateFormat.format(lgOrder.getStartTime()));
                break;
        }
        orderHolder.startLoc.setText(startLoc);
        orderHolder.endLoc.setText(endLoc);
        orderHolder.startTime.setText(simpleDateFormat.format(startTime));
        orderHolder.amount.setText(String.valueOf(amount) + "元");
        orderHolder.btnAccept.setOnClickListener((View view) -> {
            try {
                int positionNow = orderHolder.getAdapterPosition();
                String action = null;
                String orderID = null;
                switch (viewType) {
                    case OrderAdapterType.SINGLE_ORDER:
                        action = "takeSingleOrder";
                        orderID = ((Order) orders.get(positionNow).getOrder()).getOrderID();
                        break;
                    case OrderAdapterType.LONG_TERM_ORDER:
                        @SuppressWarnings("unchecked")
                        LongTermOrder lOrders = (LongTermOrder) orders.get(positionNow).getOrder();
                        action = "takeLongTermOrder";
                        List<String> orderIDs = lOrders.getOrderIDs();
                        orderID = new Gson().toJson(orderIDs);
                        break;
                    case OrderAdapterType.GROUP_ORDER:
                        @SuppressWarnings("unchecked")
                        GroupOrder gOrders = (GroupOrder) orders.get(positionNow).getOrder();
                        action = "takeGroupOrder";
                        orderID = gOrders.getGroupID();
                        break;
                    case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                        @SuppressWarnings("unchecked")
                        GroupOrder lgOrders = (GroupOrder) orders.get(positionNow).getOrder();
                        action = "takeLongTermGroupOrder";
                        orderID = lgOrders.getGroupID();
                        orderID = new Gson().toJson(orderID);
                        break;
                }
                JsonObject jsonOut = new JsonObject();
                jsonOut.addProperty("action", action);
                jsonOut.addProperty("driverID", driver.getDriverID());
                jsonOut.addProperty("orderID", orderID);
                new CommonTask().execute("/singleOrderApi", jsonOut.toString()).get();
                Log.d(TAG, jsonOut.toString());
                orders.remove(positionNow);
                notifyItemRemoved(positionNow);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return orders.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
