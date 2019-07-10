package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassengerFeedback extends AppCompatActivity {

    TextView routenum,drivername;
    Spinner vehiclenum;
    RatingBar ratingBar;
    Button submit,comport,clean,friendly,safety,quality;

    int co,cl,fr,sa,qu=0,count;
    boolean cov,clv,frv,sav,quv=false;

    String email,problem = "",driveremail;
    AlertDialog alertDialog;

    Map<String, Object> user;
    HashMap<String,Object> hm = new HashMap<String,Object>();
    FirebaseFirestore db;
    Date d;

    List<String> list;
    DocumentSnapshot doc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_feedback);

        vehiclenum = findViewById(R.id.spinner3);
        ratingBar = findViewById(R.id.ratingBar);
        submit = findViewById(R.id.button6);

        comport = findViewById(R.id.b1);
        clean = findViewById(R.id.b2);
        friendly = findViewById(R.id.b3);
        safety = findViewById(R.id.b4);
        quality = findViewById(R.id.b5);
        routenum = findViewById(R.id.textView4);
        drivername = findViewById(R.id.textView5);

        comport.setBackgroundColor(Color.GRAY);
        clean.setBackgroundColor(Color.GRAY);
        friendly.setBackgroundColor(Color.GRAY);
        safety.setBackgroundColor(Color.GRAY);
        quality.setBackgroundColor(Color.GRAY);

        db = FirebaseFirestore.getInstance();

        Intent i = this.getIntent();
        email = i.getStringExtra("email");

        alertDialog = new AlertDialog.Builder(PassengerFeedback.this).create();
        alertDialog.setTitle("Neon");

        vehiclenum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                db.collection("NumberPlates").document((String) parent.getItemAtPosition(position))
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){
                            doc = task.getResult();

                            hm = (HashMap<String, Object>) doc.getData();
                            for(Map.Entry m:hm.entrySet()){
                                driveremail = m.getValue().toString();
                            }

                            db.collection(driveremail).document("userDetails")
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if(task.isSuccessful()){
                                        doc = task.getResult();

                                        if(doc.exists()){
                                            String firstname = doc.get("fname").toString();
                                            drivername.setText("Driver's Name: "+firstname);
                                        }


                                    } else{
                                        Toast.makeText(PassengerFeedback.this,"something error",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            db.collection(driveremail).document("vehicleDetails")
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if(task.isSuccessful()){
                                        doc = task.getResult();

                                        if(doc.exists()){
                                            String routenumber = doc.get("route number").toString();
                                            routenum.setText("Route Number:"+routenumber);
                                        }


                                    }else{
                                        Toast.makeText(PassengerFeedback.this,"something error",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }else{
                            Toast.makeText(PassengerFeedback.this,"something wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        db.collection("NumberPlates")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        list  = new ArrayList<String>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            list.add(document.getId());
                        }
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(PassengerFeedback.this, android.R.layout.simple_spinner_item, list);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        vehiclenum.setAdapter(dataAdapter);
                    }
                });



        comport.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(co%2==0) {
                        comport.setBackgroundColor(Color.RED);
                        cov = true;
                    }
                    else{
                        comport.setBackgroundColor(Color.GRAY);
                        cov = false;
                    }
                    co++;
                }
                return false;
            }
        });

        clean.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(cl%2==0) {
                        clean.setBackgroundColor(Color.RED);
                        clv = true;
                    }
                    else{
                        clean.setBackgroundColor(Color.GRAY);
                        clv = false;
                    }
                    cl++;
                }
                return false;
            }
        });

        friendly.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(fr%2==0) {
                        friendly.setBackgroundColor(Color.RED);
                        frv = true;
                    }
                    else{
                        friendly.setBackgroundColor(Color.GRAY);
                        frv = false;
                    }
                    fr++;
                }
                return false;
            }
        });

        safety.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(sa%2==0) {
                        safety.setBackgroundColor(Color.RED);
                        sav = true;
                    }
                    else{
                        safety.setBackgroundColor(Color.GRAY);
                        sav = false;
                    }
                    sa++;
                }
                return false;
            }
        });

        quality.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(qu%2==0) {
                        quality.setBackgroundColor(Color.RED);
                        quv = true;
                    }
                    else{
                        quality.setBackgroundColor(Color.GRAY);
                        quv = false;
                    }
                    qu++;
                }
                return false;
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String vnum = vehiclenum.getSelectedItem().toString();
                int rate = (int) ratingBar.getRating();

                final ProgressDialog progressDialog = new ProgressDialog(PassengerFeedback.this);
                progressDialog.setTitle("Updating...");

                if(cov==true){
                    problem = problem+"comportable@";
                }

                if(clv==true){
                    problem = problem+"clean@";
                }
                if(frv==true){
                    problem = problem+"friendly@";
                }

                if(sav==true){
                    problem = problem+"safety@";
                }

                if(quv==true){
                    problem = problem+"quality@";
                }


                d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentDateandTime = sdf.format(d.getTime());

                user = new HashMap<>();
                user.put(currentDateandTime, String.valueOf(rate)+","+problem);

                if(isOnline()==true){

                        progressDialog.show();

                        db.collection("Feedback").document(vnum)
                                .set(user,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    progressDialog.dismiss();

                                    alertDialog.setMessage("Thank you, Your feedback is updated");
                                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            user.clear();
                                            PassengerFeedback.this.finish();
                                        }
                                    });

                                    alertDialog.show();

                                }

                            }
                        });


                }else {
                    Toast.makeText(PassengerFeedback.this,"oops your are disconnected.",Toast.LENGTH_SHORT).show();
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
