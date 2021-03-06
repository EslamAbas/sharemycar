package com.example.sharemycar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.CUSTOMER_OR_DRIVER;
import static com.example.sharemycar.Constants.CUSTOMER_REQUEST;
import static com.example.sharemycar.Constants.CUSTOMER_RIDE_ID;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.DRIVER_AVAILABLE;
import static com.example.sharemycar.Constants.DRIVER_WORKING;
import static com.example.sharemycar.Constants.REQUEST_LOCATION;
import static com.example.sharemycar.Constants.USER;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private Button logout, mSettings, mHistory, mRideStatus;
    private Switch mWorkingSwitch;
    private int status = 0;


    private String customerId = "", destination;
    private LatLng destinationLatLng;
    private float rideDistance;

    private Boolean isLoggingOut = false;
    private LinearLayout mUserInfo;
    private TextView mUserName, mUserPhone, mCustomerDestination;
    public LatLng pickupLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        polylines = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,

                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            mapFragment.getMapAsync(this);
        }

        mUserInfo = findViewById(R.id.userInfo);
        mUserName = findViewById(R.id.customerName);
        mUserPhone = findViewById(R.id.customerPhone);
        mCustomerDestination = findViewById(R.id.customerDestination);

        mWorkingSwitch = findViewById(R.id.workingSwitch);

        mWorkingSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                connectDriver();

            } else {
                disconnectDriver();
            }

        });

        mSettings = (Button) findViewById(R.id.settings);
        logout = (Button) findViewById(R.id.logout);
        mHistory = (Button) findViewById(R.id.history_d);
        mRideStatus = (Button) findViewById(R.id.rideStatus);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status) {
                    case 1:
                        status = 2;
                        erasePolylines();
                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                            getRouteMarker(destinationLatLng);
                        }
                        mRideStatus.setText("Drive Completed");

                        break;
                    case 2:
                        recordRide();
                        endRide();

                        break;

                }
            }
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(DriverMapActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            return;

        });
        mSettings.setOnClickListener(view -> {
            Intent i = new Intent(DriverMapActivity.this, DriverSettingsActivity.class);
            startActivity(i);
            return;

        });
        mHistory.setOnClickListener(view -> {
            Intent i = new Intent(DriverMapActivity.this, HistoryActivity.class);
            i.putExtra(CUSTOMER_OR_DRIVER, DRIVER);
            startActivity(i);
            return;

        });

        getAssignedCustomer();


    }

    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(driverId).child(CUSTOMER_REQUEST).child(CUSTOMER_RIDE_ID);

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    status = 1;
                    String customerId = snapshot.getValue().toString();
                    getUserId(customerId);
                    getAssignedCustomerPickupLocation(customerId);
                    getAssignedCustomerDestination(customerId);
                    getAssignedCustomerPickupInfo(customerId);


                } else {
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUserId(String customerId) {
        this.customerId = customerId;
    }

    Marker pickupMarker;
    DatabaseReference assignedCustomerPickupLocationRef;
    ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation(String customerId) {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child(CUSTOMER_REQUEST).child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLog = 0;

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLog = Double.parseDouble(map.get(1).toString());
                    }
                     pickupLatLng = new LatLng(locationLat, locationLog);

                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title(String.valueOf(R.string.pickup_location)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup)));
                    getRouteMarker(pickupLatLng);

                }
//                mUserInfo.setVisibility(View.GONE);
                mUserName.setText("");
                mUserPhone.setText("");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getRouteMarker(LatLng pickupLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                .build();
        routing.execute();
    }

    private void getAssignedCustomerDestination(String customerId) {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(driverId).child(CUSTOMER_REQUEST);

        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                    if (map.get("destination") != null) {
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText("Destination: " + destination);

                    } else {
                        mCustomerDestination.setText("Destination: --)");
                    }
                    double destinationLat = 0.0;
                    double destinationLng = 0.0;
                    if (map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng") != null) {
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAssignedCustomerPickupInfo(String customerId) {
        mUserInfo.setVisibility(View.VISIBLE);
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(USER).child(customerId);
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("first_Name") != null && map.get("last_name") != null) {

                        mUserName.setText(map.get("first_Name").toString() + " " + map.get("last_name").toString());
                    }
                    if (map.get("phone_Number") != null) {

                        mUserPhone.setText(map.get("phone_Number").toString());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void endRide() {
        mRideStatus.setText("picked customer");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(userId).child(CUSTOMER_REQUEST);
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CUSTOMER_REQUEST);
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId = "";
        rideDistance = 0;

        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        erasePolylines();
        customerId = "";
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null) {
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mUserInfo.setVisibility(View.GONE);
        mUserName.setText("");
        mUserPhone.setText("");
        mCustomerDestination.setText("Destination:--");

    }

    private void recordRide() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(USER).child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        driverRef.removeValue();
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.longitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);

    }


    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis() / 1000;
        return timestamp;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }

    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {


                    if (!customerId.equals("") && mLastLocation != null && location != null) {
                        rideDistance += mLastLocation.distanceTo(location) / 1000;
                    }
                    mLastLocation = location;


                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference(DRIVER_AVAILABLE);
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference(DRIVER_WORKING);
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (customerId) {
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }
            }
        }
    };

    private void connectDriver() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

    }


    private void disconnectDriver() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DRIVER_AVAILABLE);

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission();
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Log.println(Log.ERROR,"Error: ",e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

}