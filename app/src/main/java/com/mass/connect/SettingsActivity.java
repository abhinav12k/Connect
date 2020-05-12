package com.mass.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    //Constants for intent result
    private static int GALLERY_PICK = 1;

    //Firebase setup
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private StorageReference mImageRef;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private ImageView profileImage;

    private Button changeImageBtn;
    private Button changeStatusBtn;

    //progress dialog
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //layout components
        mName = findViewById(R.id.settings_name);
        mStatus = findViewById(R.id.settings_status);
        changeImageBtn = findViewById(R.id.settings_image_btn);
        changeStatusBtn = findViewById(R.id.settings_status_btn);
        profileImage = findViewById(R.id.settings_image);

        //setting up firebase
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageRef = FirebaseStorage.getInstance().getReference();

        String current_uid = mFirebaseUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default"))
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = mStatus.getText().toString();
                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);
                startActivity(status_intent);
            }
        });

        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
//
//            Uri imageUri = data.getData();
//            CropImage.activity(imageUri)
//                    .setAspectRatio(1, 1)
//                    .start(this);
//
//        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Saving image");
                mProgressDialog.setMessage("Please wait while we update your profile photo");
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                final String current_user_id = mFirebaseUser.getUid();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference file_path = mImageRef.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageRef.child("profile_images").child("thumbs").child(current_user_id+".jpg");

                file_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            mImageRef.child("profile_images").child(current_user_id + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                            thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String thumb_downloadUrl = uri.toString();
                                                    Map update_hashMap = new HashMap();
                                                    update_hashMap.put("image",downloadUrl);
                                                    update_hashMap.put("thumb_image",thumb_downloadUrl);

                                                    mDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                mProgressDialog.dismiss();
                                                                Toast.makeText(SettingsActivity.this, "Profile Photo updated Successfully!", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(SettingsActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                                                mProgressDialog.dismiss();
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }
                                    });
                                }
                            });


                        } else {
                            Toast.makeText(SettingsActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


}
