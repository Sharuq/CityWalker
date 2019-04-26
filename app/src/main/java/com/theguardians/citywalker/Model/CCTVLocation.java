package com.theguardians.citywalker.Model;

public class CCTVLocation {
    private String cctvNo;
    private String detail;
    private  double latitude;
    private double longitude;

    public CCTVLocation() {
        this.cctvNo = cctvNo;
        this.detail = detail;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCctvNo() {
        return cctvNo;
    }

    public void setCctvNo(String cctvNo) {
        this.cctvNo = cctvNo;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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
