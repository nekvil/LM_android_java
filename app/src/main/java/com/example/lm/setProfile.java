package com.example.lm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import java.util.HashMap;
import java.util.Map;

public class setProfile extends AppCompatActivity {

    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private Uri imagepath;
    private EditText mgetusername;
    private android.widget.Button msaveprofile;
    private static int PICK_IMAGE=123;

    ProgressBar mprogressbarofsetprofile;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private String name, ImageUriAcessToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        mgetusername=findViewById(R.id.getusername);
        mgetuserimage=findViewById(R.id.getuserimage);
        mgetuserimageinimageview=findViewById(R.id.getuserimageinimageview);
        msaveprofile=findViewById(R.id.saveProfile);
        mprogressbarofsetprofile=findViewById(R.id.progressbarofsetProfile);

        mgetusername.setText(firebaseAuth.getCurrentUser().getPhoneNumber());
        setDefaultProfile();

        mgetuserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE);
            }
        });


        msaveprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=mgetusername.getText().toString();
                if(name.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Имя пустое",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(imagepath==null)
                    {
                        String pkgName = getApplicationContext().getPackageName();
                        Uri path = Uri.parse("android.resource://"+pkgName+"/" + R.drawable.defprof);
                        imagepath = path;
                        Toast.makeText(getApplicationContext(),"Установлено изборжание по умолчанию",Toast.LENGTH_SHORT).show();
                    }

                    mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    Toast.makeText(getApplicationContext(),"Пользователь успешно зарегистрирован",Toast.LENGTH_SHORT).show();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);

                    Intent intent=new Intent(setProfile.this, com.example.lm.chatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    private void setDefaultProfile(){
        String pkgName = getApplicationContext().getPackageName();
        Uri path = Uri.parse("android.resource://"+pkgName+"/" + R.drawable.defprof);
        imagepath = path;
        sendDataForNewUser();
        imagepath = null;
    }


    private void sendDataForNewUser()
    {
        sendDataToRealTimeDatabase();
    }


    private void sendDataToRealTimeDatabase()
    {
        name=mgetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());
        userprofile muserprofile=new userprofile(name,firebaseAuth.getUid(),"Offline","None");
        databaseReference.setValue(muserprofile);
        sendImageToStorage();
    }


    private void sendImageToStorage()
    {

        StorageReference imageref=storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        Bitmap bitmap=null;
        try {
            bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data=byteArrayOutputStream.toByteArray();

        ///putting image to storage

        UploadTask uploadTask=imageref.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAcessToken=uri.toString();
//                        Toast.makeText(getApplicationContext(),"URI получен успешно",Toast.LENGTH_SHORT).show();
                        sendDataTocloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"URI не получен",Toast.LENGTH_SHORT).show();
                    }
                });
//                Toast.makeText(getApplicationContext(),"Изображение загружено",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Изображение не загружено",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendDataTocloudFirestore() {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String , Object> userdata=new HashMap<>();
        userdata.put("name",name);
        userdata.put("image",ImageUriAcessToken);
        userdata.put("uid",firebaseAuth.getUid());
        documentReference.set(userdata);

//        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
////                Toast.makeText(getApplicationContext(),"Данные успешно отправлены на Cloud Firestore",Toast.LENGTH_SHORT).show();
//            }
//        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK)
        {
            imagepath=data.getData();
            mgetuserimageinimageview.setImageURI(imagepath);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}