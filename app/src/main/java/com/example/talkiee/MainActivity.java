package com.example.talkiee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseAuth mAuth;

    private DatabaseReference RootRef;
    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



       mAuth = FirebaseAuth.getInstance();

        RootRef=FirebaseDatabase.getInstance().getReference();

        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Talkiee");

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);


        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser=mAuth.getCurrentUser();
        super.onStart();
        
        if(currentUser==null){
            sendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            verifyUserExistance();
        }
    }



    @Override
    protected void onStop() {

        super.onStop();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        FirebaseUser currentUser=mAuth.getCurrentUser();
        super.onDestroy();
        if(currentUser!=null){
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistance() {
        String currentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this, "Welcome..", Toast.LENGTH_SHORT).show();
                }
                else{
                   sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_options){
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_settings_options){
           sendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_find_frines_options){
            sendUserToFindFriendsActivity();
        }
        return true;

    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent=new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void sendUserToSettingsActivity() {
        Intent settingsIntet=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntet);

    }

    private void updateUserStatus(String state){
        String saveCurrentTime,saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate=currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String , Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);


    }

}
