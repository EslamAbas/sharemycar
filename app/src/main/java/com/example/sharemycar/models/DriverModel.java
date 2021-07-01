package com.example.sharemycar.models;

public class DriverModel {

    private String Email;
    private String First_Name;
    private String Last_Name;
    private String Phone;
    private String Car_Type;

    public DriverModel(String email, String first_Name, String last_Name, String phone, String car_Type) {
        Email = email;
        First_Name = first_Name;
        Last_Name = last_Name;
        Phone = phone;
        Car_Type = car_Type;
    }

    public String getEmail() {
        return Email;
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public String getLast_Name() {
        return Last_Name;
    }

    public String getPhone() {
        return Phone;
    }

    public String getCar_Type() {
        return Car_Type;
    }
}