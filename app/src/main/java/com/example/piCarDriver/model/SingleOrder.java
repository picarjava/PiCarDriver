package com.example.piCarDriver.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

public class SingleOrder extends Order implements Parcelable {
    private String orderID;
    private Integer orderType;
    private Integer rate;
    private Timestamp launchTime;

    public SingleOrder() {}

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Timestamp getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(Timestamp launchTime) {
        this.launchTime = launchTime;
    }


    @Override
    public String toString() {
        return "SingleOrder{" +
                "orderID='" + orderID + '\'' +
                ", orderType=" + orderType +
                ", rate=" + rate +
                ", note='" + note + '\'' +
                ", launchTime=" + launchTime +
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orderID);
        dest.writeValue(this.orderType);
        dest.writeValue(this.rate);
        dest.writeString(this.note);
        dest.writeSerializable(this.launchTime);
        dest.writeString(this.driverID);
        dest.writeString(this.memId);
        dest.writeValue(this.state);
        dest.writeValue(this.totalAmount);
        dest.writeString(this.startLoc);
        dest.writeString(this.endLoc);
        dest.writeSerializable(this.startTime);
        dest.writeSerializable(this.endTime);
        dest.writeValue(this.startLng);
        dest.writeValue(this.startLat);
        dest.writeValue(this.endLng);
        dest.writeValue(this.endLat);
        dest.writeString(this.note);
    }

    protected SingleOrder(Parcel in) {
        this.orderID = in.readString();
        this.orderType = (Integer) in.readValue(Integer.class.getClassLoader());
        this.rate = (Integer) in.readValue(Integer.class.getClassLoader());
        this.note = in.readString();
        this.launchTime = (Timestamp) in.readSerializable();
        this.driverID = in.readString();
        this.memId = in.readString();
        this.state = (Integer) in.readValue(Integer.class.getClassLoader());
        this.totalAmount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.startLoc = in.readString();
        this.endLoc = in.readString();
        this.startTime = (Timestamp) in.readSerializable();
        this.endTime = (Timestamp) in.readSerializable();
        this.startLng = (Double) in.readValue(Double.class.getClassLoader());
        this.startLat = (Double) in.readValue(Double.class.getClassLoader());
        this.endLng = (Double) in.readValue(Double.class.getClassLoader());
        this.endLat = (Double) in.readValue(Double.class.getClassLoader());
        this.note = in.readString();
    }

    public static final Creator<SingleOrder> CREATOR = new Creator<SingleOrder>() {
        @Override
        public SingleOrder createFromParcel(Parcel source) {
            return new SingleOrder(source);
        }

        @Override
        public SingleOrder[] newArray(int size) {
            return new SingleOrder[size];
        }
    };
}