package com.example.sharemycar.fragment;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sharemycar.DriverLoginActivity;
import com.example.sharemycar.DriverMapActivity;
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
import com.squareup.picasso.Picasso;

import java.util.UUID;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.GET_UID;


public class DriverRegistrationFragment extends Fragment implements View.OnClickListener {

    View view;

    private EditText email, password, f_name, l_name, phone, car_type, car_color;
    private Button sign_up_btn;
    private ImageButton imageButton;
    private ImageView car_lic1, car_lic2, dr_lic1, dr_lic2, drugs_test;
    private String email_text, pas_text, f_name_text, l_name_text, phone_text, type_text, color_text;

    private Uri car_uri1, car_uri2, dr_uri1, dr_uri2, test_uri;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;


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
        sign_up_btn = view.findViewById(R.id.signup_button_d);
        car_type = view.findViewById(R.id.car_type);
        car_color = view.findViewById(R.id.car_color);

        imageButton = view.findViewById(R.id.back_button_signup_d);
        car_lic1 = view.findViewById(R.id.license_btn1);
        car_lic2 = view.findViewById(R.id.license_btn2);
        dr_lic1 = view.findViewById(R.id.d_license_btn1);
        dr_lic2 = view.findViewById(R.id.d_license_btn2);
        drugs_test = view.findViewById(R.id.drugs_test_btn);

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

        sign_up_btn.setOnClickListener(this);

