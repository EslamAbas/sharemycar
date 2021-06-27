package com.example.sharemycar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

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