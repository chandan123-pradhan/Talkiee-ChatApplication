package com.example.talkiee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity {
    private Button loginbt,phoneloginbtn;
    private EditText loginemail,loginpassword;
    private TextView neednewAccountLink,forgetPasswordLink;
    private ProgressDialog loadingBar;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        neednewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });

        loginbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        
        phoneloginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllUserToLoginWitPhone();
            }
        });
    }

    private void AllUserToLoginWitPhone() {
        Intent phoneLoiginIntet=new Intent(LoginActivity.this,PhoneLoginActivity.class);
        phoneLoiginIntet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneLoiginIntet);
        finish();

    }

    private void AllowUserToLogin() {
        String email=loginemail.getText().toString();
        String password=loginpassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter your email...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter your Passwor...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("please wait while.. we are matching your account");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String currentUserId=mAuth.getCurrentUser().getUid();
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    sendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this, "You have loged successfylly", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    }
                    else{
                        String errormessage=task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error: "+errormessage, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }




    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void InitializeFields() {
        loginbt=(Button)findViewById(R.id.login_button);
       phoneloginbtn=(Button)findViewById(R.id.phone_login_button);
        loginemail=(EditText)findViewById(R.id.login_email);
        loginpassword=(EditText)findViewById(R.id.login_password);
        neednewAccountLink=(TextView)findViewById(R.id.need_new_account_link);
        forgetPasswordLink=(TextView)findViewById(R.id.forget_password_link);
        loadingBar=new ProgressDialog(this);
    }
}
