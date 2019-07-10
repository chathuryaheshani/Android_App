package com.example.chelvan.neon_uom;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class performance extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    ImageView barchart,pichart;
    String email;
    File localFile_bar = null,localFile_pi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);

        barchart = findViewById(R.id.imageView7);
        pichart = findViewById(R.id.imageView8);

        Intent intent = this.getIntent();
        email = intent.getStringExtra("email");

        String barimg = email.split("\\.")[0]+"_bar.png";
        String piimg = email.split("\\.")[0]+"_pi.png";

        storageRef = storage.getReference();
        StorageReference barchartpathReference = storageRef.child("performance/barchart/"+barimg);
        StorageReference pichartpathReference = storageRef.child("performance/piechart/"+piimg);


        try {
            localFile_bar = File.createTempFile("images", "jpg");

            barchartpathReference.getFile(localFile_bar).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    String filePath = localFile_bar.getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    barchart.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    barchart.setImageResource(R.drawable.sorry);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            localFile_pi = File.createTempFile("images", "jpg");

            pichartpathReference.getFile(localFile_pi).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    String filePath = localFile_pi.getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    pichart.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    pichart.setImageResource(R.drawable.sorry);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
