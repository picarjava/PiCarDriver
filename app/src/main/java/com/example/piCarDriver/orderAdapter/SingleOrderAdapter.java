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


public class SingleOrderAdapter extends RecyclerView.Adapter<SingleOrderAdapter.ViewHolder> {
    private List<SingleOrder> orders;

    public SingleOrderAdapter(List<SingleOrder> orders) {
        this.orders = orders;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLoc;
        TextView endLoc;
        TextView startTime;
        Button btnAccept;

        private ViewHolder(@NonNull View view) {
            super(view);
            startLoc = view.findViewById(R.id.startLoc);
            endLoc = view.findViewById(R.id.endLoc);
            startTime = view.findViewById(R.id.startTime);
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
    public void onBindViewHolder(@NonNull SingleOrderAdapter.ViewHolder viewHolder, int i) {
        viewHolder.startLoc.setText(orders.get(i).getStartLoc());
        viewHolder.endLoc.setText(orders.get(i).getEndLoc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        viewHolder.startTime.setText(simpleDateFormat.format(orders.get(i).getStartTime()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
