package com.example.sharemycar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class DriverSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField,mCarField;

    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID;
    private String mName;
    private String mPhone;
    private String mCar;
    private String mService;
    private RadioGroup mRadioGroup;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mCarField = (EditText) findViewById(R.id.car);
        mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        mBack = (Button)findViewById(R.id.back);

        mConfirm = (Button)findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);
        getUserInfo();
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });


    }
    private  void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone")!=null) {
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                        if(map.get("car")!=null){
                            mCar = map.get("car").toString();
                            mCarField.setText(mCar);
                    }
                    if(map.get("service")!=null){
                        mService = map.get("service").toString();
                        switch (mService){
                            case "Motorbikes":
                                mRadioGroup.check(R.id.Motorbikes);
                                break;
                            case "Vehicles":
                                mRadioGroup.check(R.id.Vehicles);
                                break;
                            case "Trucks":
                                mRadioGroup.check(R.id.Trucks);
                                break;

                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mCar = mCarField.getText().toString();

        int selectId = mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if (radioButton.getText()==null){
            return;
        }

        mService = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name",mName);
        userInfo.put("phone",mPhone);
        userInfo.put("car",mCar);
        userInfo.put("service",mService);

        mDriverDatabase .updateChildren(userInfo);

        finish();


    }
}