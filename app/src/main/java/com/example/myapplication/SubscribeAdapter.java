package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;

import java.util.ArrayList;

public class SubscribeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int WATCH_DIARY = 11;

    private Context mContext;
    private MainActivity mainActivity;
    private SubscribeFragment subscribeFragment;
    private String useremail; // 사용자 이메일
    private ArrayList<DiaryBook> subscribeDiaryBooks; // 구독중인 일기책들

    public SubscribeAdapter(Context context, String useremail, ArrayList<DiaryBook> subscribeDiaryBooks) {
        mContext = context;
        this.useremail = useremail;
        this.subscribeDiaryBooks = subscribeDiaryBooks;
        mainActivity = (MainActivity) context;
    }

    public void setSubscribeDiaryBooks(ArrayList<DiaryBook> subscribeDiaryBooks) {
        this.subscribeDiaryBooks = subscribeDiaryBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diary, viewGroup, false);
        SubscribeMyViewHolder subscribeMyViewHolder = new SubscribeMyViewHolder(view);

        return subscribeMyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        SubscribeMyViewHolder subscribeMyViewHolder = (SubscribeMyViewHolder) viewHolder;

        Glide.with(mContext).load(subscribeDiaryBooks.get(position).getDiaryBookUri()).into(subscribeMyViewHolder.diaryTitleImageView);
        subscribeMyViewHolder.diaryTitleTextView.setText(subscribeDiaryBooks.get(position).getDiaryBookTitleVertical());

        subscribeMyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WatchDiaryActivity.class); // 일기책 보러가기
                intent.putExtra("useremail", useremail); // 사용자 이메일
                intent.putExtra("diarybookkey", subscribeDiaryBooks.get(position).getDiaryBookKey()); // 구독중인 일기책중 보려는 일기책 키값
//                mainActivity.startActivity(intent);
                mainActivity.startActivityForResult(intent, WATCH_DIARY);

            }
        });
    }

    @Override
    public int getItemCount() {
        return subscribeDiaryBooks.size();
    }

    public class SubscribeMyViewHolder extends RecyclerView.ViewHolder {
        ImageView diaryTitleImageView;
        TextView diaryTitleTextView;

        public SubscribeMyViewHolder(@NonNull View itemView) {
            super(itemView);
            diaryTitleImageView = itemView.findViewById(R.id.item_diary_title_image);
            diaryTitleTextView = itemView.findViewById(R.id.item_diary_title_text);
        }
    }

}
