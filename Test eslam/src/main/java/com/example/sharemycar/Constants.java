package com.example.sharemycar;

import com.google.firebase.auth.FirebaseAuth;

public class Constants {
    public static final String ACCOUNTS = "Accounts";
    public static final String USER = "Users";
    public static final String DRIVER = "Drivers";
    public static final String DRIVER_AVAILABLE = "DriversAvailable";
    public static final String USER_REQUEST = "CustomerRequest";
    public static final String CUSTOMER = "Customer";
    public static final String CUSTOMER_RIDE_ID= "CustomerRideId";
    public static final String DRIVER_WORKING= "DriverWorking";

    public static final String GET_UID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


}
