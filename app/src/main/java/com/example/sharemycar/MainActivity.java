package com.example.sharemycar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.GET_UID;
import static com.example.sharemycar.Constants.USER;

public class MainActivity extends AppCompatActivity {
    private Button mUser, mDriver;
    private OnBoardingPagerAdapter adapter;
    private ArrayList<Integer> layouts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViewPager();

        mDriver = (Button) findViewById(R.id.driver_login);
        mUser = (Button) findViewById(R.id.user_login);

        startService(new Intent(MainActivity.this, onAppKilled.class));

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            category();
        }
    }

    private void category() {

        final String id = GET_UID();
        FirebaseDatabase firebaseDatabase;
        final DatabaseReference databaseReference;

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        databaseReference.child(ACCOUNTS).child(DRIVER).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(id)) {
                            updateUI(DriverMapActivity.class);
                        } else {
                            databaseReference.child(ACCOUNTS).child(USER).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)) {
                                                updateUI(UserMapActivity.class);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public void updateUI(Class aClass) {
        // go to the main activity
        Intent i = new Intent(MainActivity.this, aClass);
        startActivity(i);
        // kill current activity
        finish();
    }


    private void setupViewPager() {
        layouts.add(R.layout.onboarding_first);
        layouts.add(R.layout.onboarding_second);
        layouts.add(R.layout.onboarding_third);

        adapter = new OnBoardingPagerAdapter(this, layouts);
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);

    }

}