package com.example.lm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class specificchat extends AppCompatActivity {

    private Button sendMessageButton, attachFileButton, sendVoiceMessageButton;
    private EditText getTextMessage;
    private FloatingActionButton scrollDown;
    private RecyclerView messageRecyclerView;
    private com.example.lm.MessagesAdapter messagesAdapter;
    private ArrayList<Messages> messagesArrayList;
    private ValueEventListener statusListener, seenListener, messagesListener, connectedListener;
    private DatabaseReference statusReference, seenReference, messagesReference, connectedRef;

    private String enteredMessage;
    private String receiverStatus, receiverTypingTo;
    private String receiverUID, senderUID;
    private String senderChat, receiverChat;
    private String currentTime;
    private Boolean darkMode;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    long delay = 5000; // 5 seconds after user stops typing
    long last_text_edit = 0;

    Intent intent;
    Toolbar toolbar;
    ImageView userImage;
    ImageButton backButton;
    TextView userName, userStatus;
    Parcelable recyclerViewState;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    Handler handler = new Handler();
    Handler tHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_specificchat);

        final float scale = getResources().getDisplayMetrics().density;
        int padding_10dp = (int) (10 * scale + 0.5f);
        int padding_55dp = (int) (55 * scale + 0.5f);
        int padding_100dp = (int) (100 * scale + 0.5f);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                darkMode = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                darkMode = false;
                break;
        }

        simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        toolbar = findViewById(R.id.toolbarOfSpecificChat);
        userName = findViewById(R.id.Nameofspecificuser);
        userStatus = findViewById(R.id.status_of_user);
        userImage = findViewById(R.id.specificuserimageinimageview);
        backButton = findViewById(R.id.backbuttonofspecificchat);
        scrollDown = findViewById(R.id.scrollDown);
        getTextMessage = findViewById(R.id.getMessage);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        attachFileButton = findViewById(R.id.attachFileButton);
        sendVoiceMessageButton = findViewById(R.id.sendVoiceMessageButton);
        messageRecyclerView = findViewById(R.id.specificChatRecycler);

        intent = getIntent();
        receiverUID = intent.getStringExtra("receiverUid");

        senderUID = firebaseAuth.getUid();
        senderChat = senderUID + receiverUID;
        receiverChat = receiverUID + senderUID;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        messagesArrayList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(specificchat.this, messagesArrayList);
        messageRecyclerView.setAdapter(messagesAdapter);

        userName.setText(intent.getStringExtra("name"));
        Glide.with(this).load(intent.getStringExtra("imageURI")).centerCrop().into(userImage);


        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        scrollDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageRecyclerView.getAdapter() != null)
                    messageRecyclerView.scrollToPosition(messageRecyclerView.getAdapter().getItemCount()-1);
            }
        });


        messageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 50 && scrollDown.getVisibility() != View.VISIBLE && recyclerView.canScrollVertically(1))
                    scrollDown.show();
                if (dy < -50 && scrollDown.getVisibility() == View.VISIBLE)
                    scrollDown.hide();
                if (!recyclerView.canScrollVertically(1))
                    scrollDown.hide();
            }

        });


        connectedRef = firebaseDatabase.getReference(".info/connected");
        connectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                statusReference.addValueEventListener(statusListener);
                if (!connected) {
                    statusReference.removeEventListener(statusListener);
                    userStatus.setText("Ожидание сети...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        };


        statusReference = firebaseDatabase.getReference(receiverUID);
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tHandler.removeCallbacks(tRunnable);

                userprofile userProfile = snapshot.getValue(userprofile.class);

                if (userProfile != null) {
                    receiverTypingTo = userProfile.getTypingTo();
                    receiverStatus = userProfile.getUserStatus();
                }

                if (receiverStatus.equals("Offline")){
                    userStatus.setText("был(а) недавно");
                    if (darkMode) { userStatus.setTextColor(getResources().getColor(R.color.white_87)); }
                    else{ userStatus.setTextColor(getResources().getColor(R.color.main_80)); }
                }
                else if (receiverTypingTo.equals(senderUID)){
                    if (darkMode) { userStatus.setTextColor(getResources().getColor(R.color.white)); }
                    else{ userStatus.setTextColor(getResources().getColor(R.color.main)); }
                    tHandler.postDelayed(tRunnable, 100);
                }
                else{
                    userStatus.setText("в сети");
                    if (darkMode) { userStatus.setTextColor(getResources().getColor(R.color.white)); }
                    else{ userStatus.setTextColor(getResources().getColor(R.color.main)); }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        seenReference = firebaseDatabase.getReference().child("chats").child(receiverChat).child("messages");
        seenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Messages messages = snapshot.getValue(Messages.class);
                    updateSeenStatus(messages, snapshot, receiverUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        messagesReference = firebaseDatabase.getReference().child("chats").child(senderChat).child("messages");
        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesArrayList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Messages messages = snapshot.getValue(Messages.class);
                    updateSeenStatus(messages, snapshot, receiverUID);
                    messagesArrayList.add(messages);
                }

                messagesAdapter.notifyDataSetChanged();
//                messagesAdapter = new MessagesAdapter(specificchat.this,messagesArrayList);
//                messageRecyclerView.setAdapter(messagesAdapter);
                if (recyclerViewState != null) {
                    recyclerViewState = null;
                }
                else {
                    if (messageRecyclerView.getAdapter() != null)
                        messageRecyclerView.scrollToPosition(messageRecyclerView.getAdapter().getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        getTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(input_finish_checker);

                if (s.toString().trim().length() != 0) {
                    updateTypingToStatus(receiverUID);

                    getTextMessage.setPadding(padding_55dp, padding_10dp, padding_55dp, padding_10dp);

                    sendMessageButton.setVisibility(View.VISIBLE);
                    attachFileButton.setVisibility(View.GONE);
                    sendVoiceMessageButton.setVisibility(View.GONE);
                }
                else {
                    updateTypingToStatus("None");

                    getTextMessage.setPadding(padding_55dp, padding_10dp, padding_100dp, padding_10dp);

                    sendMessageButton.setVisibility(View.GONE);
                    attachFileButton.setVisibility(View.VISIBLE);
                    sendVoiceMessageButton.setVisibility(View.VISIBLE);
                }
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
                }
            }
        });


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enteredMessage = getTextMessage.getText().toString().trim();

                Date date = new Date();
                calendar = Calendar.getInstance();
                currentTime = simpleDateFormat.format(calendar.getTime());

                Messages messages = new Messages(enteredMessage, firebaseAuth.getUid(), date.getTime(), currentTime, false);
                firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("chats")
                        .child(senderChat)
                        .child("messages")
                        .push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        firebaseDatabase.getReference()
                                .child("chats")
                                .child(receiverChat)
                                .child("messages")
                                .push()
                                .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });
                getTextMessage.setText(null);
            }

        });

    }


    private void updateSeenStatus(Messages messages, DataSnapshot snapshot, String room) {
        if (messages != null && messages.getSenderId().equals(room) && !messages.isSeen()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("seen", true);
            snapshot.getRef().updateChildren(hashMap);
        }
    }


    private final Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                updateTypingToStatus("None");
            }
        }
    };


    private final Runnable tRunnable = new Runnable() {
        int count = 0;
        @Override
        public void run() {
            count++;

            if (count == 1)
            { userStatus.setText("печатает"); }
            else if (count == 2)
            { userStatus.setText("печатает."); }
            else if (count == 3)
            { userStatus.setText("печатает.."); }
            else if (count == 4)
            { userStatus.setText("печатает..."); }

            if (count == 4)
                count = 0;

            tHandler.postDelayed(this, 5*100);
        }
    };


    private void updateTypingToStatus(String typingTo){
        DatabaseReference presenceRef = firebaseDatabase.getReference().child(senderUID).child("typingTo");
        presenceRef.setValue(typingTo);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.video_call || itemId == R.id.search || itemId == R.id.clean_story ||
                itemId == R.id.change_colors || itemId == R.id.off_not || itemId == R.id.delete_chat) {
            Toast.makeText(getApplicationContext(), "Функция в разработке", Toast.LENGTH_SHORT).show();
        }

        return  true;
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
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
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 121) {
            messagesAdapter.copyMessage(item.getGroupId());
            Toast.makeText(getApplicationContext(),"Сообщение скопировано в буфер обмена",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        connectedRef.addValueEventListener(connectedListener);
//        statusReference.addValueEventListener(statusListener);
        seenReference.addValueEventListener(seenListener);
        messagesReference.addValueEventListener(messagesListener);
        messageRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
//        Toast.makeText(getApplicationContext(),"specChat - onStart",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStop() {
        super.onStop();
        updateTypingToStatus("None");

        statusReference.removeEventListener(statusListener);
        connectedRef.removeEventListener(connectedListener);
        seenReference.removeEventListener(seenListener);
        messagesReference.removeEventListener(messagesListener);
        recyclerViewState = messageRecyclerView.getLayoutManager().onSaveInstanceState();
        if(messagesAdapter != null)
            messagesAdapter.notifyDataSetChanged();
//        Toast.makeText(getApplicationContext(),"specChat - onStop",Toast.LENGTH_SHORT).show();

    }

}