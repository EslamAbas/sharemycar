package com.example.sharemycar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import static com.example.sharemycar.Constants.CUSTOMER;
import static com.example.sharemycar.Constants.CUSTOMER_RIDE_ID;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.DRIVER_AVAILABLE;
import static com.example.sharemycar.Constants.DRIVER_WORKING;
import static com.example.sharemycar.Constants.GET_UID;
import static com.example.sharemycar.Constants.USER_REQUEST;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    Location last_location;
    LocationRequest locationRequest;

    private Button mLogout;
    private String UserId = "";
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mLogout = findViewById(R.id.d_logout);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getAssignedUser();
    }





    private void getAssignedUser() {
        DatabaseReference assignedUserRef = FirebaseDatabase.getInstance().getReference().child(CUSTOMER).child(DRIVER).child(GET_UID()).child(CUSTOMER_RIDE_ID);
        assignedUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                        UserId = snapshot.getValue().toString();
                        getAssignedUserPickUpLocation();
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }







    private void getAssignedUserPickUpLocation() {
        DatabaseReference assignedUserPickUpLocationRef = FirebaseDatabase.getInstance().getReference().child(USER_REQUEST).child(UserId).child("1");
        assignedUserPickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLog = 0;

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1) != null) {
                        locationLog = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLog);
                    googleMap.addMarker(new MarkerOptions().position(driverLatLng).title(getString(R.string.pickup_location)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }







    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleAPIClint();
        this.googleMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleAPIClint() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }









    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (getApplicationContext() != null) {
            last_location = location;
            LatLng lng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(lng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            DatabaseReference refAVAILABLE = FirebaseDatabase.getInstance().getReference(DRIVER_AVAILABLE);
            DatabaseReference refWORKING = FirebaseDatabase.getInstance().getReference(DRIVER_WORKING);
            GeoFire geoFireAVAILABLE = new GeoFire(refAVAILABLE);
            GeoFire geoFireWORKING = new GeoFire(refWORKING);

            switch (UserId) {
                case "":
                    geoFireWORKING.removeLocation(GET_UID());
                    geoFireAVAILABLE.setLocation(GET_UID(), new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAVAILABLE.removeLocation(GET_UID());
                    geoFireWORKING.setLocation(GET_UID(), new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }









    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    protected void onStop() {
        super.onStop();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DRIVER_AVAILABLE);
        GeoFire geoFire = new GeoFire(reference);
        geoFire.removeLocation(GET_UID());
    }
}