package com.example.talkiee;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettnigs;
    private EditText UserName,userStatus;
    private CircleImageView userProileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;
    private static final int GalleryPick=1;
    private StorageReference UserProfileImagesRef;
    private Toolbar SettingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initializeField();

        UserName.setVisibility(View.INVISIBLE);

        UpdateAccountSettnigs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettins();
            }
        });


        userProileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

        RetriveUserInformation();
    }




    private void initializeField() {
        UpdateAccountSettnigs=(Button)findViewById(R.id.set_update_settings_button);
        UserName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProileImage=(CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);
        SettingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Profile Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImagesRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri>firebaseUri=taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUri=uri.toString();
                                RootRef.child("Users").child(currentUserId).child("image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(SettingsActivity.this,"Image Saved successfully",Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else{
                                            String message=task.getException().toString();
                                            Toast.makeText(SettingsActivity.this,"Error"+message,Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();

                                        }
                                    }
                                });
                            }
                        });
                    }
                });



            }
        }
    }

    private void UpdateSettins() {
        String setUserName=UserName.getText().toString();
        String setUserStatus=userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please write your Status...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Profrile Setting Updating");
            loadingBar.setMessage("Please wait, while we are updating your profile...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            HashMap<String,Object>profileMap=new HashMap<>();
                profileMap.put("uid", currentUserId);
                profileMap.put("name", setUserName);
                profileMap.put("status", setUserStatus);
            RootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Suceesfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        loadingBar.dismiss();
                        String errormessage=task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error:"+errormessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void RetriveUserInformation() {

        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")&& (dataSnapshot.hasChild("image")))){
                    String retriveUserName=dataSnapshot.child("name").getValue().toString();
                    String retriveUserStatus=dataSnapshot.child("status").getValue().toString();
                    String retriveProfileImage=dataSnapshot.child("image").getValue().toString();

                    UserName.setText(retriveUserName);
                    userStatus.setText(retriveUserStatus);
                    Picasso.get().load(retriveProfileImage).into(userProileImage);

                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                    String retriveUserName=dataSnapshot.child("name").getValue().toString();
                    String retriveUserStatus=dataSnapshot.child("status").getValue().toString();

                    UserName.setText(retriveUserName);
                    userStatus.setText(retriveUserStatus);

                }
                else{
                    UserName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please set & update profile information...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
