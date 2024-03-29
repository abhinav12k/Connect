package com.mass.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String mChatUserName;

    //TextViews and CircleImage views of the appbar
    private TextView mTitleView;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    //EditText and image views of the bottom send bar
    private ImageView mPlusBtn;
    private ImageView mSendBtn;
    private EditText mMessage;

    private Toolbar mToolbar;

    //Firebase
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private StorageReference mImageStorage;

    //Setting recycler view for chats activity
    private RecyclerView mMessagesList;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    //Amount of messages to load
    private static final int TOTAL_MESSAGES_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itemPos = 0;
    private String mPrevKey = "";
    private String mLastKey = "";

    private final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Setting up toolbar
        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("name");
//        getSupportActionBar().setTitle(mChatUserName);

        //implementing custom toolbar design inside our app bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        //setting contents of our toolbar
        mTitleView = findViewById(R.id.chat_display_name);
        mLastSeen = findViewById(R.id.chat_last_seen);
        mProfileImage = findViewById(R.id.chat_custom_bar_image);

        //setting up Ids for image and edit texts
        mPlusBtn = findViewById(R.id.chat_addBtn);
        mSendBtn = findViewById(R.id.chat_sendBtn);
        mMessage = findViewById(R.id.chat_messageBox);

        //setting up messages list recycler view

        mAdapter = new MessageAdapter(messagesList);
        mRefreshLayout = findViewById(R.id.swipe_message_list);

        mMessagesList = findViewById(R.id.messages_List);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        mTitleView.setText(mChatUserName);

        //setting up the online status of the user
        rootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")) {
                    mLastSeen.setText("Online");
                } else {
                    getTimeAgo GetTimeAgo = new getTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("Chat_activity", databaseError.getMessage());
            }
        });

        //For registering a chat between two users :)
        rootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seem", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null)
                                Log.i("CHAT_LOG", databaseError.getMessage());
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Adding onClick method for send button
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });


        mPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);

            }
        });

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(mCurrentPage * TOTAL_MESSAGES_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos, message);

                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;
                }


                Log.d("Total keys", "Last key: " + mLastKey + " Prev key: " + mPrevKey + " Message key: " + messageKey);

                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(itemPos, 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void loadMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_MESSAGES_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = mMessage.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String mCurrent_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String mChat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = rootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(mCurrent_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(mChat_user_ref + "/" + push_id, messageMap);

            mMessage.setText("");

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null)
                        Log.i("CHAT_LOG", databaseError.getMessage());
                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = rootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            final StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String download_url = uri.toString();


                                Map messageMap = new HashMap();
                                messageMap.put("message", download_url);
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUserId);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                mMessage.setText("");

                                rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        if (databaseError != null)
                                            Log.i("CHAT_LOG", databaseError.getMessage());
                                    }
                                });


                            }
                        });


                    }

                }
            });

        }


    }
}
