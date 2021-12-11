package com.example.lm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class chatFragment extends Fragment {

    private FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder> chatAdapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference messagesReference, statusReference;
    ValueEventListener messagesListener, statusListener;

    HashMap<DatabaseReference, ValueEventListener> mListenerMap = new HashMap<>();
    HashMap<DatabaseReference, ValueEventListener> sListenerMap = new HashMap<>();

    LinearLayoutManager linearLayoutManager;
    ArrayList<com.example.lm.Messages> messagesArrayList;

    Boolean dark;
    String senderRoom;
    String receiverUID,senderUID;

    String weekDayName;
    String monthName;

    int count_new_messages;

    RecyclerView chatRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.chatfragment,container,false);
//        Toast.makeText(getActivity(),"onCreateView",Toast.LENGTH_SHORT).show();

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                dark = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                dark = false;
                break;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        chatRecyclerView = v.findViewById(R.id.recyclerview);
        senderUID = firebaseAuth.getUid();

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<firebasemodel> Users = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query, firebasemodel.class).build();

        chatAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(Users) {

//            @Override
//            public long getItemId(int position) {
//                return position;
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                return position;
//            }

            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel _firebasemodel) {
//                Toast.makeText(getActivity(),"onBindViewHolder",Toast.LENGTH_SHORT).show();
                final Context  context = getActivity();
                if (isValidContextForGlide(context))
                    Glide.with(context).load(_firebasemodel.getImage()).centerCrop().into(noteViewHolder.userImageView);

                noteViewHolder.userName.setText(_firebasemodel.getName());

                receiverUID = _firebasemodel.getUid();
                senderRoom = senderUID + receiverUID;

                firebaseDatabase = FirebaseDatabase.getInstance();
                messagesArrayList = new ArrayList<>();

                messagesReference = firebaseDatabase.getReference().child("chats").child(senderRoom).child("messages");
                messagesListener = messagesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        messagesArrayList.clear();
//                        receiverUID = _firebasemodel.getUid();

                        for(DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            Messages messages = snapshot.getValue(Messages.class);
                            messagesArrayList.add(messages);
                        }

                        if (messagesArrayList.size() != 0)
                        {
                            Messages last_message = messagesArrayList.get(messagesArrayList.size() - 1);

                            noteViewHolder.last_message.setText(last_message.message);
                            noteViewHolder.last_message.setVisibility(View.VISIBLE);

                            Calendar current_date = Calendar.getInstance();
                            Calendar message_date = Calendar.getInstance();

                            current_date.getTimeInMillis();
                            message_date.setTimeInMillis(last_message.timestamp);

                            boolean Day = current_date.get(Calendar.DAY_OF_YEAR) == message_date.get(Calendar.DAY_OF_YEAR);
                            boolean Week = current_date.get(Calendar.WEEK_OF_MONTH) == message_date.get(Calendar.WEEK_OF_MONTH);
                            boolean Month = current_date.get(Calendar.MONTH) == message_date.get(Calendar.MONTH);
                            boolean Year = current_date.get(Calendar.YEAR) == message_date.get(Calendar.YEAR);

                            if (Year && Month && Week && Day){
                                noteViewHolder.date_of_message.setText(last_message.currentTime);
                            }

                            if (Year && Month && Week && !Day){
                                noteViewHolder.date_of_message.setText(getWeekDay(message_date.get(Calendar.DAY_OF_WEEK)));
                            }

                            if (Year && Month && !Week && !Day){
                                String text = DateFormat.format("dd", last_message.timestamp).toString() + " " + getMonthName(message_date.get(Calendar.MONTH));
                                noteViewHolder.date_of_message.setText(text);
                            }

                            if (Year && !Month && !Week && !Day){
                                String text = DateFormat.format("dd", last_message.timestamp).toString() + " " + getMonthName(message_date.get(Calendar.MONTH));
                                noteViewHolder.date_of_message.setText(text);
                            }

                            if (!Year){
                                noteViewHolder.date_of_message.setText(DateFormat.format("dd.MM.yy", last_message.timestamp).toString());
                            }
                            noteViewHolder.date_of_message.setVisibility(View.VISIBLE);


                            if (last_message.senderId.equals(senderUID)){
                                if (!last_message.seen){
                                    noteViewHolder.seen.setVisibility(View.GONE);
                                    noteViewHolder.delivered.setVisibility(View.VISIBLE);
                                }
                                else{
                                    noteViewHolder.delivered.setVisibility(View.GONE);
                                    noteViewHolder.seen.setVisibility(View.VISIBLE);
                                }
                            }
                            else{

                                noteViewHolder.delivered.setVisibility(View.GONE);
                                noteViewHolder.seen.setVisibility(View.GONE);

                                if (!last_message.seen){
                                    count_new_messages = 0;
                                    for (Messages message : messagesArrayList){
                                        if (!message.senderId.equals(senderUID) && !message.seen)
                                            count_new_messages += 1;
                                    }
                                    String messagesCount = Integer.toString(count_new_messages);
                                    noteViewHolder.messages_count.setText(messagesCount);
                                    noteViewHolder.messages_count.setVisibility(View.VISIBLE);
                                }
                                else{
                                    noteViewHolder.messages_count.setVisibility(View.INVISIBLE);
                                }

                            }

                        }
                        else{
                            noteViewHolder.date_of_message.setVisibility(View.INVISIBLE);
                            noteViewHolder.messages_count.setVisibility(View.INVISIBLE);
                            noteViewHolder.delivered.setVisibility(View.GONE);
                            noteViewHolder.seen.setVisibility(View.GONE);
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }

                });


                statusReference = firebaseDatabase.getReference(receiverUID);
                statusListener = statusReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userprofile UserProfile = dataSnapshot.getValue(userprofile.class);

