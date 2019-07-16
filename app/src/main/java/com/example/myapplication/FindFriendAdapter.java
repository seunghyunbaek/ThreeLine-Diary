package com.example.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.User;

import java.util.ArrayList;

public class FindFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImageView;
        private TextView userNameView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.item_activity_find_friend_search_userimage);
            userNameView = itemView.findViewById(R.id.item_activity_find_friend_search_username);
        }
    }

    private AddFriendAdapter addFriendAdapter; // 친구추가 어댑터
    private ArrayList<User> allUsers; // 모든 유저 데이터
    private ArrayList<User> filteredUsers; // 필터링된 유저 데이터
    private Context mContext;

    public FindFriendAdapter(Context mContext, ArrayList<User> users, AddFriendAdapter addFriendAdapter) {
        super();
        this.mContext = mContext;
        this.allUsers = users;
        filteredUsers = new ArrayList<User>();
        this.addFriendAdapter = addFriendAdapter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_activity_find_friend_search, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

        myViewHolder.userNameView.setText(filteredUsers.get(position).getUserName()); // 유저 이름 설정하기
        if (filteredUsers.get(position).getUserUri() != null) {// 유저 프로필 이미지 설정하기
//            myViewHolder.userImageView.setImageURI(filteredUsers.get(position).getUserUri());
            Glide.with(mContext).load(filteredUsers.get(position).getUserUri()).into(myViewHolder.userImageView);
        }

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendAdapter.addFriend(filteredUsers.get(position)); // 같이 쓸 유저 추가하기
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<User> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.clear();
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim(); // 검색어 얻기

                for (User user : allUsers) {
                    if (user.getUserName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredUsers.clear();
            filteredUsers.addAll((ArrayList<User>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return itemFilter;
    }
}
