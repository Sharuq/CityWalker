package com.theguardians.citywalker.Model;


public class PedestrianCount {


    private String sensor_id;
    private String time;
    private String total_of_directions;
    private String prediction_time;
    private String prediction_counts;
    private String busyness;


    public PedestrianCount(String sensor_id, String time, String total_of_directions, String prediction_time, String prediction_counts, String busyness) {
        this.sensor_id = sensor_id;
        this.time = time;
        this.total_of_directions = total_of_directions;
        this.prediction_time = prediction_time;
        this.prediction_counts = prediction_counts;
        this.busyness = busyness;
    }

    public String getBusyness() {
        return busyness;
    }

    public void setBusyness(String busyness) {
        this.busyness = busyness;
    }

    public String getPrediction_time() {
        return prediction_time;
    }

    public void setPrediction_time(String prediction_time) {
        this.prediction_time = prediction_time;
    }

    public String getPrediction_counts() {
        return prediction_counts;
    }

    public void setPrediction_counts(String prediction_counts) {
        this.prediction_counts = prediction_counts;
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
