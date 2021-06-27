package com.example.sharemycar.models;

public class UserModel {
    private String First_Name,Last_name,Email,Phone_Number;

    public UserModel(String first_Name, String last_name, String email, String phone_Number) {
        First_Name = first_Name;
        Last_name = last_name;
        Email = email;
        Phone_Number = phone_Number;
    }

    public UserModel() {
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public String getLast_name() {
        return Last_name;
    }

    public String getEmail() {
        return Email;
    }

    public String getPhone_Number() {
        return Phone_Number;
    }
}
