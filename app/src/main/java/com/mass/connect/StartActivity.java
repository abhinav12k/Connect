package com.mass.connect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn;
    private Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = findViewById(R.id.start_reg_btn);
        mLoginBtn = findViewById(R.id.start_login_btn);
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_Intent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_Intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
