package com.example.chelvan.neon_uom;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileHandling extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST =71 ;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    EditText fname,lname,cityname,mobile,newpass,repass;
    String email,firstname,lastname,city,mobilenum,newpassword,retypepassword,profile_url;
    Button save,reset;
    ImageView profile;
    TextView headName;

    ProgressDialog progressDialog;
    AlertDialog alertDialog;

    private Uri filePath;
    String profile_link = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_handling);

        storageReference = storage.getReference();

        fname = findViewById(R.id.e1);
        lname = findViewById(R.id.e2);
        cityname = findViewById(R.id.e3);
        mobile = findViewById(R.id.e4);
        save = findViewById(R.id.button);
        profile = findViewById(R.id.imageView3);

        newpass = findViewById(R.id.editText);
        repass = findViewById(R.id.editText1);
        reset = findViewById(R.id.button1);

        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        progressDialog = new ProgressDialog(ProfileHandling.this);
        progressDialog.setTitle("Saving...");

        alertDialog = new AlertDialog.Builder(ProfileHandling.this).create();
        alertDialog.setTitle("Neon");

        docRef = db.collection(email).document("userDetails");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();

                    if(doc.exists()){
                        firstname = doc.get("fname").toString();
                        lastname = doc.get("lname").toString();
                        mobilenum = doc.get("mobile").toString();
                        city = doc.get("city").toString();

                        fname.setText(firstname);
                        lname.setText(lastname);
                        cityname.setText(city);
                        mobile.setText(mobilenum);

                        try {
                            profile_link = doc.get("profile").toString();
                        }
                        catch(Exception e){

                        }

                        if(profile_link!=null){
                            storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReferenceFromUrl(profile_link).child("images/" + email);

                            try{
                                final File localFile = File.createTempFile("images", "png");
                                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                        profile.setImageBitmap(bitmap);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                    }
                                });

                            }catch(Exception e){
                                Toast.makeText(ProfileHandling.this, "Error " + e, Toast.LENGTH_SHORT).show();

                            }
                        }

                    }
                }
            }
        });

        profile.setClickable(true);
        profile_url = null;

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()==true){
                    if (ContextCompat.checkSelfPermission(ProfileHandling.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ProfileHandling.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    }

                    if (ContextCompat.checkSelfPermission(ProfileHandling.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                        chooseImage();
                    }

                }else{
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                firstname = fname.getText().toString();
                lastname = lname.getText().toString();
                mobilenum = mobile.getText().toString();
                city = cityname.getText().toString();

                Map<String, Object> user = new HashMap<>();
                user.put("fname", firstname);
                user.put("lname", lastname);
                user.put("city", city);
                user.put("mobile", mobilenum);
                user.put("profile",profile_url);

                docRef = db.collection(email).document("userDetails");
                docRef.set(user,SetOptions.mergeFields("fname","lname","city","mobile")).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileHandling.this,"sucess",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                        alertDialog.setMessage("Your profile is updated");
                        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        alertDialog.show();
                    }
                });

            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newpassword = newpass.getText().toString();
                retypepassword = repass.getText().toString();

                if(!newpassword.isEmpty() && !retypepassword.isEmpty()){

                    if(newpassword.equals(retypepassword)){

                        progressDialog.show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updatePassword(newpassword)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            progressDialog.dismiss();
                                            alertDialog.setMessage("New passwords are saved");
                                            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            alertDialog.show();

                                        }else{
                                            String msg = ((FirebaseAuthException) task.getException()).getMessage();
                                            progressDialog.dismiss();

                                            alertDialog.setMessage(msg);
                                            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            alertDialog.show();

                                        }

                                    }
                                });

                    }else{
                        alertDialog.setMessage("Those passwords did not match");
                        alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.show();
                    }

                }else{
                    if(newpassword.isEmpty()){
                        newpass.setError("Field cannot be left blank");
                    }
                    if(retypepassword.isEmpty()){
                        repass.setError("Field cannot be left blank");
                    }
                }
            }
        });


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
                String filepp = im.getRealPathFromURI(ProfileHandling.this,filePath);
                Bitmap bitmapmodified = im.modifyOrientation(bitmap,filepp);

                profile.setImageBitmap(bitmapmodified);
                uploadImage();
            }
            catch (IOException e)
            {
                Toast.makeText(ProfileHandling.this,""+e,Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ProfileHandling.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }

    }

}
