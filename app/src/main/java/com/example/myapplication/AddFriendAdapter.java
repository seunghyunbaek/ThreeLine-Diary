package com.example.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.User;

import java.util.ArrayList;

class AddFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<User> users;
    private String useremail;
    private ImageView nextImage;

    public AddFriendAdapter(Context mContext, User user, String useremail, ImageView nextImage) {
        super();

        this.mContext = mContext;

        users = new ArrayList<>();
        users.add(user);

        this.useremail = useremail;
        this.nextImage = nextImage;
    }

    // 같이 쓸 친구 추가하기
    public void addFriend(User user) { // user는 추가하려는 유저

        // 사용자는 추가로 추가되지 않는다
        if (user.getEmail().equals(useremail))
            return;

        // 친구가 이미 추가되었으면 더 이상 추가하지 않음 ( 친구 1명만 같이 쓸 때 )
//        if (users.size() > 1)
//            return;
        // 이미 추가되었는지 확인하기 ( 친구 여러명과 같이 쓸 때 )
        for (User addedUser : users) {
            if (addedUser.getEmail().equals(user.getEmail())) { // 이미 추가된 경우 더이상 추가되지 않는다
                return;
            }
        }

        users.add(user);
        nextImage.setVisibility(View.VISIBLE);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_activity_find_friend_add, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

        myViewHolder.addFriendUserNameView.setText(users.get(position).getUserName()); // 같이쓸 유저 이름
        if (users.get(position).getUserUri() != null) {
//            myViewHolder.addFriendUerImageView.setImageURI(users.get(position).getUserUri()); // 같이 쓸 유저 프로필 이미지
            Glide.with(mContext).load(users.get(position).getUserUri()).into(myViewHolder.addFriendUerImageView);
        } else
            myViewHolder.addFriendUerImageView.setImageResource(R.drawable.pic_user_profile);

        // 같이 쓸 유저 제거하기 (한명이랑 쓸 때)
//        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (position > 0) {
//                    users.remove(position);
//                    nextImage.setVisibility(View.GONE);
//                    notifyDataSetChanged();
//                }
//            }
//        });
        // 여러명이랑 쓸 때
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    users.remove(position);
                } else {
                    nextImage.setVisibility(View.GONE);
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // 같이쓸 유저의 이메일 반환 (한명이랑 쓸 때)
    public String getJoinUserEmail() {
        if (users.size() > 1)
            return users.get(users.size() - 1).getEmail();
        return "";
    }

    // 여러명이랑 같이 쓸 때
    public ArrayList<String> getJoinUsersEmail() {
        ArrayList<String> joinusersemail = new ArrayList<String>();
        for (User joinUser : users) {
            joinusersemail.add(joinUser.getEmail());
        }
        return joinusersemail;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView addFriendUerImageView;
        private TextView addFriendUserNameView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            addFriendUerImageView = itemView.findViewById(R.id.item_activity_find_add_friend_userimage);
            addFriendUserNameView = itemView.findViewById(R.id.item_activity_find_add_friend_username);
        }
    }
}
