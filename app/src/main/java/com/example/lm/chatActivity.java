package com.example.lm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class chatActivity extends AppCompatActivity {

    TabLayout tabLayout;
    TabItem mchat,mcall,mstatus;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    androidx.appcompat.widget.Toolbar mtoolbar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /*
        Код табов
        */

//        tabLayout=findViewById(R.id.include);
//        mchat=findViewById(R.id.chat);
//        mcall=findViewById(R.id.calls);
////        mstatus=findViewById(R.id.status);
//        viewPager=findViewById(R.id.fragmentcontainer);

        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase= FirebaseDatabase.getInstance();

        mtoolbar=findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);

        Drawable drawable= ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_more_vert_24);
        mtoolbar.setOverflowIcon(drawable);

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


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.profile:
                Intent intent=new Intent(chatActivity.this,UpdateProfile.class);
                startActivity(intent);
                break;

            case R.id.settings:
                Toast.makeText(getApplicationContext(),"Функция в разработке",Toast.LENGTH_SHORT).show();
                break;
        }

        return  true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Поиск");
        searchView.findViewById(androidx.appcompat.R.id.search_plate).setBackground(null);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                return false;
//            }
//        });

        return true;
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
    protected void onStart() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseReference presenceRef=firebaseDatabase.getReference().child(firebaseAuth.getUid()).child("userStatus");
        presenceRef.onDisconnect().setValue("Offline");
    }
}