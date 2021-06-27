package com.example.sharemycar.models;

public class DriverModel {

    private String Email;
    private String First_Name;
    private String Last_Name;
    private String Phone;
    private String Car_Type;
    private String Car_Color;
    private String Car_Lic1;
    private String Car_Lic2;
    private String Driver_Lic1;
    private String Driver_Lic2;
    private String Drugs_Test;

    public DriverModel(String email, String first_Name, String last_Name, String phone, String car_Type, String car_Color, String car_Lic1, String car_Lic2, String driver_Lic1, String driver_Lic2, String drugs_Test) {
        Email = email;
        First_Name = first_Name;
        Last_Name = last_Name;
        Phone = phone;
        Car_Type = car_Type;
        Car_Color = car_Color;
        Car_Lic1 = car_Lic1;
        Car_Lic2 = car_Lic2;
        Driver_Lic1 = driver_Lic1;
        Driver_Lic2 = driver_Lic2;
        Drugs_Test = drugs_Test;
    }

    public DriverModel() {
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

    public String getCar_Color() {
        return Car_Color;
    }

    public String getCar_Lic1() {
        return Car_Lic1;
    }

    public String getCar_Lic2() {
        return Car_Lic2;
    }

    public String getDriver_Lic1() {
        return Driver_Lic1;
    }

    public String getDriver_Lic2() {
        return Driver_Lic2;
    }

    public String getDrugs_Test() {
        return Drugs_Test;
    }
}
