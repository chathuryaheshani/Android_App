package com.example.chelvan.neon_uom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Driver extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback {

    FirebaseFirestore db;
    FirebaseStorage storage;
    DocumentReference docRef;

    private GoogleMap mMap;
    Marker marker;

    TextView headName, headEmail,spped;
    Button start, stop;
    ImageView profilepic;
    Toolbar toolbar;

    LocationManager locationManager;
    LocationListener locationListener;

    String email, firstname, profile_link, trip;
    int start_click = 0;
    double lat, lon;

    Map<String, Object> coordinates = new HashMap<>();
    Map<String, Object> current_location = new HashMap<>();
    Map<String, Object> status = new HashMap<>();

    Date d;
    CharSequence date, time;

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
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
        headName = (TextView) header.findViewById(R.id.headtext);
        headEmail = (TextView) header.findViewById(R.id.headmail);
        profilepic = (ImageView) header.findViewById(R.id.imageView);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        spped = findViewById(R.id.textView7);

        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");
        headEmail.setText(email);

        start.setBackgroundColor(Color.GRAY);
        stop.setBackgroundColor(Color.GRAY);

        db = FirebaseFirestore.getInstance();
        docRef = db.collection(email).document("userDetails");

        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        firstname = doc.get("fname").toString();
                        toolbar.setTitle(firstname+"-Driver");

                        try {
                            profile_link = doc.get("profile").toString();
                        }
                        catch(Exception e){

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
                                Toast.makeText(Driver.this, "Error" + e, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                lat = location.getLatitude();
                lon = location.getLongitude();

                animateCamera(location);
                showMarker(location);

                d = new Date();
                time = DateFormat.format("HH:mm:ss", d.getTime());
                coordinates.put(time.toString(),String.valueOf(lon)+"@"+String.valueOf(lat));

                db.collection(email).document("Trip").collection(date.toString()).document(trip)
                        .set(coordinates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });

                current_location.put("gps",String.valueOf(lon)+"@"+String.valueOf(lat));
                db.collection(email).document("current_location")
                        .set(current_location).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

                spped.setText("Speed: "+location.getSpeed()+" m/s");

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                showAlertBox();
            }
        };


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_click == 0) {

                    if (ContextCompat.checkSelfPermission(Driver.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Driver.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }

                    else {
                        Toast.makeText(Driver.this, "Trip started. Do not close app without Stop", Toast.LENGTH_SHORT).show();
                        start.setBackgroundColor(Color.GREEN);
                        stop.setBackgroundColor(Color.GRAY);
                        start_click = 1;

                        status.put("status", "true");
                        db.collection(email).document("Available")
                                .set(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });

                        d = new Date();
                        date = DateFormat.format("MMMM d, yyyy", d.getTime());

                        if(marker!=null){
                            marker.remove();
                            marker = null;
                        }

                        trip = randomAlphaNumeric(15);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);

                    }
                }

                else {
                    Toast.makeText(Driver.this, "please stop this trip first.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_click == 1) {

                        spped.setText("Speed: ");
                        Toast.makeText(Driver.this, "Your trip is finished.", Toast.LENGTH_SHORT).show();
                        start.setBackgroundColor(Color.GRAY);
                        stop.setBackgroundColor(Color.RED);
                        start_click = 0;

                        locationManager.removeUpdates(locationListener);

                        status.put("status", "false");
                        db.collection(email).document("Available")
                                .set(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });

                        if(marker!=null){
                            marker.remove();
                            marker = null;
                        }
                }

                else {
                    Toast.makeText(Driver.this, "please start this trip first.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void showAlertBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Driver.this);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(Driver.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driver.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
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

    private void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon)).position(latLng));
        } else {
            com.example.chelvan.neon_uom.MarkerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Spherical());
        }
    }


    public  String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
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
        getMenuInflater().inflate(R.menu.driver, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.performance) {
            intent = new Intent(Driver.this, performance.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if (id == R.id.feedback) {
            intent = new Intent(Driver.this, DriverFeedback.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if (id == R.id.newsfeed) {
            intent = new Intent(Driver.this, DriverPost.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if (id == R.id.setting) {
            intent = new Intent(Driver.this, ProfileHandling.class);
            intent.putExtra("email", email);
            startActivity(intent);

        } else if(id==R.id.vehicle){
            intent = new Intent(Driver.this, VehicleDetailsChange.class);
            intent.putExtra("email", email);
            startActivity(intent);

        }else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(Driver.this, "Logout", Toast.LENGTH_SHORT).show();
            intent = new Intent(Driver.this, LoginPage.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
