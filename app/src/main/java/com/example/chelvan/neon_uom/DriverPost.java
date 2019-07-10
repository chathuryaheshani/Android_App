package com.example.chelvan.neon_uom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DriverPost extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText title,msg;
    Button post;

    int count;
    String email,route_num,name;
    CharSequence date;
    Date d;

    Map<String, Object> count_map;
    Map<String, Object> data;

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_post);

        title = findViewById(R.id.editText8);
        msg = findViewById(R.id.editText7);

        post = findViewById(R.id.button5);

        final Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        final ProgressDialog progressDialog = new ProgressDialog(DriverPost.this);
        progressDialog.setTitle("Saving...");

        db.collection(email).document("vehicleDetails")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){
                            DocumentSnapshot dc = task.getResult();
                            route_num = dc.get("route number").toString();
                            name = dc.get("vehicle Name").toString();

                        }else{
                            Toast.makeText(DriverPost.this, "something error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        post.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                d = new Date();
                date  = DateFormat.format("MMMM dd, yyyy", d.getTime());

                if(!title.getText().toString().isEmpty()) {

                    progressDialog.show();

                    Calendar cal = Calendar.getInstance();

                    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat date = new SimpleDateFormat("MMMM dd, yyyy");

                    data = new HashMap<>();

                    data = new HashMap<>();
                    data.put("title", title.getText().toString());
                    data.put("msg", msg.getText().toString());
                    data.put("name",name);
                    data.put("time",time.format(cal.getTime()));
                    data.put("routenum",route_num);
                    data.put("date",date.format(cal.getTime()));
                    data.put("email",email);

                    db.collection("posts").document(route_num).collection(date.format(cal.getTime())).document(randomAlphaNumeric(20))
                            .set(data)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        progressDialog.dismiss();
                                        title.setText("");
                                        msg.setText("");
                                    }

                                }
                            });


                }else{
                    title.setError("Field cannot be left blank");
                }
            }
        });

    }


    public String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
