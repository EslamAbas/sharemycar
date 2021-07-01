package com.example.sharemycar.models;

public class DriverLicenseModel {
    private String Car_Lic1;
    private String Car_Lic2;
    private String Driver_Lic1;
    private String Driver_Lic2;
    private String Drugs_Test;

    public DriverLicenseModel(String car_Lic1, String car_Lic2, String driver_Lic1, String driver_Lic2, String drugs_Test) {
        Car_Lic1 = car_Lic1;
        Car_Lic2 = car_Lic2;
        Driver_Lic1 = driver_Lic1;
        Driver_Lic2 = driver_Lic2;
        Drugs_Test = drugs_Test;
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
