package com.example.sharemycar;

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

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.CAR_TYPE;
import static com.example.sharemycar.Constants.DRIVER;

public class DriverSettingsActivity extends AppCompatActivity {

    private EditText mFirstName, mPhoneField, mLastName;

    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID;
    private String first_name;
    private String mPhone;
    private String last_name;
    private String type;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        mFirstName = (EditText) findViewById(R.id.firsst_name_d_s);
        mLastName = (EditText) findViewById(R.id.last_name_d_s);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        mBack = (Button)findViewById(R.id.back);

        mConfirm = (Button)findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(DRIVER).child(userID);
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
                    if(map.get("first_Name")!=null){
                        first_name = map.get("first_Name").toString();
                        mFirstName.setText(first_name);
                    }
                    if(map.get("phone")!=null) {
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                        if(map.get("last_Name")!=null){
                            last_name = map.get("last_Name").toString();
                            mLastName.setText(last_name);
                    }
                    if(map.get("car_Type")!=null){
                        type = map.get("car_Type").toString();
                        switch (type){
                            case "Motorcycle":
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
        first_name = mFirstName.getText().toString();
        mPhone = mPhoneField.getText().toString();
        last_name = mLastName.getText().toString();

        int selectId = mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if (radioButton.getText()==null){
            return;
        }

        type = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("first_Name", first_name);
        userInfo.put("last_Name", last_name);
        userInfo.put("phone",mPhone);
        userInfo.put("car_Type", type);

        mDriverDatabase .updateChildren(userInfo);

        finish();


    }
}