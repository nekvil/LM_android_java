package com.example.lm;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;


public class chatFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;
    ArrayList<com.example.lm.Messages> messagesArrayList;
    ImageView mimageviewofuser;



    FirebaseDatabase firebaseDatabase;
    String senderroom,recieverroom;
    String mrecievername,sendername,mrecieveruid,msenderuid;

    FirestoreRecyclerAdapter<com.example.lm.firebasemodel,NoteViewHolder> chatAdapter;

    RecyclerView mrecyclerview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       View v=inflater.inflate(R.layout.chatfragment,container,false);
       firebaseAuth=FirebaseAuth.getInstance();
       firebaseFirestore= FirebaseFirestore.getInstance();
       mrecyclerview=v.findViewById(R.id.recyclerview);

        Query query=firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<firebasemodel> allusername=new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query, firebasemodel.class).build();

        chatAdapter=new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusername) {

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
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {

                noteViewHolder.particularusername.setText(firebasemodel.getName());

                String uri=firebasemodel.getImage();
                Picasso.get().load(uri).into(mimageviewofuser);
//                System.out.println(uri);
                msenderuid=firebaseAuth.getUid();
                mrecieveruid= firebasemodel.getUid();
                senderroom=msenderuid+mrecieveruid;

                firebaseDatabase=FirebaseDatabase.getInstance();
                DatabaseReference databaseReference=firebaseDatabase.getReference().child("chats").child(senderroom).child("messages");
                messagesArrayList=new ArrayList<>();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesArrayList.clear();

                        for(DataSnapshot snapshot1:snapshot.getChildren())
                        {
                            Messages messages=snapshot1.getValue(Messages.class);
                            messagesArrayList.add(messages);
                        }

                        if (messagesArrayList.size() != 0)
                        {
                            noteViewHolder.last_message.setText(messagesArrayList.get(messagesArrayList.size() - 1).message);

                            String date_now = DateFormat.format("dd.MM.yy", Calendar.getInstance().getTime()).toString();
                            String date_last_mes = DateFormat.format("dd.MM.yy", messagesArrayList.get(messagesArrayList.size() - 1).timestamp).toString();

                            if (date_now.equals(date_last_mes)){
                                String substr = messagesArrayList.get(messagesArrayList.size() - 1).currenttime+"  "+date_now;
                                noteViewHolder.date_of_message.setText(substr.substring(0,substr.length()-3));
                            }
                            else{
                                noteViewHolder.date_of_message.setText(date_last_mes);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

//                noteViewHolder.last_message.setTextColor(getResources().getColor(R.color.white_60));

                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getActivity(), specificchat.class);
                        intent.putExtra("name",firebasemodel.getName());
                        intent.putExtra("receiveruid",firebasemodel.getUid());
                        intent.putExtra("imageuri",firebasemodel.getImage());
                        intent.putExtra("status",firebasemodel.getStatus());
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

        mrecyclerview.setHasFixedSize(true);
//        chatAdapter.setHasStableIds(true);

        linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        mrecyclerview.setLayoutManager(linearLayoutManager);
        mrecyclerview.setAdapter(chatAdapter);

        chatAdapter.startListening();

        return v;
    }


    public class
    NoteViewHolder extends RecyclerView.ViewHolder
    {
        private TextView particularusername;
        private TextView last_message;
        private TextView date_of_message;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            particularusername=itemView.findViewById(R.id.nameofuser);
            last_message=itemView.findViewById(R.id.last_message);
            date_of_message=itemView.findViewById(R.id.date_of_message);
            mimageviewofuser=itemView.findViewById(R.id.imageviewofuser);

        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        chatAdapter.startListening();
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(chatAdapter!=null)
        {
            chatAdapter.stopListening();
        }
    }

}
