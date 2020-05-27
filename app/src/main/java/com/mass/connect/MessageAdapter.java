package com.mass.connect;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by abhinav on 24/5/20.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    //    Our Model class i.e Messages is used to retrieve data from the firebase and use
//    this adapter is used to load that data inside the recycler view.
    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.MessageViewHolder holder, int position) {

        String current_user_id = mAuth.getCurrentUser().getUid();

        final Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        String type = c.getType();

        mUsersDatabase.child(from_user).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String DisplayName = (String) dataSnapshot.getValue();
                holder.messageDisplayName.setText(DisplayName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.profileImage.setImageResource(R.drawable.default_avatar);

        if (type.equals("text")) {

            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }else{

            holder.messageText.setVisibility(View.INVISIBLE);

            Picasso.get().load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(holder.messageImage);

        }


        if (from_user.equals(current_user_id)) {


//            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);

        } else {

//            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.BLACK);
        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView messageDisplayName;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            messageDisplayName = view.findViewById(R.id.message_Display_Name);
            messageImage = view.findViewById(R.id.message_image);
        }

    }

}
