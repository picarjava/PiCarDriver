package com.example.piCarDriver.model;

import java.util.List;

public class LongTermOrder extends Order {
    private List<String> orderIDs;

    public List<String> getOrderIDs() {
        return orderIDs;
    }

    public void setOrderIDs(List<String> orderIDs) {
        this.orderIDs = orderIDs;
    }

    public String getMemID() {
        return memId;
    }

    public void setMemID(String memID) {
        this.memId = memID;
    }

    @Override
    public String toString() {
        return "LongTermOrder{" +
                "orderIDs=" + orderIDs +
                ", driverID='" + driverID + '\'' +
                ", memId='" + memId + '\'' +
                ", state=" + state +
                ", totalAmount=" + totalAmount +
                ", startLoc='" + startLoc + '\'' +
                ", endLoc='" + endLoc + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startLng=" + startLng +
                ", startLat=" + startLat +
                ", endLng=" + endLng +
                ", endLat=" + endLat +
                ", note='" + note + '\'' +
                ", driverID='" + driverID + '\'' +
                ", memId='" + memId + '\'' +
                ", state=" + state +
                ", totalAmount=" + totalAmount +
                ", startLoc='" + startLoc + '\'' +
                ", endLoc='" + endLoc + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startLng=" + startLng +
                ", startLat=" + startLat +
                ", endLng=" + endLng +
                ", endLat=" + endLat +
                ", note='" + note + '\'' +
                '}';
    }
}
