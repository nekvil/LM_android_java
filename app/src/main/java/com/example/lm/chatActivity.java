package com.example.lm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;


public class chatActivity extends AppCompatActivity {

//    TabLayout tabLayout;
//    TabItem mchat,mcall,mstatus;
//    ViewPager viewPager;
//    PagerAdapter pagerAdapter;

    TextView appTitleName;
    androidx.appcompat.widget.Toolbar toolbar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;

    ValueEventListener connectedListener;
    DatabaseReference connectedRef;

    String userId;

    Handler tHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        tabLayout=findViewById(R.id.include);
//        mchat=findViewById(R.id.chat);
//        mcall=findViewById(R.id.calls);
////        mstatus=findViewById(R.id.status);
//        viewPager=findViewById(R.id.fragmentcontainer);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = firebaseAuth.getUid();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appTitleName = findViewById(R.id.appTitleName);

        connectedRef = firebaseDatabase.getReference(".info/connected");
        connectedListener = connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                tHandler.removeCallbacks(tRunnable);
                if (connected) {
                    DatabaseReference userStatusRef = firebaseDatabase.getReference().child(userId).child("userStatus");
                    userStatusRef.setValue("Online");
                    userStatusRef.onDisconnect().setValue("Offline");

                    DatabaseReference lastOnlineRef = firebaseDatabase.getReference().child(userId).child("lastOnline");
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    appTitleName.setText(R.string.short_app_name);
                } else {
                    tHandler.postDelayed(tRunnable, 100);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

//        pagerAdapter=new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
//        viewPager.setAdapter(pagerAdapter);
//
//        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//
////               tab.getPosition()==0 || tab.getPosition()==1 || tab.getPosition()==2
//
//                if(tab.getPosition()==0 || tab.getPosition()==1)
//                {
//                    pagerAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//
//
//        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.profile){
            Intent intent = new Intent(chatActivity.this, UpdateProfile.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.settings){
            Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
        }
        else{
            return false;
        }

        return  true;
    }


//    @Override
//    public boolean onCreateOptionsMenu(final Menu menu) {
//
//        getMenuInflater().inflate(R.menu.menu,menu);
//        final MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//        searchView.setQueryHint("Поиск");
//        searchView.findViewById(androidx.appcompat.R.id.search_plate).setBackground(null);
//
//        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                setItemsVisibility(menu, searchItem, false);
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                setItemsVisibility(menu, searchItem, true);
//                return true;
//            }
//        });
//
////        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
////            @Override
////            public boolean onQueryTextSubmit(String s) {
////                return false;
////            }
////
////            @Override
////            public boolean onQueryTextChange(String s) {
////                return false;
////            }
////        });
//
//        return super.onCreateOptionsMenu(menu);
//    }


//    private void setItemsVisibility(@NonNull Menu menu, MenuItem exception, boolean visible) {
//        for (int i=0; i<menu.size(); ++i) {
//            MenuItem item = menu.getItem(i);
//            if (item != exception)
//            { item.setVisible(visible); }
//        }
//    }


    private final Runnable tRunnable = new Runnable() {
        int count = 0;
        @Override
        public void run() {
            count++;

            if (count == 1)
            { appTitleName.setText("Ожидание сети"); }
            else if (count == 2)
            { appTitleName.setText("Ожидание сети."); }
            else if (count == 3)
            { appTitleName.setText("Ожидание сети.."); }
            else if (count == 4)
            { appTitleName.setText("Ожидание сети..."); }

            if (count == 4)
                count = 0;

            tHandler.postDelayed(this, 5*100);
        }
    };


}
