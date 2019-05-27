package com.theguardians.citywalker.Model;

public class UserContact {
    // Variables
    private int id;
    private String name;

    private String phoneNumber;
    // Constructor
    public UserContact(){

    }

    public UserContact(String name, String phone_number) {

        this.name = name;
        this.phoneNumber = phone_number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phone_number) {
        this.phoneNumber = phone_number;
    }
}
