package com.example.lm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class setUsersProfile extends AppCompatActivity {

    private ImageView setUserImage;
    private Uri imagePath;
    private EditText setUserName;
    Button saveUserProfileButton;
    ProgressBar loadingProgressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;

    DatabaseReference profileRDRef;
    StorageReference setUserImageRef;
    DocumentReference profileFDRef;

    private String userName, imageUriAccessToken, userId;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        imagePath = uri;
                        setUserImage.setImageURI(imagePath);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();

        setUserName = findViewById(R.id.getusername);
        setUserImage = findViewById(R.id.getuserimageinimageview);
        saveUserProfileButton = findViewById(R.id.saveProfile);
        loadingProgressBar = findViewById(R.id.progressbarofsetProfile);


        if (firebaseAuth.getCurrentUser() != null){
            setUserName.setText(firebaseAuth.getCurrentUser().getPhoneNumber());
            setDefaultProfile();
        }


        setUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });


        setUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveUserProfileButton.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        saveUserProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = setUserName.getText().toString();
                if(userName.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Имя пустое",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(imagePath == null)
                    {
                        String pkgName = getApplicationContext().getPackageName();
                        imagePath = Uri.parse("android.resource://"+pkgName+"/" + R.drawable.defprof);
                    }

                    loadingProgressBar.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    loadingProgressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(setUsersProfile.this, com.example.lm.chatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    private void setDefaultProfile(){
        String pkgName = getApplicationContext().getPackageName();
        imagePath = Uri.parse("android.resource://"+pkgName+"/" + R.drawable.defprof);
        sendDataForNewUser();
        imagePath = null;
    }


    private void sendDataForNewUser()
    {
        sendDataToRealtimeDatabase();
    }


    private void sendDataToRealtimeDatabase()
    {
        Date date = new Date();
        userName = setUserName.getText().toString().trim();
        userRDModel _userRDModel = new userRDModel(userName, firebaseAuth.getUid(), "Online", "None", date.getTime());

        profileRDRef = firebaseDatabase.getReference(userId);
        profileRDRef.setValue(_userRDModel);
        sendImageToStorage();
    }


    private void sendImageToStorage()
    {
        setUserImageRef = firebaseStorage.getReference().child("Images").child(userId).child("Profile Pic");

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        }
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = setUserImageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                setUserImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        sendDataToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private void sendDataToCloudFirestore() {
        profileFDRef = firebaseFirestore.collection("Users").document(userId);

        Map<String , Object> userdata=new HashMap<>();
        userdata.put("name", userName);
        userdata.put("image", imageUriAccessToken);
        userdata.put("uid", userId);

        profileFDRef.set(userdata);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}