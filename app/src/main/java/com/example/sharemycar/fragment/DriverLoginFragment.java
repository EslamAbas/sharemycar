package com.example.sharemycar.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharemycar.DriverLoginActivity;
import com.example.sharemycar.DriverMapActivity;
import com.example.sharemycar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.GET_UID;


public class DriverLoginFragment extends Fragment {
    private View view;

    private EditText mEmail, mPassword;
    private Button mLogin, mRegistration;

    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_driver_login, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        mEmail = view.findViewById(R.id.email_input_d);
        mPassword = view.findViewById(R.id.password_input_d);

        mLogin = view.findViewById(R.id.login_button_d);
        mRegistration = view.findViewById(R.id.registration_button_d);

        ImageButton imageButton = view.findViewById(R.id.back_button_d);

        imageButton.setOnClickListener((View.OnClickListener) v -> ((DriverLoginActivity) getActivity()).onBackPressed());

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DriverLoginActivity) getActivity()).loudFragment(new DriverRegistrationFragment());

            }
        });

        mLogin.setOnClickListener(v -> {
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                mEmail.setError(getString(R.string.empty_email));
                mPassword.setError(getString(R.string.empty_pass));
                Toast.makeText(getContext(), "Sign in error", Toast.LENGTH_SHORT).show();
            } else {

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            mEmail.setError(getString(R.string.wrong_email_and_pass));
                            mPassword.setError(getString(R.string.wrong_email_and_pass));
                            Toast.makeText(getContext(), "Sign in error", Toast.LENGTH_SHORT).show();
                        } else {
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
                                                Intent i = new Intent(getContext(), DriverMapActivity.class);
                                                startActivity(i);
                                                getActivity().finish();
                                            } else {

                                                mEmail.setError(getString(R.string.wrong_email_and_pass));
                                                mPassword.setError(getString(R.string.wrong_email_and_pass));
                                                Toast.makeText(getContext(), "Sign in error", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }


                                    });
                        }

                    }
                });
            }
        });
    }
}