package com.example.lm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class UpdateProfile extends AppCompatActivity {

    Intent intent;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    ListenerRegistration listenerRegistration;
    ValueEventListener profileListener;
    DatabaseReference profileReference;

    private ImageView mgetnewimageinimageview;
    private ImageButton mbackbuttonofupdateprofile;
    private EditText mnewusername;
    private TextView mcurrentStatus, muserPhone;
    private StorageReference storageReference;
    private androidx.appcompat.widget.Toolbar mtoolbarofupdateprofile;

    private Uri imagepath;
    public Boolean pathStatus;
    public String ImageURIacessToken;
    private static int PICK_IMAGE=123;

    ProgressBar mprogressbarofupdateprofile;
    android.widget.Button mupdateprofilebutton;
    String newname, oldname, currentStatus, userPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        setSupportActionBar(mtoolbarofupdateprofile);

        mtoolbarofupdateprofile=findViewById(R.id.toolbarofupdateprofile);
        mbackbuttonofupdateprofile=findViewById(R.id.backbuttonofupdateprofile);
        mgetnewimageinimageview=findViewById(R.id.getnewuserimageinimageview);
        mprogressbarofupdateprofile=findViewById(R.id.progressbarofupdateprofile);
        mnewusername=findViewById(R.id.getnewusername);
        mcurrentStatus=findViewById(R.id.currentStatus);
        muserPhone = findViewById(R.id.userPhone);
        mupdateprofilebutton=findViewById(R.id.updateprofilebutton);

        intent=getIntent();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        pathStatus = false;

        mbackbuttonofupdateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DocumentReference docRef = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        EventListener<DocumentSnapshot> eventListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (snapshot != null && snapshot.exists()) {
                    firebasemodel mfirebasemodel = snapshot.toObject(firebasemodel.class);
                    ImageURIacessToken = mfirebasemodel.getImage();
                    final Context  context = UpdateProfile.this;
                    if (isValidContextForGlide(context)){
                        Glide.with(UpdateProfile.this).load(ImageURIacessToken).centerCrop().into(mgetnewimageinimageview);
                    }
                }
            }
        };

        if (listenerRegistration == null ) {
//            Toast.makeText(getApplicationContext(),"listenerRegistration",Toast.LENGTH_SHORT).show();
            listenerRegistration = docRef.addSnapshotListener(eventListener);
        }

        profileReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        profileListener = profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userprofile muserprofile=snapshot.getValue(userprofile.class);
                oldname = muserprofile.getUsername();
                currentStatus = muserprofile.getUserStatus();
                userPhone = firebaseAuth.getCurrentUser().getPhoneNumber();
                mnewusername.setText(oldname);
                mcurrentStatus.setText(currentStatus);
                muserPhone.setText(userPhone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getApplicationContext(),"Не удалось получить",Toast.LENGTH_SHORT).show();
            }
        });

        mupdateprofilebutton.setVisibility(View.INVISIBLE);
        mnewusername.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().equals(oldname)) {
                    if (pathStatus){
                        mupdateprofilebutton.setVisibility(View.VISIBLE);
                    }
                    else {
                        mupdateprofilebutton.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    mupdateprofilebutton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());
        mupdateprofilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newname=mnewusername.getText().toString().trim().replaceAll(" +", " ");;
                if(newname.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Имя пустое",Toast.LENGTH_SHORT).show();
                }
                else
                {
//                    mprogressbarofupdateprofile.setVisibility(View.VISIBLE);

                    userprofile muserprofile = new userprofile(newname,firebaseAuth.getUid(),"Online","None");
                    databaseReference.setValue(muserprofile);

                    if(imagepath!=null) {
                        updateimagetostorage();
                    }
                    updatenameoncloudfirestore();

                    Toast.makeText(getApplicationContext(),"Обновление",Toast.LENGTH_SHORT).show();
//                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                    finish();
                    Toast.makeText(getApplicationContext(),"Профиль успешно обновлён",Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        mgetnewimageinimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE);

            }
        });

    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    private void updatenameoncloudfirestore() {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String , Object> userdata=new HashMap<>();
        userdata.put("name",newname);
        userdata.put("image",ImageURIacessToken);
        userdata.put("uid",firebaseAuth.getUid());
        documentReference.set(userdata);
    }

    private void updateimagetostorage() {
        storageReference=firebaseStorage.getReference();
        StorageReference imageref = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        Bitmap bitmap = null;
        try {
            bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data=byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = imageref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageURIacessToken=uri.toString();
//                        Toast.makeText(getApplicationContext(),"URI получен успешно",Toast.LENGTH_SHORT).show();
                        updatenameoncloudfirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(),"URI не получен",Toast.LENGTH_SHORT).show();
                    }


                });
//                Toast.makeText(getApplicationContext(),"Изображение обновлено",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),"Ошибка обновления изображения",Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK)
        {
            if (data != null) {
                imagepath=data.getData();
            }
            mgetnewimageinimageview.setImageURI(imagepath);
            pathStatus = true;
            mupdateprofilebutton.setVisibility(View.VISIBLE);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        listenerRegistration = docRef.addSnapshotListener(eventListener);
//    }


    @Override
    protected void onStop() {
        super.onStop();
        profileReference.removeEventListener(profileListener);
        if (listenerRegistration != null) {
//            Toast.makeText(getApplicationContext(),"listenerRegistration.remove();",Toast.LENGTH_SHORT).show();
            listenerRegistration.remove();
        }
    }

}