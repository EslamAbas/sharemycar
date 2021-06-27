package com.example.sharemycar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.CUSTOMER_REQUEST;
import static com.example.sharemycar.Constants.CUSTOMER_RIDE_ID;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.DRIVER_AVAILABLE;
import static com.example.sharemycar.Constants.DRIVER_WORKING;
import static com.example.sharemycar.Constants.REQUEST_LOCATION;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button logout, mSettings,mHistory,mRideStatus;
    private Switch mWorkingSwitch;
    private int status=0;


    private String customerId = "",destination;
    private LatLng destinationLatLng;
    private float rideDistance;

    private Boolean isLoggingOut = false;
    private LinearLayout mUserInfo;
    private TextView mUserName, mUserPhone, mCustomerDestination;
    public LatLng pickLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,

                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            mapFragment.getMapAsync(this);
        }

        mUserInfo =  findViewById(R.id.userInfo);
        mUserName =  findViewById(R.id.customerName);
        mUserPhone = findViewById(R.id.customerPhone);
        mCustomerDestination =  findViewById(R.id.customerDestination);

        mWorkingSwitch = findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    connectDriver();

                }else {
                        disconnectDriver();
                }

            }
        });

        mSettings = (Button) findViewById(R.id.settings);
        logout = (Button) findViewById(R.id.logout);
        mHistory = (Button) findViewById(R.id.history);
        mRideStatus = (Button) findViewById(R.id.rideStatus);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status){
                    case 1:
                        status=2;
                        erasePolylines();
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
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
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DriverMapActivity.this, DriverSettingsActivity.class);
                startActivity(i);
                return;

            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DriverMapActivity.this, HistoryActivity.class);
                i.putExtra("CustomerOrDriver","Drivers");
                startActivity(i);
                return;

            }
        });

        getAssignedCustomer();

    }

    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(driverId).child("customerRequest").child(CUSTOMER_RIDE_ID);

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    status=1;
                    customerId = snapshot.getValue().toString();
                    getAssignedCustomerPickupLocation(customerId);
                    getAssignedCustomerDestination();

                } else {
                   endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                    LatLng pickupLatLng = new LatLng(locationLat, locationLog);

                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title(String.valueOf(R.string.pickup_location)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    getRouteMarker(pickupLatLng);

                }
                mUserInfo.setVisibility(View.GONE);
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

    private void getAssignedCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(driverId).child("customerRequest");

        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                    if(map.get("destination")!=null){
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText("Destination: "+ destination );

                    }
                    else {
                        mCustomerDestination.setText("Destination: --)");
                    }
                    double destinationLat =0.0;
                    double destinationLng =0.0;
                    if(map.get("destinationLat")!=null){
                        destinationLat=Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng")!=null){
                        destinationLng=Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                    }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAssignedCustomerPickupInfo() {
        mUserInfo.setVisibility(View.VISIBLE);
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(customerId).child(CUSTOMER_RIDE_ID);
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {

                        mUserName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {

                        mUserPhone.setText(map.get("phone").toString());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

     private void endRide(){
                   mRideStatus.setText("picked customer");
                   erasePolylines();

                   String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(userId).child("CUSTOMER_REQUEST");
                   driverRef.removeValue();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CUSTOMER_REQUEST);
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(customerId);
                    customerId ="";
                    rideDistance = 0;

                    if (pickupMarker != null){
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

     private void  recordRide(){
         String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
         DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(userId).child("history");
         DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child("customers").child(customerId).child("history");
         DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
         driverRef.removeValue();
         String requestId = historyRef.push().getKey();
         driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

         HashMap map = new HashMap();
         map.put("driver",userId);
         map.put("customer",customerId);
         map.put("rating",0);
         map.put("timestamp",getCurrentTimestamp());
         map.put("destination",destination);
         map.put("location/from/lat",pickLatLng.latitude);
         map.put("location/from/lng",pickLatLng.longitude);
         map.put("location/to/lat",destinationLatLng.longitude);
         map.put("location/to/lng",destinationLatLng.longitude);
         map.put("distance",rideDistance);
         historyRef.child(requestId).updateChildren(map);

     }




    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,

                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,}, REQUEST_LOCATION);
            return;
        }

        buildGoogleApiClint();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClint() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void connectDriver(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void disconnectDriver(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (getApplicationContext() != null) {

            if(!equals("")){
                rideDistance += mLastLocation.distanceTo(location)/1000;

            }

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
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
}