package com.example.sharemycar.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class UserLoginFragment extends Fragment {
    private View view;

    private EditText mEmail, mPassword;
    private Button mLogin, mRegistration;

    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_login, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        mEmail = view.findViewById(R.id.email_input_u);
        mPassword = view.findViewById(R.id.password_input_u);

        mLogin = view.findViewById(R.id.login_button_u);
        mRegistration = view.findViewById(R.id.registration_button_u);

        ImageButton imageButton = view.findViewById(R.id.back_button_u);

        imageButton.setOnClickListener((View.OnClickListener) v -> ( getActivity()).onBackPressed());

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UserLoginActivity) getActivity()).loudFragment(new UserRegistrationFragment());

            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                Intent i = new Intent(getContext(), UserMapActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }
                        }
                    });
                }

            }
        });

    }
}