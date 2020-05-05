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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button SendVeryficationCodeButon,VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String mVeriricationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=FirebaseAuth.getInstance();

        initializerFields();

        SendVeryficationCodeButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber=InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number first.....", Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone.....");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,
                            callbacks);             // Activity (for callback binding)
                                   // OnVerificationStateChangedCallbacks

                }
            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVeryficationCodeButon.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

               String verificationCode=InputVerificationCode.getText().toString();

               if(TextUtils.isEmpty(verificationCode)){
                   Toast.makeText(PhoneLoginActivity.this, "Please write verification code first", Toast.LENGTH_SHORT).show();
               }
               else{
                   loadingBar.setTitle("Verification Code");
                   loadingBar.setMessage("please wait, while we are Verifying verification code.....");
                   loadingBar.setCanceledOnTouchOutside(true);
                   loadingBar.show();
                   PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVeriricationId,verificationCode);
                   signInWithPhoneAuthCredential(credential);
               }
            }
        });
        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                String message=e.getMessage().toString();
                Toast.makeText(PhoneLoginActivity.this, "Error :"+message, Toast.LENGTH_SHORT).show();

                SendVeryficationCodeButon.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }
            public void onCodeSent(String verificationId,PhoneAuthProvider.ForceResendingToken token){
                mVeriricationId=verificationId;
                mResendToken=token;
                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check", Toast.LENGTH_SHORT).show();


                SendVeryficationCodeButon.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);

            }
        };
    }

    private void initializerFields() {
        SendVeryficationCodeButon=(Button)findViewById(R.id.send_varificaiton_code_button);
        VerifyButton=(Button)findViewById(R.id.varify_button);
        InputPhoneNumber=(EditText)findViewById(R.id.phone_number_input);
        InputVerificationCode=(EditText)findViewById(R.id.varification_code_input);
        loadingBar=new ProgressDialog(this);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    loadingBar.dismiss();
                    Toast.makeText(PhoneLoginActivity.this,"Congratulations you are loged in Successfully..",Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                }
                else{
                    String message=task.getException().toString();
                    Toast.makeText(PhoneLoginActivity.this,"Error :"+message,Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
