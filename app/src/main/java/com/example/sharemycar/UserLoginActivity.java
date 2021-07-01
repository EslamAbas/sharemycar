package com.example.sharemycar;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sharemycar.fragment.UserLoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserLoginActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        fragmentManager = getSupportFragmentManager();
        loudFragment(new UserLoginFragment());


    }

    public void loudFragment(Fragment fragment) {
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.user_container, fragment).commit();
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

