package com.example.chelvan.neon_uom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DriverFeedback extends AppCompatActivity {

    FirebaseFirestore db;
    DocumentReference docRef;

    Button analysis, clear;
    ListView listView;
    FeedbackData newUser;

    String email, vehicle_num, complient;
    HashMap<String, Object> hm = new HashMap<String, Object>();
    int tot_star = 0, feedback_count, com_count = 0, sa_count = 0, clean_count = 0, quality_count = 0, friendly_count = 0;
    UsersAdapter adapter;

    AlertDialog alertDialog;
    String[] data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_feedback);

        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        listView = findViewById(R.id.listview);
        analysis = findViewById(R.id.button9);
        clear = findViewById(R.id.button8);

        ArrayList<FeedbackData> arrayOfUsers = new ArrayList<FeedbackData>();
        adapter = new UsersAdapter(this, arrayOfUsers);
        listView.setAdapter(adapter);

        alertDialog = new AlertDialog.Builder(DriverFeedback.this).create();
        alertDialog.setTitle("Neon");

        //Vehicle number get
        db = FirebaseFirestore.getInstance();
        docRef = db.collection(email).document("vehicleDetails");
        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                vehicle_num = doc.get("Vehicle NP").toString();
                                setWidget(vehicle_num);
                            }
                        }
                    }
                });

        //Analysis of feedback
        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    int value_rate = (100 / (feedback_count * 5)) * tot_star;
                    int value_comportable = (100 / feedback_count) * com_count;
                    int value_clean = (100 / feedback_count) * clean_count;
                    int value_friendly = (100 / feedback_count) * friendly_count;
                    int value_quality = (100 / feedback_count) * quality_count;
                    int value_safety = (100 / feedback_count) * sa_count;

                    alertDialog.setMessage("Rating: "+String.valueOf(value_rate)+"%\n"+
                            "Comportable: "+String.valueOf(value_comportable)+"%\n"+
                            "Clean: "+String.valueOf(value_clean)+"%\n"+
                            "Friendly: "+String.valueOf(value_friendly)+"%\n"+
                            "Quality: "+String.valueOf(value_quality)+"%\n"+
                            "Safety: "+String.valueOf(value_safety)+"%\n"
                    );
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();

                }
                catch(Exception e){
                    Toast.makeText(DriverFeedback.this,"You have no data",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Cleat of button
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Feedback").document(vehicle_num)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                listView.setAdapter(null);
                                feedback_count = 0;
                            }
                        });
            }
        });

    }

    public void setWidget(String vnum) {
        //Feedback data get
        docRef = db.collection("Feedback").document(vehicle_num);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        DocumentSnapshot doc = task.getResult();
                        hm = (HashMap<String, Object>) doc.getData();

                        for (Map.Entry m : hm.entrySet()) {
                            String date = m.getKey().toString();
                            String[] res = m.getValue().toString().split(",");

                            char[] strArray = date.toCharArray();
                            date = ""+strArray[0]+strArray[1]+strArray[2]+strArray[3]+"-"+strArray[4]+strArray[5]+"-"+strArray[6]+strArray[7];

                            String star = res[0];
                            tot_star = Integer.parseInt(star) + tot_star;
                            feedback_count = feedback_count + 1;

                            try {
                                data = res[1].split("@");
                                complient = Arrays.toString(res[1].split("@"));
                                complient = complient.replace("[", "");
                                complient = complient.replace("]", "");

                                if (ArrayUtils.contains(data, "comportable")) {
                                    com_count = com_count + 1;
                                }
                                if (ArrayUtils.contains(data, "clean")) {
                                    clean_count = clean_count + 1;
                                }
                                if (ArrayUtils.contains(data, "friendly")) {
                                    friendly_count = friendly_count + 1;
                                }
                                if (ArrayUtils.contains(data, "safety")) {
                                    sa_count = sa_count + 1;
                                }
                                if (ArrayUtils.contains(data, "quality")) {
                                    quality_count = quality_count + 1;
                                }
                            } catch (Exception w) {
                                complient = "Nothing";
                            }

                            newUser = new FeedbackData("Rating: " + star + "/5", "problems: " + complient,"Date: "+date);
                            adapter.add(newUser);
                        }

                    } else {
                        Toast.makeText(DriverFeedback.this, "No result", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    class UsersAdapter extends ArrayAdapter<FeedbackData> {
        public UsersAdapter(Context context, ArrayList<FeedbackData> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            FeedbackData user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feedbackdata, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
            TextView tvdate = (TextView) convertView.findViewById(R.id.textView16) ;
            // Populate the data into the template view using the data object
            tvName.setText(user.points);
            tvHome.setText(user.compliments);
            tvdate.setText(user.date);
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
