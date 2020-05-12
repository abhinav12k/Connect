package com.mass.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Toolbar mToolbar;

    private Button loginBtn;
    private ProgressDialog mRegProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        mRegProgress = new ProgressDialog(this);

        //adding toolbar
        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    mRegProgress.setTitle("Logging In");
                    mRegProgress.setMessage("Please wait while we check your credentials.");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                   login_user(email, password);

                }

            }
        });

    }

    private void login_user(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mRegProgress.dismiss();
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }else{
                    mRegProgress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot Signin please check the details and try again.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
