package com.example.piCarDriver.model;

public class OrderAdapterType {
    public final static int SINGLE_ORDER = 0;
    public final static int LONG_TERM_ORDER = 1;
    public final static int LONG_TERM_GROUP_ORDER = 3;
    private Object order;
    private int viewType;

    public OrderAdapterType(Object object, int viewType) {
        order = object;
        this.viewType = viewType;
    }

    public Object getOrder() {
        return order;
    }

    public void setOrder(Object order) {
        this.order = order;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
