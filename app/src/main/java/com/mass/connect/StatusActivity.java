package com.mass.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout status;
    private Button saveBtn;

    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    //Progess Dialog
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        status = findViewById(R.id.currentStatus);
        saveBtn = findViewById(R.id.saveBtn);

        //setting up toolbar
        toolbar = findViewById(R.id.status_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setting up firebase database
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        String status_value=getIntent().getStringExtra("status_value");
        status.getEditText().setText(status_value);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progress = new ProgressDialog(StatusActivity.this);
                progress.setTitle("Saving Changes");
                progress.setMessage("Please wait while we update your status");
                progress.show();

                String Cstatus = status.getEditText().getText().toString();
                mDatabase.child("status").setValue(Cstatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progress.dismiss();
                        }else{
                            Toast.makeText(StatusActivity.this,"There was some error saving changes",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
}
