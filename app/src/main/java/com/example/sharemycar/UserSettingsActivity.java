package com.example.sharemycar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class UserSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField;

    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userID;
    private String mName;
    private String mPhone;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mBack = (Button)findViewById(R.id.back);

        mConfirm = (Button)findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Users").child(userID);
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
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone")!=null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
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

        Map userInfo = new HashMap();
        userInfo.put("name",mName);
        userInfo.put("Phone",mPhone);
        mUserDatabase.updateChildren(userInfo);

        finish();


    }
}