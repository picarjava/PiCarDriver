package com.example.piCarDriver;

import java.sql.Date;

public class Driver {
    private String memID;
    private String driverID;
    private String plateNum;
    private Integer verified;
    private Integer banned;
    private Date    deadline;
    private Integer onlineCar;
    private Integer score;
    private String  carType;
    private Integer sharedCar;
    private Integer pet;
    private Integer smoke;
    private Integer babySeat;

    public Driver() {

    }

    public String getMemID() {
        return memID;
    }

    public void setMemID(String memID) {
        this.memID = memID;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public String getPlateNum() {
        return plateNum;
    }

    public void setPlateNum(String plateNum) {
        this.plateNum = plateNum;
    }

    public Integer getVerified() {
        return verified;
    }

    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    public Integer getBanned() {
        return banned;
    }

    public void setBanned(Integer banned) {
        this.banned = banned;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Integer getOnlineCar() {
        return onlineCar;
    }

    public void setOnlineCar(Integer onlineCar) {
        this.onlineCar = onlineCar;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public Integer getSharedCar() {
        return sharedCar;
    }

    public void setSharedCar(Integer sharedCar) {
        this.sharedCar = sharedCar;
    }

    public Integer getPet() {
        return pet;
    }

    public void setPet(Integer pet) {
        this.pet = pet;
    }

    public Integer getSmoke() {
        return smoke;
    }

    public void setSmoke(Integer smoke) {
        this.smoke = smoke;
    }

    public Integer getBabySeat() {
        return babySeat;
    }

    public void setBabySeat(Integer babySeat) {
        this.babySeat = babySeat;
    }
}
