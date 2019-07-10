package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class ForgetPassword extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText mail;
    Button reset;
    AlertDialog alertDialog;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mail = findViewById(R.id.editText1);
        reset = findViewById(R.id.bt1);

        alertDialog = new AlertDialog.Builder(ForgetPassword.this).create();
        alertDialog.setTitle("Neon");

        final ProgressDialog progressDialog = new ProgressDialog(ForgetPassword.this);
        progressDialog.setTitle("Resetting...");

        mAuth = FirebaseAuth.getInstance();

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnline()==true){

                    email = mail.getText().toString();

                    if(!email.isEmpty()){

                        progressDialog.show();

                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();

                                            alertDialog.setMessage("Please check your email. We`ve sent a reset to " + email +" to reset your account");
                                            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            alertDialog.show();

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
                        mail.setError("Field cannot be left blank");
                    }

                }else{
                    alertDialog.setMessage("Make sure internet and restart app");
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
