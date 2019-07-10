package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Homepage extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;
    FirebaseFirestore db;
    DocumentReference docRef;

    String email;
    ProgressBar progressBar;
    private android.support.v7.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        alertDialog = new android.support.v7.app.AlertDialog.Builder(Homepage.this).create();
        alertDialog.setTitle("Neon");

        if(isOnline()==true){

            if (mAuth.getCurrentUser() != null){

                user = mAuth.getCurrentUser();
                email = user.getEmail();

                if (user.isEmailVerified() == true){

                    db = FirebaseFirestore.getInstance();
                    docRef = db.collection(email).document("userDetails");

                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot document = task.getResult();

                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                if (document.exists()) {

                                    String s = document.get("option").toString();

                                    if (s.equals("Driver")) {

                                        docRef = db.collection(email).document("vehicleDetails");
                                        docRef.get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if(task.isSuccessful()){

                                                            if (document.exists()){
                                                               Intent intent = new Intent(Homepage.this, Driver.class);
                                                                intent.putExtra("email", email);
                                                                startActivity(intent);
                                                            }
                                                            else{
                                                                Intent intent = new Intent(Homepage.this, VehicleDetailsAdd.class);
                                                                intent.putExtra("email", email);
                                                                startActivity(intent);
                                                            }

                                                        }else{
                                                            Toast.makeText(Homepage.this,"Error",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }

                                    else {

                                        Intent intent = new Intent(Homepage.this, Passenger.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                    }

                                }

                                else{
                                    Intent iinent = new Intent(Homepage.this, UserDetailsAdd.class);
                                    iinent.putExtra("email", email);
                                    startActivity(iinent);

                                }
                            }

                            else{
                                Toast.makeText(Homepage.this, "Error ", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }

                else{
                    Intent intent = new Intent(Homepage.this, LoginPage.class);
                    startActivity(intent);

                }
            }

            else{
                Intent intent = new Intent(Homepage.this, LoginPage.class);
                startActivity(intent);

            }
        }

        else{
            alertDialog.setMessage("Make sure internet connection");
            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.show();
        }


    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        else {
            return false;
        }
    }
}