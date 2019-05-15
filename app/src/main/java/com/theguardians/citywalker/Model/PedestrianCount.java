package com.theguardians.citywalker.Model;


public class PedestrianCount {


    private String sensor_id;
    private String real_time;
    private String total_of_directions;
    private String predict_time;
    private String hourly_counts;
    private String busyness;


    public PedestrianCount() {
        this.sensor_id = sensor_id;
        this.real_time = real_time;
        this.total_of_directions = total_of_directions;
        this.predict_time = predict_time;
        this.hourly_counts = hourly_counts;
        this.busyness = busyness;
    }

    public String getBusyness() {
        return busyness;
    }

    public void setBusyness(String busyness) {
        this.busyness = busyness;
    }

    public String getPrediction_time() {
        return predict_time;
    }

    public void setPrediction_time(String prediction_time) {
        this.predict_time = prediction_time;
    }

    public String getPrediction_counts() {
        return hourly_counts;
    }

    public void setPrediction_counts(String prediction_counts) {
        this.hourly_counts = prediction_counts;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }

    public String getTime() {
        return real_time;
    }

    public void setTime(String time) {
        this.real_time = time;
    }

    public String getTotal_of_directions() {
        return total_of_directions;
    }

    public void setTotal_of_directions(String total_of_directions) {
        this.total_of_directions = total_of_directions;
    }
}
