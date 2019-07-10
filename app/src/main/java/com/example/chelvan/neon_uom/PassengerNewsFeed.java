package com.example.chelvan.neon_uom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class PassengerNewsFeed extends AppCompatActivity {

    ListView listview;
    Users1Adapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HashMap<String,Object> hm;
    NewsFeedData newUser;
    String routenumber,date;
    EditText search_box;

    Button search;
    ImageButton previous;
    int pre = 0;
    boolean searchActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_news_feed);

        listview = findViewById(R.id.ListView);
        ArrayList<NewsFeedData> arrayOfUsers = new ArrayList<NewsFeedData>();
        adapter = new Users1Adapter(this, arrayOfUsers);
        listview.setAdapter(adapter);
        search_box = findViewById(R.id.editText6);
        search = findViewById(R.id.button7);

        previous = findViewById(R.id.button10);
        previous.setClickable(true);

        commonDataCollect();


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                routenumber = search_box.getText().toString();

                if(!routenumber.isEmpty()){
                    adapter.clear();
                    pre = 0;
                    specificDataCollect();
                    searchActive = true;
                }
                else{
                    search_box.setError("please give the route number");
                }
            }
        });


        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(pre>-6){

                    pre = pre-1;

                    if(searchActive==false){
                        commonDataCollect();
                    }

                    else{
                        specificDataCollect();
                    }
                }

                else{

                    Toast.makeText(PassengerNewsFeed.this, "Only last 7 days posts can be show", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void specificDataCollect(){

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_YEAR, pre);

            db.collection("posts").document(routenumber).collection(sdf.format(cal.getTime()))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {

                                QuerySnapshot doc = task.getResult();
                                if(doc.isEmpty()){

                                    Toast.makeText(PassengerNewsFeed.this, "No post available.", Toast.LENGTH_SHORT).show();
                                }

                                for (QueryDocumentSnapshot dc : task.getResult()) {

                                    hm = (HashMap<String, Object>) dc.getData();

                                    if(hm.get("title")!=null) {

                                        String title = hm.get("title").toString();
                                        String msg = hm.get("msg").toString();
                                        String time =  hm.get("time").toString();
                                        String rn = hm.get("routenum").toString();
                                        String name = hm.get("name").toString();
                                        String dates = hm.get("date").toString();

                                        newUser = new NewsFeedData(title,msg,"Route Number: "+rn,"Time: "+time,"Date: "+dates,"Driver's Name: "+name);
                                        adapter.add(newUser);

                                    }
                                }
                            }
                        }
                    });

    }

    private void commonDataCollect(){
        db.collection("Bus_Routes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()) {

                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                            Calendar cal = Calendar.getInstance();

                            cal.add(Calendar.DAY_OF_YEAR, pre);

                            for (QueryDocumentSnapshot dc : task.getResult()) {

                                db.collection("posts").document(dc.getId()).collection(sdf.format(cal.getTime()))
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                if (task.isSuccessful()) {

                                                    for (QueryDocumentSnapshot dc : task.getResult()) {

                                                        hm = (HashMap<String, Object>) dc.getData();

                                                        if(hm.get("title")!=null) {

                                                            String title = hm.get("title").toString();
                                                            String msg = hm.get("msg").toString();
                                                            String time =  hm.get("time").toString();
                                                            String rn = hm.get("routenum").toString();
                                                            String name = hm.get("name").toString();
                                                            String dates = hm.get("date").toString();

                                                            newUser = new NewsFeedData(title,msg,"Route Number: "+rn,"Time: "+time,"Date: "+dates,"Driver's Name: "+name);
                                                            adapter.add(newUser);

                                                        }

                                                    }
                                                }

                                            }
                                        });
                            }

                        }
                    }
                });

    }

    class Users1Adapter extends ArrayAdapter<NewsFeedData> {
        public Users1Adapter(Context context, ArrayList<NewsFeedData> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            NewsFeedData user = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.customlayout, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.textView1);
            TextView msg = (TextView) convertView.findViewById(R.id.textView2);
            TextView routenum = (TextView) convertView.findViewById(R.id.textView3);
            TextView time = (TextView) convertView.findViewById(R.id.textView4);
            TextView date = (TextView) convertView.findViewById(R.id.textView5);
            TextView name = (TextView) convertView.findViewById(R.id.textView6);

            title.setText(user.title);
            msg.setText(user.msg);
            routenum.setText(user.routenum);
            time.setText(user.time);
            date.setText(user.date);
            name.setText(user.name);

            return convertView;
        }
    }

}
