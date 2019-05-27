package com.theguardians.citywalker.Model;

public class PedestrianSensor {
    private String sensor_id;
    private String sensor_description;
    private  double latitude;
    private double longitude;

    public PedestrianSensor() {
        this.sensor_id = sensor_id;
        this.sensor_description = sensor_description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }

    public String getSensor_description() {
        return sensor_description;
    }

    public void setSensor_description(String sensor_description) {
        this.sensor_description = sensor_description;
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
