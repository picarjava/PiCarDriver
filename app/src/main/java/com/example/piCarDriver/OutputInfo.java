package com.example.piCarDriver;

public class OutputInfo {
    private String driverID;
    private LatLng latLng;
    private boolean isExecuting;

    public OutputInfo() {}

    public OutputInfo(String driverID) {
        this.driverID = driverID;
    }

    public OutputInfo(String driverID, LatLng latLng, boolean isExecuting) {
        this(driverID);
        this.latLng = latLng;
        this.isExecuting = isExecuting;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public void setExecuting(boolean executing) {
        isExecuting = executing;
    }

    public static class LatLng {
        private double latitude;
        private double longitude;

        public LatLng() {}

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
