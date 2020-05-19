package com.mass.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private Button mProfileDeclineReqBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    private FirebaseUser mCurrent_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");


        mProfileName = findViewById(R.id.profile_Display_Name);
        mProfileImage = findViewById(R.id.profileImage);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mProfileSendReqBtn = findViewById(R.id.profile_sendFriendRequest);
        mProfileDeclineReqBtn = findViewById(R.id.profile_declineFriendRequest);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading....");
        mProgressDialog.setMessage("Please wait while we load the data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        final String[] display_name = new String[1];

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                display_name[0] = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name[0]);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                // *****************************Friend list/ Request Feature******************************

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            //means either the friend request is received or we have we sent that so updating the respective buttons

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mProfileDeclineReqBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineReqBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {

                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        } else {

                            //checking that the person is already a friend or not

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend" + " " + display_name[0]);
                                        mProgressDialog.dismiss();
                                        mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineReqBtn.setEnabled(false);

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ------------------Not friends state ---------------------------

                if (mCurrent_state.equals("not_friends")) {

                    DatabaseReference mNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = mNotificationref.getKey();

                    //Saving notifications to the database so that they can be implemented using functions in the firebase
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id+"/request_type","sent");
                    requestMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid()+"/request_type","received");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(ProfileActivity.this, "There was some error in sending the request", Toast.LENGTH_SHORT).show();
                            }

                            mProfileDeclineReqBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
                        }
                    });
                }

                // ------------------Cancel Friend req state ---------------------------
                if (mCurrent_state.equals("req_sent")) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineReqBtn.setEnabled(false);

                                }
                            });
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    mProfileSendReqBtn.setEnabled(true);
                                    Toast.makeText(ProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            });

                    mProgressDialog.dismiss();
                }

                // ------------------Request received state ---------------------------
                if (mCurrent_state.equals("req_received")) {

                    final String CurrentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+mCurrent_user.getUid()+"/"+user_id+"/date/",CurrentDate);
                    friendsMap.put("Friends/"+user_id+"/"+mCurrent_user.getUid()+"/date/",CurrentDate);

                    friendsMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id,null);
                    friendsMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError==null){

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend" + " " + display_name[0]);
                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);


                            }else{
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

                //-----------------Unfriends-------------------
                if(mCurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/"+mCurrent_user.getUid()+"/"+user_id,null);
                    unfriendMap.put("Friends/"+user_id+"/"+mCurrent_user.getUid(),null);
                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError==null){

                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");
                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);


                            }else{
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }

            }
        });

        mProfileDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map RequestMap = new HashMap();
                RequestMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id,null);
                RequestMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid(),null);
                mRootRef.updateChildren(RequestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if(databaseError==null){

                            mCurrent_state = "not_friends";
                            mProfileSendReqBtn.setText("Send Friend Request");
                            mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                            mProfileDeclineReqBtn.setEnabled(false);


                        }else{
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        mProfileSendReqBtn.setEnabled(true);
                    }
                });
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mCurrent_user.getUid()).child("online").setValue(true);
    }
}
