package com.example.chelvan.neon_uom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Passenger extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    GoogleMap mMap;
    FirebaseFirestore db;
    FirebaseStorage storage;
    DocumentReference docRef;

    TextView headName, headEmail;
    ImageView profilepic;
    EditText searchbox;
    ImageButton search;
    Toolbar toolbar;
    TextView arrival;

    String email, firstname, profile_link, search_num;
    int hash_map_size,min=5;
    boolean notificationGot = false;

    String bus_loct, per_loct, bus_email;
    Marker pmarker, dmarker = null;
    LatLng bus_latLng, per_latLng;
    LocationListener locationListener;
    LocationManager locationManager;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        headName = (TextView) header.findViewById(R.id.htext);
        headEmail = (TextView) header.findViewById(R.id.hmail);
        profilepic = (ImageView) header.findViewById(R.id.imageView);
        arrival = findViewById(R.id.textView12);

        searchbox = findViewById(R.id.editText5);
        search = findViewById(R.id.imgeButtona);

        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");
        headEmail.setText(email);

        db = FirebaseFirestore.getInstance();
        docRef = db.collection(email).document("userDetails");

        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        firstname = doc.get("fname").toString();
                        toolbar.setTitle(firstname + "-Passenger");
                        try {
                            profile_link = doc.get("profile").toString();
                        } catch (Exception e) {

                        }

                        headName.setText(firstname);

                        if (profile_link != null) {
                            storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReferenceFromUrl(profile_link).child("images/" + email);

                            try {
                                final File localFile = File.createTempFile("images", "png");
                                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                        profilepic.setImageBitmap(bitmap);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(Passenger.this, "Error " + e, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });


        if (ContextCompat.checkSelfPermission(Passenger.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Passenger.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {

                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    per_loct = lat + "," + lon;
                    per_latLng = new LatLng(lat, lon);

                    pmarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.person)).position(per_latLng));

                    locationManager.removeUpdates(locationListener);

                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                    showAlertBox();
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                search_num = searchbox.getText().toString();
                bus_email = null;
                arrival.setText("Waiting...");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(arrival.getText().toString().equals("Waiting...")){
                            arrival.setText("No Services available.");
                        }
                    }
                }, 4000);

                if (!search_num.isEmpty()) {

                    if(dmarker!=null){
                        dmarker.remove();
                        dmarker = null;
                    }

                    db.collection("Bus_Drivers").document(search_num)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot doc = task.getResult();
                                    try {
                                        HashMap<String, Object> drivers = (HashMap<String, Object>) doc.getData();
                                        hash_map_size = drivers.size();

                                        for (final Map.Entry<String, Object> entry : drivers.entrySet()) {
                                            db.collection(entry.getValue().toString()).document("Available")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                            DocumentSnapshot doc = task.getResult();

                                                            String status = doc.get("status").toString();
                                                            if (status.equals("true")) {

                                                                bus_email = entry.getValue().toString();
                                                                RealLocation(bus_email);
                                                            }
                                                        }
                                                    });
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                            });
                } else {
                    searchbox.setError("please input route number");
                }
            }
        });

    }

    private void RealLocation(final String email) {

        db.collection(email).document("current_location")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        String string = snapshot.get("gps").toString();
                        String[] location = string.split("@");
                        double longi = Double.parseDouble(location[0]);
                        double lat = Double.parseDouble(location[1]);

                        bus_loct = location[1] + "," + location[0];
                        bus_latLng = new LatLng(lat, longi);

                        Location loc = new Location("");
                        loc.setLatitude(lat);
                        loc.setLongitude(longi);

                        animateCamera(loc);
                        markerDriver(loc);

                        Predict predict = new Predict();
                        predict.execute();
                    }

                });
    }


    private class Predict extends AsyncTask<String, String, String> {
        String inputline, value = "Not updated";
        URL urlobj;
        String[] values;
        String[] res;
        StringBuffer response;

        @Override
        protected String doInBackground(String... strings) {

            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + per_loct + "&destinations=" + bus_loct + "&mode=driving&key=AIzaSyAa8hhm3_BvlthQ6ewuWiPQKWGEshB2yGA";
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

            } catch (Exception e) {
            }

            return value;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String r) {

            if (value.equals("Not updated")) {

                Toast.makeText(Passenger.this, "Please wait until receiving your location.", Toast.LENGTH_SHORT).show();
            }

            arrival.setText("Arrival time is: " + value);

            try {
                min = Integer.parseInt(value.split("\"")[1].split(" ")[0]);
            }

            catch (Exception e) {

            }


            if (min <= 1 && !notificationGot) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Passenger.this, "")
                        .setSmallIcon(R.drawable.notify)
                        .setContentTitle("Alert!")
                        .setContentText("Bus is coming near.")
                        .setAutoCancel(true)
                        .setDefaults(Notification.BADGE_ICON_LARGE)
                        .setPriority(Notification.PRIORITY_HIGH);

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
                mBuilder.setAutoCancel(true);
                notificationGot = true;
            }

            if(min >1){

                notificationGot = false;
            }

        }
    }


    public void showAlertBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Passenger.this);
        builder.setMessage("To continue, turn on device location, which uses GPS location services.");

        AlertDialog dialog = builder.create();

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("NO THANKS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();

        dialog.setCanceledOnTouchOutside(true);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(Passenger.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Passenger.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(16)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

    }

    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }

    private void markerDriver(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (dmarker == null) {
            dmarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon)).position(latLng));
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
            getCameraPositionWithBearing(latLng);

        } else {
            com.example.chelvan.neon_uom.MarkerAnimation.animateMarkerToGB(dmarker, latLng, new LatLngInterpolator.Spherical());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.passenger, menu);
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.prediction) {
            Intent intent = new Intent(Passenger.this, Schedule.class);
            intent.putExtra("email", email);
            intent.putExtra("bus_email", bus_email);
            intent.putExtra("bus_current", bus_loct);
            startActivity(intent);

        } else if (id == R.id.feedback) {
            Intent intent = new Intent(Passenger.this, PassengerFeedback.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if (id == R.id.setting) {
            Intent intent = new Intent(Passenger.this, ProfileHandling.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if (id == R.id.newsfeed) {
            Intent intent = new Intent(Passenger.this, PassengerNewsFeed.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(Passenger.this, "Logout", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Passenger.this, LoginPage.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}