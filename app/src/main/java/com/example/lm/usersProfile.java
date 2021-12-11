package com.example.lm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class usersProfile extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;

    DatabaseReference profileRef, connectedRef, userNameRef;
    StorageReference setUserImageRef;
    DocumentReference getUserImageRef;

    ListenerRegistration listenerRegistration;
    ValueEventListener connectedListener;
    EventListener<DocumentSnapshot> eventListener;

    private ImageView setUserImage;
    private EditText setUserName;
    private TextView currentUserStatus;
    private androidx.appcompat.widget.Toolbar toolbar;

    TextView currentUserPhone;
    ImageButton backButton;
    ProgressBar updateProgressBar;
    android.widget.Button updateUserProfileButton;

    private Uri imagePath;
    public Boolean pathStatus;
    public String ImageUriAccessToken, newUserName, currentUserName, userId;
    final Context  context = usersProfile.this;


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        imagePath = uri;
                        setUserImage.setImageURI(imagePath);
                        pathStatus = true;
                        if (setUserName.getText().toString().trim().length() != 0)
                            updateUserProfileButton.setVisibility(View.VISIBLE);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        setSupportActionBar(toolbar);

        toolbar = findViewById(R.id.toolbarofupdateprofile);
        backButton = findViewById(R.id.backbuttonofupdateprofile);
        setUserImage = findViewById(R.id.getnewuserimageinimageview);
        updateProgressBar = findViewById(R.id.progressbarofupdateprofile);
        setUserName = findViewById(R.id.getnewusername);
        currentUserStatus = findViewById(R.id.currentStatus);
        currentUserPhone = findViewById(R.id.userPhone);
        updateUserProfileButton = findViewById(R.id.updateprofilebutton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getUid();
        pathStatus = false;


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        connectedRef = firebaseDatabase.getReference(".info/connected");
        connectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    currentUserStatus.setText("в сети");
                } else {
                    currentUserStatus.setText("был(а) недавно");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        };


        getUserImageRef = firebaseFirestore.collection("Users").document(userId);
        eventListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (snapshot != null && snapshot.exists()) {
                    userFDModel _userFDModel = snapshot.toObject(userFDModel.class);
                    if (_userFDModel != null) {
                        ImageUriAccessToken = _userFDModel.getImage();
                    }
                    if (isValidContextForGlide(context)){
                        Glide.with(context).load(ImageUriAccessToken).centerCrop().into(setUserImage);
                    }
                }
            }
        };

        if (firebaseAuth.getCurrentUser() != null)
            currentUserPhone.setText(firebaseAuth.getCurrentUser().getPhoneNumber());

        profileRef = firebaseDatabase.getReference(userId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRDModel _userRDModel = snapshot.getValue(userRDModel.class);
                if (_userRDModel != null){
                    currentUserName = _userRDModel.getUserName();
                    setUserName.setText(currentUserName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getApplicationContext(),"Не удалось получить",Toast.LENGTH_SHORT).show();
            }
        });


        setUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().equals(currentUserName)) {
                    if (pathStatus)
                        updateUserProfileButton.setVisibility(View.VISIBLE);
                    else
                        updateUserProfileButton.setVisibility(View.INVISIBLE);
                }
                else if (s.toString().trim().length() == 0){
                    updateUserProfileButton.setVisibility(View.INVISIBLE);
                }
                else{
                    updateUserProfileButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        userNameRef = firebaseDatabase.getReference().child(userId).child("userName");
        updateUserProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newUserName = setUserName.getText().toString().trim().replaceAll(" +", " ");

                if(newUserName.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Имя пустое",Toast.LENGTH_SHORT).show();
                }
                else
                {
//                    updateProgressBar.setVisibility(View.VISIBLE);
                    userNameRef.setValue(newUserName);
                    if(imagePath != null)
                        uploadImageToStorage();
                    updateDataOnCloudFirestore();

                    Toast.makeText(getApplicationContext(),"Обновление",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),"Профиль успешно обновлён",Toast.LENGTH_SHORT).show();
//                    updateProgressBar.setVisibility(View.INVISIBLE);
                    finish();
                }
            }
        });


        setUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });

    }


    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }


    private void updateDataOnCloudFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(userId);
        Map<String , Object> userdata = new HashMap<>();
        userdata.put("name", newUserName);
        userdata.put("image", ImageUriAccessToken);
        userdata.put("uid", userId);
        documentReference.set(userdata);
    }


    private void uploadImageToStorage() {
        setUserImageRef = firebaseStorage.getReference().child("Images").child(userId).child("Profile Pic");

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();

        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        }
        byte[] data=byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = setUserImageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                setUserImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken=uri.toString();
                        updateDataOnCloudFirestore();
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


    @Override
    protected void onStart() {
        super.onStart();
        if (listenerRegistration == null){
            listenerRegistration = getUserImageRef.addSnapshotListener(eventListener);
        }
        connectedRef.addValueEventListener(connectedListener);
//        Toast.makeText(getApplicationContext(),"usersProfile - onStart",Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        connectedRef.removeEventListener(connectedListener);
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
//        Toast.makeText(getApplicationContext(),"usersProfile - onStop",Toast.LENGTH_SHORT).show();
    }

}