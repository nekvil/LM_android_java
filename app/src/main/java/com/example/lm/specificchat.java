package com.example.lm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class specificchat extends AppCompatActivity {

    Intent intent;

    EditText mgetmessage;
    Button msendmessagebutton;
    CardView msendmessagecardview;
    androidx.appcompat.widget.Toolbar mtoolbarofspecificchat;
    ImageView mimageviewofspecificuser;
    ImageButton mbackbuttonofspecificchat;
    TextView mnameofspecificuser, mstatus_of_user;
    RecyclerView mmessagerecyclerview;
    com.example.lm.MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;
    ValueEventListener statusListener, seenListener, messagesListener;
    DatabaseReference statusReference, seenReference, messagesReference;

    private String enteredmessage;

    String mrecieverstatus, mrecieverTypingTo;
    String sendername, mrecievername;
    String mrecieveruid, msenderuid;
    String senderroom,receiverroom;
    String currenttime;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;

    long delay = 5000; // 5 seconds after user stops typing
    long last_text_edit = 0;

    Handler handler = new Handler();
    Handler tHandler = new Handler();

    Boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificchat);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                dark = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                dark = false;
                break;
        }

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
//                Toast.makeText(getApplicationContext(),"Open profile user",Toast.LENGTH_SHORT).show();
//            }
//        });

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();

        simpleDateFormat=new SimpleDateFormat("HH:mm", Locale.getDefault());

        msenderuid=firebaseAuth.getUid();
        mrecieveruid=intent.getStringExtra("receiverUid");
        mrecievername=intent.getStringExtra("name");


        mgetmessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(input_finish_checker);

                if (s.toString().trim().length() != 0)
                { updateTypingToStatus(mrecieveruid); }
                else
                { updateTypingToStatus("None"); }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            { }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                } else { }
            }
        });


        statusReference = firebaseDatabase.getReference(mrecieveruid);
        statusListener = statusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tHandler.removeCallbacks(tRunnable);

                userprofile muserprofile = snapshot.getValue(userprofile.class);
                mrecieverTypingTo = muserprofile.getTypingTo();
                mrecieverstatus = muserprofile.getUserStatus();
//                mnameofspecificuser.setText(muserprofile.getUsername());

                if (mrecieverstatus.equals("Offline")){
                    mstatus_of_user.setText("был(а) недавно");
                    if (dark) { mstatus_of_user.setTextColor(getResources().getColor(R.color.white_87)); }
                    else{ mstatus_of_user.setTextColor(getResources().getColor(R.color.main_80)); }
                }
                else if (mrecieverTypingTo.equals(msenderuid)){
                    if (dark) { mstatus_of_user.setTextColor(getResources().getColor(R.color.white)); }
                    else{ mstatus_of_user.setTextColor(getResources().getColor(R.color.main)); }
                    tHandler.postDelayed(tRunnable, 1*100);
                }
                else{
                    mstatus_of_user.setText("в сети");
                    if (dark) { mstatus_of_user.setTextColor(getResources().getColor(R.color.white)); }
                    else{ mstatus_of_user.setTextColor(getResources().getColor(R.color.main)); }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mstatus_of_user.setText("Нет данных");
//                Toast.makeText(getApplicationContext(),"Не удалось получить статус",Toast.LENGTH_SHORT).show();
            }
        });


        senderroom=msenderuid+mrecieveruid;
        receiverroom=mrecieveruid+msenderuid;


        seenReference = firebaseDatabase.getReference().child("chats").child(receiverroom).child("messages");
        seenListener = seenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Messages messages = snapshot.getValue(Messages.class);
                    if (messages != null && messages.getSenderId().equals(mrecieveruid) && !messages.isSeen()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        messagesReference = firebaseDatabase.getReference().child("chats").child(senderroom).child("messages");
        messagesListener = messagesReference.addValueEventListener(new ValueEventListener() {
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
//                messagesAdapter.notifyDataSetChanged();
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

        String uri=intent.getStringExtra("imageURI");
        if(uri.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Empty image",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Glide.with(this).load(uri).centerCrop().into(mimageviewofspecificuser);
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

                    Messages senderRoom = new Messages(enteredmessage,firebaseAuth.getUid(),date.getTime(),currenttime,false);
                    Messages receiverRoom = new Messages(enteredmessage,firebaseAuth.getUid(),date.getTime(),currenttime,true);
                    firebaseDatabase=FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats")
                            .child(senderroom)
                            .child("messages")
                            .push()
                            .setValue(senderRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(receiverroom)
                                    .child("messages")
                                    .push()
                                    .setValue(receiverRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
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


    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                updateTypingToStatus("None");
            }
        }
    };


    private Runnable tRunnable = new Runnable() {
        int count = 0;
        @Override
        public void run() {
            count++;

            if (count == 1)
            { mstatus_of_user.setText("печатает"); }
            else if (count == 2)
            { mstatus_of_user.setText("печатает."); }
            else if (count == 3)
            { mstatus_of_user.setText("печатает.."); }
            else if (count == 4)
            { mstatus_of_user.setText("печатает..."); }

            if (count == 4)
                count = 0;

            tHandler.postDelayed(this, 5*100);
        }
    };


    private void updateTypingToStatus(String typingTo){
        DatabaseReference presenceRef=firebaseDatabase.getReference().child(firebaseAuth.getUid()).child("typingTo");
        presenceRef.setValue(typingTo);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.video_call:
            case R.id.search:
            case R.id.clean_story:
            case R.id.change_colors:
            case R.id.off_not:
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
    public void onStop() {
        super.onStop();
        statusReference.removeEventListener(statusListener);
        seenReference.removeEventListener(seenListener);
        messagesReference.removeEventListener(messagesListener);
    }


    @Override
    public void onPause() {
        super.onPause();
        updateTypingToStatus("None");

//        if(messagesAdapter!=null)
//        {
//            messagesAdapter.notifyDataSetChanged();
//        }

    }

}