//                        Toast.makeText(getActivity(),"onDataChange statusListener",Toast.LENGTH_SHORT).show();

                        if (UserProfile != null){

                            if (UserProfile.userStatus.equals("Online"))
                                noteViewHolder.status_icon.setVisibility(View.VISIBLE);
                            else if (UserProfile.userStatus.equals("Offline"))
                                noteViewHolder.status_icon.setVisibility(View.GONE);

                            if (noteViewHolder.last_message.getText().toString().equals("Last message")
                                    || noteViewHolder.last_message.getText().toString().equals("был(а) недавно")
                                    || noteViewHolder.last_message.getText().toString().equals("в сети"))
                            {
                                if (UserProfile.userStatus.equals("Online")){
                                    noteViewHolder.last_message.setText("в сети");
                                    noteViewHolder.status_icon.setVisibility(View.VISIBLE);
                                }
                                else if (UserProfile.userStatus.equals("Offline")) {
                                    noteViewHolder.last_message.setText("был(а) недавно");
                                    noteViewHolder.status_icon.setVisibility(View.GONE);
                                }
                                noteViewHolder.last_message.setVisibility(View.VISIBLE);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                mListenerMap.put(messagesReference, messagesListener);
                sListenerMap.put(statusReference, statusListener);

                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getActivity(), specificchat.class);
                        intent.putExtra("name", _firebasemodel.getName());
                        intent.putExtra("receiverUid", _firebasemodel.getUid());
                        intent.putExtra("imageURI", _firebasemodel.getImage());
                        startActivity(intent);
                    }
                });
            }


            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        chatRecyclerView.setHasFixedSize(true);
//        chatRecyclerView.setItemAnimator(null);
//        chatAdapter.setHasStableIds(true);
        linearLayoutManager=new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        return v;
    }


    public static class NoteViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView userName;
        private final TextView last_message;
        private final TextView date_of_message;
        private final TextView messages_count;
        private final ImageView userImageView;
        private final ImageView delivered;
        private final ImageView seen;
        private final ImageView status_icon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.nameofuser);
            last_message = itemView.findViewById(R.id.last_message);
            date_of_message = itemView.findViewById(R.id.date_of_message);
            userImageView = itemView.findViewById(R.id.imageviewofuser);
            delivered = itemView.findViewById(R.id.status_of_seen);
            seen = itemView.findViewById(R.id.status_of_seen_2);
            messages_count = itemView.findViewById(R.id.messages_count);
            status_icon = itemView.findViewById(R.id.status_icon);
        }
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


    private String getWeekDay(int num){

        switch (num) {
            case Calendar.MONDAY:
                weekDayName = "пн";
                break;
            case Calendar.TUESDAY:
                weekDayName = "вт";
                break;
            case Calendar.WEDNESDAY:
                weekDayName = "ср";
                break;
            case Calendar.THURSDAY:
                weekDayName = "чт";
                break;
            case Calendar.FRIDAY:
                weekDayName = "пт";
                break;
            case Calendar.SATURDAY:
                weekDayName = "сб";
                break;
            case Calendar.SUNDAY:
                weekDayName = "вс";
                break;
        }

        return weekDayName;
    }


    private String getMonthName(int num){

        switch (num) {
            case Calendar.JANUARY:
                monthName = "янв.";
                break;
            case Calendar.FEBRUARY:
                monthName = "февр.";
                break;
            case Calendar.MARCH:
                monthName = "март.";
                break;
            case Calendar.APRIL:
                monthName = "апр.";
                break;
            case Calendar.MAY:
                monthName = "мая";
                break;
            case Calendar.JUNE:
                monthName = "июн.";
                break;
            case Calendar.JULY:
                monthName = "июл.";
                break;
            case Calendar.AUGUST:
                monthName = "авг.";
                break;
            case Calendar.SEPTEMBER:
                monthName = "сент.";
                break;
            case Calendar.OCTOBER:
                monthName = "окт.";
                break;
            case Calendar.NOVEMBER:
                monthName = "нояб.";
                break;
            case Calendar.DECEMBER:
                monthName = "дек.";
                break;
        }

        return monthName;
    }


    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Поиск");
        searchView.findViewById(androidx.appcompat.R.id.search_plate).setBackground(null);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu, searchItem, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setItemsVisibility(menu, searchItem, true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }


    private void setItemsVisibility(@NonNull Menu menu, MenuItem exception, boolean visible) {
        for (int i=0; i<menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception)
            { item.setVisible(visible); }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
//        Toast.makeText(getActivity(),"onStart - startListening",Toast.LENGTH_SHORT).show();
    }


    public void removeListener(@NonNull HashMap<DatabaseReference, ValueEventListener> listenerMap){
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
//            Toast.makeText(getActivity(), "removeEventListener", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter != null)
        {
            RecyclerView.LayoutManager layoutManager = chatRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.removeAllViews();
            }
            removeListener(mListenerMap);
            removeListener(sListenerMap);
            chatAdapter.stopListening();
        }
    }

}
