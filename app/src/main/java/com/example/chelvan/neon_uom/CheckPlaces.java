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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CheckPlaces extends AppCompatActivity {
    FirebaseFirestore db;
    ListView listView;
    Button submit;
    String email,routenum;

    HashMap<String,Object> hm = new HashMap<String,Object>();
    AlertDialog alertDialog;
    Item item;
    Users2Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_places);

        listView =(ListView) findViewById(R.id.listview);
        submit = (Button) findViewById(R.id.printbtn);

        ArrayList<Item> arrayOfUsers = new ArrayList<Item>();
        adapter = new Users2Adapter(this, arrayOfUsers);
        listView.setAdapter(adapter);

        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");
        routenum = intent.getStringExtra("routenum");

        db = FirebaseFirestore.getInstance();
        alertDialog = new AlertDialog.Builder(CheckPlaces.this).create();
        alertDialog.setTitle("Neon");

        if(isOnline()==true) {

            final ProgressDialog progressDialog = new ProgressDialog(CheckPlaces.this);
            progressDialog.setTitle("Saving...");
            progressDialog.show();

            DocumentReference docRef = db.collection("Bus_Routes").document(routenum);
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();

                                if(document.exists()){
                                    progressDialog.dismiss();
                                    DocumentSnapshot doc = task.getResult();

                                    hm = (HashMap<String, Object>) doc.getData();

                                    Map<String, Object> map = new TreeMap<>(hm);

                                    for(Map.Entry m:map.entrySet()){

                                        item = new Item(m.getValue().toString());
                                        adapter.add(item);
                                    }

                                }

                                else{
                                    progressDialog.dismiss();
                                    alertDialog.setMessage("Please mail us your route number,we will add in system. until it  happen you cannot get service mail:neonmora34@gmail.com");
                                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    alertDialog.show();

                                    db.collection(email).document("vehicleDetails")
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                }

                            }

                            else{
                                progressDialog.dismiss();
                                Toast.makeText(CheckPlaces.this,"Something wrong...",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

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

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()==true){
                    if(!hm.isEmpty()){

                        Toast.makeText(CheckPlaces.this,"Welcome.",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CheckPlaces.this,Driver.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }

                    else{
                        Toast.makeText(CheckPlaces.this,"It is not available",Toast.LENGTH_SHORT).show();
                    }

                }

                else{
                    Toast.makeText(CheckPlaces.this,"oops your are disconnected...",Toast.LENGTH_SHORT).show();
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

    class Users2Adapter extends ArrayAdapter<Item> {
        public Users2Adapter(Context context, ArrayList<Item> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Item user = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            tvName.setText(user.name);

            return convertView;
        }
    }
}