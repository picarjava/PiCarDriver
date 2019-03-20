package com.example.piCarDriver.model;

public class OrderAdapterType {
    public final static int SINGLE_ORDER = 0;
    public final static int LONG_TERM_ORDER = 1;
    public final static int GROUP_ORDER = 2;
    public final static int LONG_TERM_GROUP_ORDER = 3;
    private Order order;
    private int viewType;

    public OrderAdapterType(Order order, int viewType) {
        this.order = order;
        this.viewType = viewType;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
