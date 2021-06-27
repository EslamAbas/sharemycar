package com.example.sharemycar.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.sharemycar.R;
import com.example.sharemycar.UserLoginActivity;
import com.example.sharemycar.UserMapActivity;
import com.example.sharemycar.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.GET_UID;
import static com.example.sharemycar.Constants.USER;


public class UserRegistrationFragment extends Fragment {

    View view;
    private EditText email, password, f_name, l_name, phone;
    private Button sign_up_btn;
    private ImageButton imageButton;

    private String email_text, pas_text, f_name_text, l_name_text, phone_text;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_registretion, container, false);

        email = view.findViewById(R.id.email_signup_u);
        password = view.findViewById(R.id.password_signup_u);
        f_name = view.findViewById(R.id.first_name_u);
        l_name = view.findViewById(R.id.last_name_u);
        phone = view.findViewById(R.id.mobile_input_u);
        sign_up_btn = view.findViewById(R.id.signup_button_u);
        imageButton = view.findViewById(R.id.back_button_signup_u);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        imageButton.setOnClickListener(v ->
                ((UserLoginActivity) getActivity()).loudFragment(new UserLoginFragment()));

        sign_up_btn.setOnClickListener(view -> {
            checkData();
        });


    }

    private void checkData() {
        email_text = email.getText().toString();
        pas_text = password.getText().toString();
        f_name_text = f_name.getText().toString();
        l_name_text = l_name.getText().toString();
        phone_text = phone.getText().toString();

        if (nameValidation(f_name_text, l_name_text) && emailValidation(email_text) && passwordValidation(pas_text) && phoneValidation(phone_text)) {

            auth.fetchSignInMethodsForEmail(email_text).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = task.getResult().getSignInMethods().isEmpty();
                    if (check) {
                        auth.createUserWithEmailAndPassword(email_text, pas_text).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                UploadData(email_text, f_name_text, l_name_text, phone_text);
                            }
                        });
                    } else {
                        email.setError("Email Is Already Taken");
                    }

                }
            });
        } else {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }


    }

    private void UploadData(String email_text, String f_name_text, String l_name_text, String phone_text) {
        UserModel model = new UserModel(f_name_text, l_name_text, email_text, phone_text);
        reference.child(ACCOUNTS).child(USER).child(GET_UID()).setValue(model);
        Intent i = new Intent(getContext(), UserMapActivity.class);
        startActivity(i);
        getActivity().finish();


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

}