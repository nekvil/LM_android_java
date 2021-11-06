package com.example.lm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class specificchat extends AppCompatActivity {

    EditText mgetmessage;
    ImageButton msendmessagebutton;

    CardView msendmessagecardview;
    androidx.appcompat.widget.Toolbar mtoolbarofspecificchat;
    ImageView mimageviewofspecificuser;
    TextView mnameofspecificuser, mstatus_of_user;

    private String enteredmessage;
    Intent intent;
    String mrecievername;
    String sendername;
    String mrecieveruid;
    String msenderuid;
    String mrecieverstatus;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String senderroom,recieverroom;

    ImageButton mbackbuttonofspecificchat;

    RecyclerView mmessagerecyclerview;

    String currenttime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    com.example.lm.MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;
    FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificchat);

        mgetmessage=findViewById(R.id.getmessage);
        msendmessagecardview=findViewById(R.id.carviewofsendmessage);
        msendmessagebutton=findViewById(R.id.imageviewsendmessage);
        mtoolbarofspecificchat=findViewById(R.id.toolbarofspecificchat);
        mnameofspecificuser=findViewById(R.id.Nameofspecificuser);
        mstatus_of_user=findViewById(R.id.status_of_user);
        mimageviewofspecificuser=findViewById(R.id.specificuserimageinimageview);
        mbackbuttonofspecificchat=findViewById(R.id.backbuttonofspecificchat);

        messagesArrayList=new ArrayList<>();
        mmessagerecyclerview=findViewById(R.id.recyclerviewofspecific);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mmessagerecyclerview.setLayoutManager(linearLayoutManager);

        messagesAdapter=new MessagesAdapter(specificchat.this,messagesArrayList);
        mmessagerecyclerview.setAdapter(messagesAdapter);

        intent=getIntent();
        setSupportActionBar(mtoolbarofspecificchat);

//        mtoolbarofspecificchat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(),"ХОБА!",Toast.LENGTH_SHORT).show();
//            }
//        });

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();

        simpleDateFormat=new SimpleDateFormat("HH:mm");

        msenderuid=firebaseAuth.getUid();
        mrecieveruid=getIntent().getStringExtra("receiveruid");
        mrecievername=getIntent().getStringExtra("name");
//        mrecieverstatus=getIntent().getStringExtra("status");

        DatabaseReference databaseRef=firebaseDatabase.getReference(mrecieveruid);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userprofile muserprofile = snapshot.getValue(userprofile.class);
                mrecieverstatus = muserprofile.getUserStatus();

                if (mrecieverstatus.equals("Offline")){
                    mstatus_of_user.setText("Был(а) недавно");
                    mstatus_of_user.setTextColor(getResources().getColor(R.color.white_87));
                }
                else{
                    mstatus_of_user.setText("В сети");
                    mstatus_of_user.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mstatus_of_user.setText("Нет данных");
//                Toast.makeText(getApplicationContext(),"Не удалось получить статус",Toast.LENGTH_SHORT).show();
            }
        });

        senderroom=msenderuid+mrecieveruid;
        recieverroom=mrecieveruid+msenderuid;

        DatabaseReference databaseReference=firebaseDatabase.getReference().child("chats").child(senderroom).child("messages");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();

                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Messages messages=snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }

                messagesAdapter=new MessagesAdapter(specificchat.this,messagesArrayList);
                mmessagerecyclerview.setAdapter(messagesAdapter);
                mmessagerecyclerview.smoothScrollToPosition(mmessagerecyclerview.getAdapter().getItemCount());
                messagesAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mbackbuttonofspecificchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mnameofspecificuser.setText(mrecievername);
        String uri=intent.getStringExtra("imageuri");
        if(uri.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Empty image",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Picasso.get().load(uri).into(mimageviewofspecificuser);
        }


        msendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enteredmessage=mgetmessage.getText().toString();
                if(enteredmessage.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Введите сообщение",Toast.LENGTH_SHORT).show();
                }

                else

                {
                    Date date=new Date();
                    calendar=Calendar.getInstance();
                    currenttime=simpleDateFormat.format(calendar.getTime());

                    Messages messages=new Messages(enteredmessage,firebaseAuth.getUid(),date.getTime(),currenttime);
                    firebaseDatabase=FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats")
                            .child(senderroom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(recieverroom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });

                    mgetmessage.setText(null);
                }
            }
        });

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {

            case R.id.video_call:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
            case R.id.search:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
            case R.id.clean_story:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
            case R.id.change_colors:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
            case R.id.off_not:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete_chat:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
        }

        return  true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu,menu);
        try {
            if (menu instanceof MenuBuilder) {
                ((MenuBuilder) menu).setOptionalIconsVisible(true);
            }
        } catch (Exception e) {
            // do nothing
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(),"Now User is Online",Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference presenceRef=firebaseDatabase.getReference().child(firebaseAuth.getUid()).child("userStatus");
        presenceRef.setValue("Online");

        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(),"Now User is Offline",Toast.LENGTH_SHORT).show();
            }
        });
        DatabaseReference presenceRef=firebaseDatabase.getReference().child(firebaseAuth.getUid()).child("userStatus");
        presenceRef.setValue("Offline");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!=null)
        {
            messagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseReference presenceRef=firebaseDatabase.getReference().child(firebaseAuth.getUid()).child("userStatus");
        presenceRef.onDisconnect().setValue("Offline");
    }

}