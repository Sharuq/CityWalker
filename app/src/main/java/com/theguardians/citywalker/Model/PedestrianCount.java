package com.theguardians.citywalker.Model;


public class PedestrianCount {


    private String sensor_id;
    private String time;
    private String total_of_directions;

    public PedestrianCount() {
        this.sensor_id = sensor_id;
        this.time = time;
        this.total_of_directions = total_of_directions;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTotal_of_directions() {
        return total_of_directions;
    }

    public void setTotal_of_directions(String total_of_directions) {
        this.total_of_directions = total_of_directions;
    }
}
