package com.example.sharemycar.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharemycar.DriverMapActivity;
import com.example.sharemycar.R;
import com.example.sharemycar.models.DriverLicenseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.DRIVER;
import static com.example.sharemycar.Constants.GET_UID;
import static com.example.sharemycar.Constants.LICENCE;

public class DriverLicenseFragment extends Fragment {
    View view;

    private ImageView car_lic1, car_lic2, dr_lic1, dr_lic2, drugs_test, car_err, driver_err, test_err;
    private Uri car_uri1, car_uri2, dr_uri1, dr_uri2, test_uri;
    private Button register_btn;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private RotateLoading loading;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_driver_license, container, false);

        car_lic1 = view.findViewById(R.id.license_btn1);
        car_lic2 = view.findViewById(R.id.license_btn2);
        dr_lic1 = view.findViewById(R.id.d_license_btn1);
        dr_lic2 = view.findViewById(R.id.d_license_btn2);
        drugs_test = view.findViewById(R.id.drugs_test_btn);
        car_err = view.findViewById(R.id.car_err);
        driver_err = view.findViewById(R.id.driver_err);
        test_err = view.findViewById(R.id.test_err);
        register_btn = view.findViewById(R.id.registration_btn_d);
        loading = view.findViewById(R.id.rotateloading);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        register_btn.setOnClickListener(v -> CheckData());

        car_lic1.setOnClickListener(v -> {

            Intent car_lic_photo1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(car_lic_photo1, 1);
        });

        car_lic2.setOnClickListener(v -> {
            Intent car_lic_photo2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(car_lic_photo2, 2);
        });

        dr_lic1.setOnClickListener(v -> {
            Intent dr_lic_photo1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(dr_lic_photo1, 3);
        });

        dr_lic2.setOnClickListener(v -> {
            Intent dr_lic_photo2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(dr_lic_photo2, 4);
        });

        drugs_test.setOnClickListener(v -> {
            Intent drugs_test_photo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(drugs_test_photo, 5);
        });


    }


    private void CheckData() {

        if (picValidation(car_uri1, car_err) && picValidation(car_uri2, car_err) && picValidation(dr_uri1, driver_err)
                && picValidation(dr_uri2, driver_err) && picValidation(test_uri, test_err)) {
            loading.start();
            UpdatedLicence();
        }
    }

    private void UpdatedData(String Car_Licence_1, String Car_Licence_2, String Driver_Licence_1,
                             String Driver_Licence_2, String Test_Licence) {

        DriverLicenseModel model = new DriverLicenseModel(Car_Licence_1, Car_Licence_2, Driver_Licence_1, Driver_Licence_2, Test_Licence);
        reference.child(ACCOUNTS).child(DRIVER).child(GET_UID()).child(LICENCE).setValue(model);
        loading.stop();

        Intent i = new Intent(getContext(), DriverMapActivity.class);
        startActivity(i);
        getActivity().finish();
    }



    private void UpdatedLicence() {

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
                String path1 = download_uri.toString();
                UpdatedLicence(path1);

            }).addOnFailureListener(e -> {

            });
        }
    }
    private void UpdatedLicence(String path1) {

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
                String path2= download_uri.toString();
                UpdatedLicence(path1,path2);

            }).addOnFailureListener(e -> {

            });
        }
    }

    private void UpdatedLicence(String path1, String path2)  {

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
                String path3 = download_uri.toString();
                UpdatedLicence(path1,path2,path3);

            }).addOnFailureListener(e -> {

            });
        }
    }
    private void UpdatedLicence(String path1,String path2,String path3) {

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
                String path4 = download_uri.toString();
                UpdatedLicence(path1,path2,path3,path4);

            }).addOnFailureListener(e -> {

            });
        }
    }
    private void UpdatedLicence(String path1,String path2,String path3,String path4) {

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
                String path5 = download_uri.toString();
                UpdatedData(path1,path2,path3,path4,path5);

            }).addOnFailureListener(e -> {

            });
        }
    }

    private boolean picValidation(Uri u1, ImageView error) {

        if (u1 == null) {
            error.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Please Add Your License", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            error.setVisibility(View.INVISIBLE);
            return true;
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