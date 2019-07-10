package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignUp extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;

    EditText email_text,password_text,varify_password;
    Button signup;

    String email,password,varifypassword;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email_text = findViewById(R.id.editText1);
        password_text = findViewById(R.id.editText2);
        varify_password = findViewById(R.id.editText3);
        signup = findViewById(R.id.button3);

        alertDialog = new AlertDialog.Builder(SignUp.this).create();
        alertDialog.setTitle("Neon");

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnline()==true){

                    email = email_text.getText().toString();
                    password = password_text.getText().toString();
                    varifypassword = varify_password.getText().toString();

                    if(!email.isEmpty() && !password.isEmpty() && !varifypassword.isEmpty()){

                        if(password_text.getText().toString().equals(varify_password.getText().toString())){

                            EmailCheck emailcheck = new EmailCheck();
                            emailcheck.execute();

                        }

                        else{
                            alertDialog.setMessage("Those passwords did not match");
                            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            alertDialog.show();
                        }

                    }

                    else{
                        if(email.isEmpty()){
                            email_text.setError("Field cannot be left blank");
                        }
                        if(password.isEmpty()){
                            password_text.setError("Field cannot be left blank");
                        }
                        if(varify_password.getText().toString().isEmpty()){
                            varify_password.setError("Field cannot be left blank");
                        }
                    }

                }

                else{
                    alertDialog.setMessage("No Internet Connection");
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
            }
        });


    }

    private class EmailCheck extends AsyncTask<String,String,String> {
        String url,value,inputline;
        URL urlobj;
        String[] values;
        String[] res;
        StringBuffer response;
        private String apikey = "786690cd5119f1579d0bd6028bfb56b8";
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... strings) {
            url = "http://apilayer.net/api/check?access_key="+apikey+"&email="+email+"&smtp=1&format=1";
            urlobj = null;
            try {
                urlobj = new URL(url);

            } catch (MalformedURLException e) {

            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) urlobj.openConnection();

            } catch (IOException e) {

            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            } catch (IOException e) {

            }

            response = new StringBuffer();

            try {
                while ((inputline = in.readLine()) != null) {
                    response.append(inputline);
                }

                values = response.toString().split(",");
                res = values[6].toString().split(":");
                value = res[1];
            }

            catch(Exception e)
            { }
            return value;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SignUp.this);
            progressDialog.setTitle("please wait");

            progressDialog.show();

        }

        @Override
        protected void onPostExecute(String s) {

            if(value.equals("true")){
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    user = mAuth.getCurrentUser();
                                    user.sendEmailVerification();

                                    alertDialog.setMessage("Please check your email. We`ve sent a confirmation to " + email +" to verify your email address");
                                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(SignUp.this, LoginPage.class);
                                            startActivity(i);
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
            }

            else{
                progressDialog.dismiss();
                alertDialog.setMessage("you may have entered an incorrect email address. Please correct it ");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        }

    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}
