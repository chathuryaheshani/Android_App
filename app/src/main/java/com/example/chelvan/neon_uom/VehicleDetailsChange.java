package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleDetailsChange extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    EditText vName,nPlate,stCount;
    String vehicleName,numberPlateNumber,seatCount,email,optiondata,oldvnumplate,oldbusroute,old_driver_pos;
    Spinner option,routenum;
    List<String> list;
    Button save;
    int i;
    int pos;

    ProgressDialog progressDialog;
    AlertDialog alertDialog;

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details_change);

        nPlate=findViewById(R.id.e1);
        vName=findViewById(R.id.e2);
        stCount=findViewById(R.id.e3);
        routenum = findViewById(R.id.spinner);
        save=findViewById(R.id.button);

        nPlate.setEnabled(false);
        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        progressDialog = new ProgressDialog(VehicleDetailsChange.this);
        progressDialog.setTitle("Saving...");

        alertDialog = new AlertDialog.Builder(VehicleDetailsChange.this).create();
        alertDialog.setTitle("Neon");

        if(isOnline()==true){

            docRef = db.collection(email).document("vehicleDetails");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();
                        if(doc.exists()){
                            vehicleName = doc.get("vehicle Name").toString();
                            numberPlateNumber = doc.get("Vehicle NP").toString();
                            seatCount = doc.get("scount").toString();

                            vName.setText(vehicleName);
                            nPlate.setText(numberPlateNumber);
                            stCount.setText(seatCount);

                            oldvnumplate = numberPlateNumber;
                            oldbusroute = doc.get("route number").toString();

            db.collection("Bus_Routes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                list  = new ArrayList<String>();

                                i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    list.add(document.getId());

                                    if(oldbusroute.equals(document.getId())){
                                       pos = i;
                                    }

                                    i=i+1;
                                }

                                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(VehicleDetailsChange.this, android.R.layout.simple_spinner_item, list);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                routenum.setAdapter(dataAdapter);

                                routenum.setSelection(pos);

                            }
                        }
                    });


                        }
                    }
                }
            });


        }else {
            Toast.makeText(VehicleDetailsChange.this,"Oops you are disconnected!",Toast.LENGTH_SHORT).show();
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                if(isOnline()==true){

                    HashMap<String,Object> vehicledetails = new HashMap<>();
                    vehicledetails.put("Vehicle NP",nPlate.getText().toString());
                    vehicledetails.put("route number",routenum.getSelectedItem().toString());
                    vehicledetails.put("scount",stCount.getText().toString());
                    vehicledetails.put("vehicle Name",vName.getText().toString());

                    db.collection(email).document("vehicleDetails")
                            .set(vehicledetails)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                }
                            });

                    HashMap<String,Object> numberplate =  new HashMap<>();
                    numberplate.put("userId",email);


                    //Old Bus_Route delete
                    db.collection("Bus_Drivers").document(oldbusroute)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    DocumentSnapshot doc = task.getResult();
                                    HashMap<String,Object> hs = (HashMap<String, Object>) doc.getData();
                                    for ( Map.Entry<String, Object> entry : hs.entrySet()) {
                                        String key = entry.getKey();
                                        String tab = entry.getValue().toString();

                                        if(tab.equals(email)){
                                            old_driver_pos = key;
                                        }
                                    }

                                    DocumentReference docRef = db.collection("Bus_Drivers").document(oldbusroute);
                                    HashMap<String,Object> up = new HashMap<>();
                                    up.put(old_driver_pos, FieldValue.delete());

                                    docRef.update(up).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                                }
                            });


                    //New Bus_Route add
                    db.collection("Bus_Drivers").document(routenum.getSelectedItem().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    HashMap<String,Object> data = new HashMap<>();
                                    data.put(randomAlphaNumeric(10),email);

                                    db.collection("Bus_Drivers").document(routenum.getSelectedItem().toString())
                                            .set(data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                }
                            });


                }else{
                    Toast.makeText(VehicleDetailsChange.this,"Opps your are disconnected...",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
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
