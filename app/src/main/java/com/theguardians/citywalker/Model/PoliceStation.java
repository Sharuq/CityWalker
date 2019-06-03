package com.theguardians.citywalker.Model;

/**
 * This class is utilised as model for police station locations
 * @Author Sharuq
 * @Version 2.0
 */
import java.io.Serializable;

public class PoliceStation implements Serializable {

    private String police_station;
    private  double latitude;
    private double longitude;
    private String address;
    private String tel;


    public PoliceStation() {
        this.address = address;
        this.tel = tel;
        this.police_station = police_station;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPolice_station() {
        return police_station;
    }

    public void setPolice_station(String police_station) {
        this.police_station = police_station;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
