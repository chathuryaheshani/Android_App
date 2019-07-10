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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleDetailsAdd extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText vnum,vname,scount;
    Button submit;
    Spinner routenum;
    String vehicle_num,vehicle_name,route_num,seat_count="0",email;
    AlertDialog alertDialog;
    List<String> list;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details_add);

        vnum = findViewById(R.id.editText1);
        vname = findViewById(R.id.editText2);
        scount = findViewById(R.id.editText3);
        routenum = findViewById(R.id.spinner);
        submit = findViewById(R.id.button);

        alertDialog = new AlertDialog.Builder(VehicleDetailsAdd.this).create();
        alertDialog.setTitle("Neon");

        final Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        final ProgressDialog progressDialog = new ProgressDialog(VehicleDetailsAdd.this);
        progressDialog.setTitle("Saving...");

        if(isOnline()==true) {

            db.collection("Bus_Routes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                list  = new ArrayList<String>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    list.add(document.getId());
                                }
                                list.add("cannot find");
                                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(VehicleDetailsAdd.this, android.R.layout.simple_spinner_item, list);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                routenum.setAdapter(dataAdapter);

                            } else {
                                Toast.makeText(VehicleDetailsAdd.this,"Something Wrong",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(VehicleDetailsAdd.this,"oops your are disconnected...",Toast.LENGTH_SHORT).show();
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnline()==true){

                    vehicle_num = vnum.getText().toString();
                    vehicle_name = vname.getText().toString();
                    seat_count = scount.getText().toString();
                    route_num = routenum.getSelectedItem().toString();

                    if(!vehicle_num.isEmpty() && !route_num.isEmpty() && !seat_count.isEmpty() && Integer.parseInt(seat_count)<=70 &&Integer.parseInt(seat_count)>=10 ){
                        progressDialog.show();

                        Map<String, Object> user = new HashMap<>();
                        user.put("Vehicle NP", vehicle_num);
                        user.put("vehicle Name", vehicle_name);
                        user.put("route number", route_num);
                        user.put("scount", seat_count);

                        db.collection(email).document("vehicleDetails")
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(VehicleDetailsAdd.this, CheckPlaces.class);
                                        intent.putExtra("email", email);
                                        intent.putExtra("routenum", route_num);
                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(VehicleDetailsAdd.this, "something wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });


                        //Number Plate details
                        Map<String, Object> numberplate = new HashMap<>();
                        numberplate.put("userId",email);
                        db.collection("NumberPlates").document(vehicle_num)
                                .set(numberplate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });

                        //Status of vehicle
                        HashMap<String,Object> status = new HashMap<>();
                        status.put("status", "false");
                        db.collection(email).document("Available")
                                .set(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });


                        //Bus_Drivers details add
                        Map<String,Object> drivers = new HashMap<>();
                        drivers.put(randomAlphaNumeric(8),email);
                        db.collection("Bus_Drivers").document(route_num)
                                .set(drivers)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            System.out.println("Bus drivers id updated");
                                        }
                                    }
                                });
                    }

                    else{
                        if(vehicle_num.isEmpty()){
                            vnum.setError("Field cannot be left blank");
                        }

                        if(seat_count.isEmpty() ){

                            scount.setError("Field cannot be left blank");
                        }
                        if(!seat_count.isEmpty() && Integer.parseInt(seat_count)>70){

                            scount.setError("seat count must be less than 70");
                        }

                        if(!seat_count.isEmpty() && Integer.parseInt(seat_count)<10){

                            scount.setError("seat count must be greate than 10");
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


    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
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