        car_lic1.setOnClickListener(this);
        car_lic2.setOnClickListener(this);
        dr_lic1.setOnClickListener(this);
        dr_lic2.setOnClickListener(this);
        drugs_test.setOnClickListener(this);


    }

    private void checkData() {
        email_text = email.getText().toString();
        pas_text = password.getText().toString();
        f_name_text = f_name.getText().toString();
        l_name_text = l_name.getText().toString();
        phone_text = phone.getText().toString();
        type_text = car_type.getText().toString();
        color_text = car_color.getText().toString();

        if (nameValidation(f_name_text, l_name_text) && emailValidation(email_text) && passwordValidation(pas_text) && phoneValidation(phone_text)
                && typeValidation(type_text) && colorValidation(color_text) && picValidation(car_uri1, car_uri2, dr_uri1, dr_uri2, test_uri)) {

            auth.fetchSignInMethodsForEmail(email_text).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean check = task.getResult().getSignInMethods().isEmpty();
                    if (check) {
                        auth.createUserWithEmailAndPassword(email_text, pas_text).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                UploadLicense(email_text, f_name_text, l_name_text, phone_text, type_text, color_text);
                            }
                        });
                    } else {
                        email.setError(getString(R.string.email_used));
                    }

                }
            });
        }
    }

    private void UploadLicense(String email_text, String f_name_text, String l_name_text, String phone_text, String type_text, String color_text) {


        if (car_uri1 != null) {
            UploadTask uploadTask;
            StorageReference ref = storageReference.child("License/"+ UUID.randomUUID().toString());

            uploadTask = ref.putFile(car_uri1);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                Uri download_uri = task.getResult();
                String c_lic_path1 = download_uri.toString();
                UploadLicense(email_text, f_name_text, l_name_text, phone_text, type_text, color_text,c_lic_path1);

            }).addOnFailureListener(e -> {

            });
        }
    }

    private void UploadLicense(String email_text, String f_name_text, String l_name_text, String phone_text, String type_text, String color_text, String c_lic_path1) {
        if (car_uri2 != null) {
            UploadTask uploadTask;
            StorageReference ref = storageReference.child("License/"+ UUID.randomUUID().toString());

            uploadTask = ref.putFile(car_uri2);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                Uri download_uri = task.getResult();
               String c_lic_path2 = download_uri.toString();
                UploadLicense(email_text, f_name_text, l_name_text, phone_text, type_text, color_text,c_lic_path1,c_lic_path2);

            }).addOnFailureListener(e -> {

            });
        }

    }

    private void UploadLicense(String email_text, String f_name_text, String l_name_text, String phone_text, String type_text, String color_text, String c_lic_path1, String c_lic_path2) {
        if (dr_uri1 != null) {
            UploadTask uploadTask;
            StorageReference ref = storageReference.child("License/"+ UUID.randomUUID().toString());

            uploadTask = ref.putFile(dr_uri1);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                Uri download_uri = task.getResult();
                String d_lic_path1 = download_uri.toString();
                UploadLicense(email_text, f_name_text, l_name_text, phone_text, type_text, color_text,c_lic_path1,c_lic_path2,d_lic_path1);

            }).addOnFailureListener(e -> {

            });
        }

    }

    private void UploadLicense(String email_text, String f_name_text, String l_name_text, String phone_text, String type_text, String color_text,
                               String c_lic_path1, String c_lic_path2, String d_lic_path1) {
        if (dr_uri2 != null) {
            UploadTask uploadTask;
            StorageReference ref = storageReference.child("License/"+ UUID.randomUUID().toString());

            uploadTask = ref.putFile(dr_uri2);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                Uri download_uri = task.getResult();
               String d_lic_path2 = download_uri.toString();
                UploadLicense(email_text, f_name_text, l_name_text, phone_text, type_text, color_text,c_lic_path1,c_lic_path2,d_lic_path1,d_lic_path2);

            }).addOnFailureListener(e -> {

            });
        }

    }

    private void UploadLicense(String email_text, String f_name_text, String l_name_text, String phone_text, String type_text, String color_text,
                               String c_lic_path1, String c_lic_path2, String d_lic_path1, String d_lic_path2) {
        if (test_uri != null) {
            UploadTask uploadTask;
            StorageReference ref = storageReference.child("License/"+ UUID.randomUUID().toString());

            uploadTask = ref.putFile(test_uri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                Uri download_uri = task.getResult();
               String drugs_test_path = download_uri.toString();
                UploadData(email_text,f_name_text,l_name_text,phone_text,type_text,color_text
                        ,c_lic_path1,c_lic_path2,d_lic_path1,d_lic_path2,drugs_test_path);
            }).addOnFailureListener(e -> {

            });
        }

    }

    private void UploadData(String email_text, String f_name_text, String l_name_text, String phone_text, String type_text, String color_text,
                            String car_lic1, String car_lice2, String driver_lice1, String driver_lice2, String drugs_test) {

        DriverModel driverModel = new DriverModel(email_text,f_name_text,l_name_text, phone_text, type_text,
                color_text, car_lic1, car_lice2, driver_lice1, driver_lice2, drugs_test);

        reference.child(ACCOUNTS).child(DRIVER).child(GET_UID()).setValue(driverModel);
        Intent i = new Intent(getContext(), DriverMapActivity.class);
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

    private boolean picValidation(Uri u1, Uri u2, Uri u3, Uri u4, Uri u5) {

        if (u1 == null && u2 == null && u3 == null && u4 == null && u5 == null) {
            Toast.makeText(getContext(), "Please Add Your License", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    private boolean typeValidation(String type) {

        if (TextUtils.isEmpty(type)) {
            car_type.setError(getString(R.string.empty_car_type));
            return false;
        } else {
            car_type.setError(null);
            return true;
        }

    }

    private boolean colorValidation(String color) {

        if (TextUtils.isEmpty(color)) {
            car_color.setError(getString(R.string.empty_car_color));
            return false;
        } else {
            car_color.setError(null);

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
            case R.id.signup_button_d:
                checkData();
                break;
            case R.id.license_btn1:
                Intent car_lic_photo1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(car_lic_photo1, 1);
                break;
            case R.id.license_btn2:
                Intent car_lic_photo2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(car_lic_photo2, 2);
                break;
            case R.id.d_license_btn1:
                Intent dr_lic_photo1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(dr_lic_photo1, 3);
                break;
            case R.id.d_license_btn2:
                Intent dr_lic_photo2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(dr_lic_photo2, 4);
                break;
            case R.id.drugs_test_btn:
                Intent drugs_test_photo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(drugs_test_photo, 5);
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        car_uri1 = data.getData();
                        Picasso.get()
                                .load(car_uri1)
                                .placeholder(R.drawable.ic_camera)
                                .error(R.drawable.ic_broken_image)
                                .into(car_lic1);

                    }
                }
                break;
            case 2:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        car_uri2 = data.getData();
                        Picasso.get()
                                .load(car_uri2)
                                .placeholder(R.drawable.ic_camera)
                                .error(R.drawable.ic_broken_image)
                                .into(car_lic2);

                    }
                }
                break;
            case 3:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        dr_uri1 = data.getData();
                        Picasso.get()
                                .load(dr_uri1)
                                .placeholder(R.drawable.ic_camera)
                                .error(R.drawable.ic_broken_image)
                                .into(dr_lic1);

                    }
                }
                break;
            case 4:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        dr_uri2 = data.getData();
                        Picasso.get()
                                .load(dr_uri2)
                                .placeholder(R.drawable.ic_camera)
                                .error(R.drawable.ic_broken_image)
                                .into(dr_lic2);

                    }
                }
                break;
            case 5:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        test_uri = data.getData();
                        Picasso.get()
                                .load(test_uri)
                                .placeholder(R.drawable.ic_camera)
                                .error(R.drawable.ic_broken_image)
                                .into(drugs_test);

                    }
                }
                break;
        }

    }

}
