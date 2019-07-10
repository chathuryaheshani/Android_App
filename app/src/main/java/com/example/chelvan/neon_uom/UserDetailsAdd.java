package com.example.chelvan.neon_uom;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserDetailsAdd extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    EditText firstname, lastname, mobile;
    Button submit;
    RadioGroup gender;
    RadioButton male, female;
    Spinner option,city_spinner;
    ImageView profile;

    String fname, lname, city, mobilenum, gender_detail, email,profile_url,optionValue;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    AlertDialog alertDialog;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details_add);

        firstname = findViewById(R.id.editText1);
        lastname = findViewById(R.id.editText2);
        city_spinner = findViewById(R.id.spinner4);
        mobile = findViewById(R.id.editText4);
        submit = findViewById(R.id.button);
        gender = findViewById(R.id.radioGroup);
        male = findViewById(R.id.radioButton);
        female = findViewById(R.id.radioButton2);
        option = findViewById(R.id.spinner2);
        profile = findViewById(R.id.imageView3);

        alertDialog = new AlertDialog.Builder(UserDetailsAdd.this).create();
        alertDialog.setTitle("Neon");

        storageReference = storage.getReference();

        profile.setClickable(true);
        profile_url = null;

        final Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        gender_detail = "male";

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton:
                        gender_detail = "male";
                        break;
                    case R.id.radioButton2:
                        gender_detail = "female";
                        break;
                }
            }
        });


        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnline()==true){
                    if (ContextCompat.checkSelfPermission(UserDetailsAdd.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(UserDetailsAdd.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }

                    if (ContextCompat.checkSelfPermission(UserDetailsAdd.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        chooseImage();
                    }

                }

                else{
                    alertDialog.setMessage("No Internet Connection");
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnline()==true){

                    fname = firstname.getText().toString();
                    lname = lastname.getText().toString();
                    city = city_spinner.getSelectedItem().toString();
                    mobilenum = mobile.getText().toString();
                    optionValue = option.getSelectedItem().toString();

                    if (!fname.isEmpty() && !city.isEmpty() && !mobilenum.isEmpty() && mobilenum.length()==10) {

                        final ProgressDialog progressDialog = new ProgressDialog(UserDetailsAdd.this);
                        progressDialog.setTitle("Saving...");
                        progressDialog.show();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, Object> user = new HashMap<>();
                        user.put("fname", fname);
                        user.put("lname", lname);
                        user.put("city", city);
                        user.put("mobile", mobilenum);
                        user.put("gender", gender_detail);
                        user.put("option", optionValue);
                        user.put("profile",profile_url);

                        db.collection(email).document("userDetails")
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        if (optionValue.equals("Driver")) {
                                            Intent intent = new Intent(UserDetailsAdd.this, VehicleDetailsAdd.class);
                                            intent.putExtra("email", email);
                                            startActivity(intent);

                                        } else {
                                        Intent intent = new Intent(UserDetailsAdd.this, Passenger.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(UserDetailsAdd.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    else {

                        if (fname.isEmpty()) {
                            firstname.setError("Field cannot be left blank");
                        }

                        if (mobilenum.isEmpty()) {
                            mobile.setError("Field cannot be left blank");
                        }

                        if(!mobilenum.isEmpty() && mobilenum.length()!=10){
                            mobile.setError("phone number should be 10 digits");
                        }
                    }

                }

                else{
                    alertDialog.setMessage("No Internet Connection");
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();

                }
            }
        });

    }



    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            IamgeHandling im = new IamgeHandling();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                String filepp = im.getRealPathFromURI(UserDetailsAdd.this,filePath);
                Bitmap bitmapmodified = im.modifyOrientation(bitmap,filepp);

                profile.setImageBitmap(bitmapmodified);
                uploadImage();
            }
            catch (IOException e)
            {
                Toast.makeText(UserDetailsAdd.this,""+e,Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("images/"+ email);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            profile_url = ref.getRoot().toString();
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserDetailsAdd.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }

    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting() ) {
            return true;
        }

        else {
            return false;
        }
    }
}
