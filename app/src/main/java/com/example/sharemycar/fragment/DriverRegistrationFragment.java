package com.example.sharemycar.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharemycar.DriverLoginActivity;
import com.example.sharemycar.R;
import com.example.sharemycar.models.DriverModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.victor.loading.rotate.RotateLoading;

import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.CAR_TYPE;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.GET_UID;


public class DriverRegistrationFragment extends Fragment implements View.OnClickListener {

    View view;

    private EditText email, password, f_name, l_name, phone;
    private Button next_btn;
    private ImageButton imageButton;
    private String email_text, pas_text, f_name_text, l_name_text, phone_text, type_text;

    private RadioGroup mRadioGroup;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private RotateLoading loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_driver_registretion, container, false);

        email = view.findViewById(R.id.email_signup_d);
        password = view.findViewById(R.id.password_signup_d);
        f_name = view.findViewById(R.id.first_name_d);
        l_name = view.findViewById(R.id.last_name_d);
        phone = view.findViewById(R.id.mobile_input_d);
        next_btn = view.findViewById(R.id.next_btn);
        mRadioGroup = view.findViewById(R.id.radioGroup_d);
        loading = view.findViewById(R.id.rotateloading);

        imageButton = view.findViewById(R.id.back_button_signup_d);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        imageButton.setOnClickListener(this);

        next_btn.setOnClickListener(this);




    }

    private void checkData() {
        email_text = email.getText().toString();
        pas_text = password.getText().toString();
        f_name_text = f_name.getText().toString();
        l_name_text = l_name.getText().toString();
        phone_text = phone.getText().toString();
        if (mRadioGroup.getCheckedRadioButtonId() != -1) {
            RadioButton type_selected = view.findViewById(mRadioGroup.getCheckedRadioButtonId());
            type_text = type_selected.getText().toString();
        }


        if (nameValidation(f_name_text, l_name_text) && emailValidation(email_text) && passwordValidation(pas_text) && phoneValidation(phone_text) && typeValidation(type_text)) {
            loading.start();
            auth.fetchSignInMethodsForEmail(email_text).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = task.getResult().getSignInMethods().isEmpty();
                    if (check) {
                        auth.createUserWithEmailAndPassword(email_text, pas_text).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DriverModel driverModel = new DriverModel(email_text, f_name_text, l_name_text, phone_text, type_text);
                                reference.child(ACCOUNTS).child(DRIVER).child(GET_UID()).setValue(driverModel);
                                reference.child(CAR_TYPE).child(type_text).child(GET_UID()).setValue(driverModel);
                                loading.stop();
                                ((DriverLoginActivity)getActivity()).loudFragment(new DriverLicenseFragment());

                            }
                        });
                    } else {
                        email.setError(getString(R.string.email_used));
                        loading.stop();
                    }


                }
            });


        }
    }







    private boolean nameValidation(String fname, String lname) {

        if (TextUtils.isEmpty(fname)) {
            f_name.setError(getString(R.string.f_name_emplty));
            return false;
        } else if (TextUtils.isEmpty(lname)) {
            l_name.setError(getString(R.string.l_name_empty));
            return false;
        } else {
            f_name.setError(null);
            l_name.setError(null);
            return true;
        }

    }


    private boolean typeValidation(String type) {

        if (TextUtils.isEmpty(type)) {
            Toast.makeText(getContext(), getString(R.string.empty_car_type), Toast.LENGTH_SHORT).show();
            return false;
        } else {

            return true;
        }

    }


    private boolean emailValidation(String email) {
        if (TextUtils.isEmpty(email)) {
            this.email.setError(getString(R.string.empty_email));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError(getString(R.string.wrong_email));
            return false;
        } else {
            this.email.setError(null);
            return true;
        }
    }

    private boolean passwordValidation(String pass) {
        if (TextUtils.isEmpty(pass)) {
            password.setError(getString(R.string.empty_pass));
            return false;
        } else if (pass.length() < 6) {
            password.setError(getString(R.string.short_pass));
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    private boolean phoneValidation(String phone_text) {
        if (TextUtils.isEmpty(phone_text)) {
            phone.setError(getString(R.string.embty_phone));
            return false;
        } else if (!Pattern.matches("^(010|011|012|015)[0-9]{8}$", phone_text)) {
            phone.setError(getString(R.string.incorrect_phone));
            return false;
        } else {
            phone.setError(null);
            return true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button_signup_d:
                ((DriverLoginActivity) getActivity()).loudFragment(new DriverLoginFragment());
                break;
            case R.id.next_btn:
                checkData();
                break;

        }
    }


}
