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

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.USER;

public class UserSettingsActivity extends AppCompatActivity {

    private EditText mFirstName,mLastName, mPhoneField;

    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userID;
    private String first_name;
    private String last_name;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mFirstName = (EditText) findViewById(R.id.first_name_u_s);
        mLastName = (EditText) findViewById(R.id.last_name_u_s);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mBack = (Button)findViewById(R.id.back);

        mConfirm = (Button)findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(USER).child(userID);
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
                    if(map.get("first_Name")!=null){
                        first_name = map.get("first_Name").toString();
                        mFirstName.setText(first_name);
                    }
                    if(map.get("last_name")!=null){
                        first_name = map.get("last_name").toString();
                        mFirstName.setText(first_name);
                    }
                    if(map.get("phone_Number")!=null){
                        mPhone = map.get("phone_Number").toString();
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
        first_name = mFirstName.getText().toString();
        last_name = mLastName.getText().toString();
        mPhone = mPhoneField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("first_Name", first_name);
        userInfo.put("last_name", last_name);
        userInfo.put("phone_Number",mPhone);
        mUserDatabase.updateChildren(userInfo);

        finish();


    }
}