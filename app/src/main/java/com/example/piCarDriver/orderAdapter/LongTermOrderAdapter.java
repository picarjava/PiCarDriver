package com.example.piCarDriver.orderAdapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.piCarDriver.R;
import com.example.piCarDriver.SingleOrder;

import java.text.SimpleDateFormat;
import java.util.List;

public class LongTermOrderAdapter extends RecyclerView.Adapter<LongTermOrderAdapter.ViewHolder> {
    private List<List<SingleOrder>> orders;

    public LongTermOrderAdapter(List<List<SingleOrder>> orders) {
        this.orders = orders;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLoc;
        TextView endLoc;
        TextView startTime;
        TextView endTime;
        Button btnAccept;

        private ViewHolder(@NonNull View view) {
            super(view);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
            endTime = view.findViewById(R.id.endTime);
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
        List<SingleOrder> ordersNow = orders.get(i);
        SingleOrder startOrder = ordersNow.get(0);
        SingleOrder endOrder = ordersNow.get(ordersNow.size() - 1);
        viewHolder.startLoc.setText(startOrder.getStartLoc());
        viewHolder.endLoc.setText(startOrder.getEndLoc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        viewHolder.startTime.setText(simpleDateFormat.format(startOrder.getStartTime()));
        viewHolder.endTime.setText(simpleDateFormat.format(endOrder.getStartTime()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
