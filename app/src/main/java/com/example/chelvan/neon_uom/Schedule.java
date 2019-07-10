package com.example.chelvan.neon_uom;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Schedule extends AppCompatActivity {

    String bus_email,bus_current_loc;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SheduleData pd;
    Users2Adapter adapter;
    ListView listview;
    String routenum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction1);

        Intent intent = this.getIntent();
        bus_email = intent.getStringExtra("bus_email");
        bus_current_loc = intent.getStringExtra("bus_current");

        listview = findViewById(R.id.ListView);
        ArrayList<SheduleData> arrayOfUsers = new ArrayList<SheduleData>();
        adapter = new Schedule.Users2Adapter(this, arrayOfUsers);
        listview.setAdapter(adapter);

        if(bus_email!=null && bus_current_loc!=null) {

            db.collection(bus_email).document("vehicleDetails")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful())
                            {
                                DocumentSnapshot dc = task.getResult();

                                routenum = dc.get("route number").toString();

                                db.collection("Bus_Routes").document(routenum)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                DocumentSnapshot dc = task.getResult();

                                                HashMap<String, Object> hm = (HashMap<String, Object>) dc.getData();

                                                Map<String, Object> map = new TreeMap<>(hm);

                                                for (Map.Entry<String, Object> entry : map.entrySet()) {

                                                    PlaceTimePredict pt = new PlaceTimePredict();
                                                    pt.execute(entry.getValue().toString());

                                                }
                                            }
                                        });
                        }
                        }
                    });
        }

        else{
            Toast.makeText(Schedule.this,"No Services available.",Toast.LENGTH_SHORT).show();
        }
    }

    private class PlaceTimePredict extends AsyncTask<String,String,String>{

        String place;
        String inputline,value;
        URL urlobj;
        String[] values;
        String[] res;
        StringBuffer response;

        @Override
        protected String doInBackground(String... strings) {
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+bus_current_loc+"&destinations="+strings[0]+"&mode=driving&key=AIzaSyAa8hhm3_BvlthQ6ewuWiPQKWGEshB2yGA";
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

                values = response.toString().split(":");
                res = values[9].split(",");
                value = res[0];

            }

            catch(Exception e)
            { System.out.println("suthan "+e);}


            place = strings[0];
            return null;
        }


        @Override
        protected void onPostExecute(String r){

            pd = new SheduleData(place,value);
            adapter.add(pd);

        }
    }

    class Users2Adapter extends ArrayAdapter<SheduleData> {
        public Users2Adapter(Context context, ArrayList<SheduleData> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SheduleData user = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custome, parent, false);
            }

            TextView place = (TextView) convertView.findViewById(R.id.textView11);
            TextView time = (TextView) convertView.findViewById(R.id.textView14);


            place.setText(user.place);
            time.setText(user.time);

            return convertView;
        }
    }
}
