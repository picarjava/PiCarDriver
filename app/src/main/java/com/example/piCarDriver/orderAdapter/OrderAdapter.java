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
import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


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

        public GroupOrderHolder(@NonNull View view) {
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
        Order order = null;
        int amount = 0;
        int people;
        switch (viewType) {
            case OrderAdapterType.SINGLE_ORDER:
                order = (Order) orders.get(position).getOrder();
                amount = order.getTotalAmount();
                break;
            case OrderAdapterType.LONG_TERM_ORDER:
            case OrderAdapterType.GROUP_ORDER:
                @SuppressWarnings("unchecked")
                List<Order> lOrders = (List<Order>) orders.get(position).getOrder();
                order = lOrders.get(0);
                amount = lOrders.stream()
                                .mapToInt(Order::getTotalAmount)
                                .reduce((acc, totalAmount) -> acc + totalAmount)
                                .orElse(0);
                ((LongTermOrderHolder) orderHolder).endTime.setText(simpleDateFormat.format(lOrders.get(lOrders.size() - 1).getStartTime()));
                break;
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                @SuppressWarnings("unchecked")
                List<List<Order>> lgOrders = (List<List<Order>>) orders.get(position).getOrder();
                order = lgOrders.get(0).get(0);
                amount = lgOrders.stream()
                                 .flatMap(Collection::stream)
                                 .mapToInt(Order::getTotalAmount)
                                 .reduce((acc, c) -> acc + c)
                                 .orElse(0);
                people = lgOrders.get(0).size();
                ((LongTermGroupOrderHolder) orderHolder).endTime.setText(simpleDateFormat.format(lgOrders.get(lgOrders.size() - 1).get(0).getStartTime()));
                ((LongTermGroupOrderHolder) orderHolder).people.setText(String.valueOf(people));
                break;
        }

        assert order != null;
        orderHolder.startLoc.setText(order.getStartLoc());
        orderHolder.endLoc.setText(order.getEndLoc());

        orderHolder.startTime.setText(simpleDateFormat.format(order.getStartTime()));
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
                        List<Order> lOrders = (List<Order>) orders.get(positionNow).getOrder();
                        action = "takeLongTermOrder";
                        List<String> orderIDs = lOrders.stream()
                                                       .map(Order::getOrderID)
                                                       .collect(Collectors.toList());
                        orderID = new Gson().toJson(orderIDs);
                        break;
                    case OrderAdapterType.GROUP_ORDER:
                        @SuppressWarnings("unchecked")
                        List<Order> gOrders = (List<Order>) orders.get(positionNow).getOrder();
                        action = "takeGroupOrder";
                        orderIDs = gOrders.stream()
                                          .map(Order::getOrderID)
                                          .collect(Collectors.toList());
                        orderID = new Gson().toJson(orderIDs);
                        break;
                    case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                        @SuppressWarnings("unchecked")
                        List<List<Order>> lgOrders = (List<List<Order>>) orders.get(positionNow).getOrder();
                        action = "takeLongTermGroupOrder";
                        orderIDs = lgOrders.stream()
                                           .flatMap(List::stream)
                                           .map(Order::getOrderID)
                                           .collect(Collectors.toList());
                        orderID = new Gson().toJson(orderIDs);

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
