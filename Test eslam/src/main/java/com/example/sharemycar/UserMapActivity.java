package com.example.sharemycar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import static com.example.sharemycar.Constants.CUSTOMER;
import static com.example.sharemycar.Constants.CUSTOMER_RIDE_ID;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.DRIVER_AVAILABLE;
import static com.example.sharemycar.Constants.DRIVER_WORKING;
import static com.example.sharemycar.Constants.GET_UID;
import static com.example.sharemycar.Constants.USER_REQUEST;

public class UserMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    GoogleApiClient googleApiClient;
    Location LastLocation;
    LocationRequest locationRequest;

    private GoogleMap Map;
    private SupportMapFragment mapFragment;

    private Button logout, mRequest;
    private LatLng pickupLocation;

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;
    private Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        logout = findViewById(R.id.logout);
        mRequest = findViewById(R.id.request);

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent (UserMapActivity.this,MainActivity.class );
            startActivity(intent);
            finish();
            return;

        });

        mRequest.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USER_REQUEST);
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userId , new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()));


            pickupLocation = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());

            Map.addMarker(new MarkerOptions().position(pickupLocation).title(getString(R.string.pickup_here)));

            mRequest.setText(getString(R.string.getting_driver));

            getClosestDriver();
        });



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }
    }

    private void getClosestDriver() {
        DatabaseReference drivers_location = FirebaseDatabase.getInstance().getReference().child(DRIVER_AVAILABLE);
        GeoFire geoFire = new GeoFire(drivers_location);

        GeoQuery query = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
        query.removeAllListeners();
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound)
                {
                    driverFound = true;
                    driverFoundID = key;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(CUSTOMER).child(DRIVER).child(driverFoundID);
                    HashMap map = new HashMap();
                    map.put(CUSTOMER_RIDE_ID,GET_UID());
                    driverRef.updateChildren(map);
                    getDriverLocation();
                    mRequest.setText(R.string.looking_for_driver);

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDriverLocation() {
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child(DRIVER_WORKING).child(driverFoundID).child("1");
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLog = 0;
                    mRequest.setText(R.string.driver_found);
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1) != null){
                        locationLog = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLog);
                    if (driverMarker != null){
                        driverMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText(R.string.driver_here);
                    }else{
                        mRequest.setText(getString(R.string.driver_found) + ":" + distance);
                    }
                    driverMarker = Map.addMarker(new MarkerOptions().position(driverLatLng).title(getString(R.string.your_driver)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        Map.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        LastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        Map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        Map.animateCamera(CameraUpdateFactory.zoomTo(13));


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length >0 && grantResults [0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
}
