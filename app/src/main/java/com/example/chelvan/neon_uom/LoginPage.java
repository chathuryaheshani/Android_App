package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginPage extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;
    FirebaseFirestore db;
    DocumentReference docRef;

    EditText email_text,password_text;
    Button login;
    TextView signup,forget;

    String email,password,value;
    AlertDialog alertDialog;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        login = findViewById(R.id.button3);
        signup = findViewById(R.id.button2);
        forget = findViewById(R.id.button4);
        email_text = findViewById(R.id.editText1);
        password_text = findViewById(R.id.editText2);

        alertDialog = new AlertDialog.Builder(LoginPage.this).create();
        alertDialog.setTitle("Neon");

        final ProgressDialog progressDialog = new ProgressDialog(LoginPage.this);
        progressDialog.setTitle("Logging in...");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline() == true){
                    email = email_text.getText().toString();
                    password = password_text.getText().toString();

                    if (!email.isEmpty() && !password.isEmpty()){
                        progressDialog.show();

                        mAuth.signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if(task.isSuccessful()){
                                            user = mAuth.getCurrentUser();
                                            if (user.isEmailVerified() == true){

                                                db = FirebaseFirestore.getInstance();
                                                docRef = db.collection(email).document("userDetails");

                                                docRef.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                DocumentSnapshot document = task.getResult();

                                                                if(task.isSuccessful()){
                                                                    if (document.exists()) {

                                                                        DocumentSnapshot doc = task.getResult();
                                                                        String s = doc.get("option").toString();

                                                                        if (s.equals("Driver")) {
                                                                            docRef = db.collection(email).document("vehicleDetails");
                                                                            docRef.get()
                                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                            DocumentSnapshot document = task.getResult();
                                                                                            if(task.isSuccessful()){
                                                                                                progressDialog.dismiss();

                                                                                                if (document.exists()){
                                                                                                    Intent intent = new Intent(LoginPage.this, Driver.class);
                                                                                                    intent.putExtra("email", email);
                                                                                                    startActivity(intent);
                                                                                                }
                                                                                                else{
                                                                                                    Intent intent = new Intent(LoginPage.this, VehicleDetailsAdd.class);
                                                                                                    intent.putExtra("email", email);
                                                                                                    startActivity(intent);
                                                                                                }

                                                                                            }else{
                                                                                                progressDialog.dismiss();
                                                                                                Toast.makeText(LoginPage.this,"Error",Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        } else {
                                                                            progressDialog.dismiss();
                                                                            Intent intent = new Intent(LoginPage.this, Passenger.class);
                                                                            intent.putExtra("email", email);
                                                                            startActivity(intent);
                                                                        }

                                                                    } else {
                                                                        progressDialog.dismiss();

                                                                        Intent inent = new Intent(LoginPage.this, UserDetailsAdd.class);
                                                                        inent.putExtra("email", email);
                                                                        startActivity(inent);
                                                                    }

                                                                }else{
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(LoginPage.this,"Something Error",Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });

                                            }else{
                                                progressDialog.dismiss();
                                                alertDialog.setMessage("Please check your email. We`ve sent a confirmation to " + email +" to verify your email address");
                                                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                                alertDialog.show();
                                            }


                                        }else{
                                            progressDialog.dismiss();
                                            String msg = ((FirebaseAuthException) task.getException()).getMessage();
                                            alertDialog.setMessage(msg);
                                            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            alertDialog.show();
                                        }

                                    }
                                });

                    }else{
                        if(email.isEmpty()){
                            email_text.setError("Field cannot be left blank");
                        }
                        if(password.isEmpty()){
                            password_text.setError("Field cannot be left blank");
                        }
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

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iinent = new Intent(LoginPage.this, SignUp.class);
                startActivity(iinent);
            }
        });

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iinent = new Intent(LoginPage.this, ForgetPassword.class);
                startActivity(iinent);
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
}
