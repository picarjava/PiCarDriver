package com.example.piCarDriver.model;

import java.sql.Timestamp;

public class GroupOrder {
    private String gOrderID;
    private String driverID;
    private String memID;
    private Integer state;
    private Integer totalAmount;
    private Timestamp launchTime;
    private Timestamp startTime;
    private Timestamp endTime;
    private String startLoc;
    private String endLoc;
    private Double startLng;
    private Double startLat;
    private Double endLng;
    private Double endLat;
    private Integer orderType;
    private Integer rate;
    private String note;
    private String groupID;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupId) {
        this.groupID = groupId;
    }

    public String getgOrderID() {
        return gOrderID;
    }

    public void setgOrderID(String gOrderId) {
        this.gOrderID = gOrderId;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverId) {
        this.driverID = driverId;
    }

    public String getMemID() {
        return memID;
    }

    public void setMemID(String memId) {
        this.memID = memId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(Timestamp launchTime) {
        this.launchTime = launchTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStartLoc() {
        return startLoc;
    }

    public void setStartLoc(String startLoc) {
        this.startLoc = startLoc;
    }

    public String getEndLoc() {
        return endLoc;
    }

    public void setEndLoc(String endLoc) {
        this.endLoc = endLoc;
    }

    public Double getStartLng() {
        return startLng;
    }

    public void setStartLng(Double startLng) {
        this.startLng = startLng;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getEndLng() {
        return endLng;
    }

    public void setEndLng(Double endLng) {
        this.endLng = endLng;
    }

    public Double getEndLat() {
        return endLat;
    }

    public void setEndLat(Double endLat) {
        this.endLat = endLat;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
