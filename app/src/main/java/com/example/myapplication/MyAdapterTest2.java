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

public class MyAdapterTest2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int WATCH_DIARY = 11;
    private Context mContext;
    private MainActivity mainActivity;
    private String useremail; // 사용자의 이메일
    private ArrayList<DiaryBook> allDiaryBooks; // 모든 일기책

    MyAdapterTest2(Context context, String useremail, ArrayList<DiaryBook> allDiaryBooks) {
        this.mContext = context; // 액티비티의 컨텍스트?
        mainActivity = (MainActivity) mContext; // 메인 액티비티 찾음
        this.useremail = useremail;
        this.allDiaryBooks = allDiaryBooks;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // 아이템 뷰 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
        // 아이템 뷰에 데이터 넣기
        Glide.with(mContext).load(allDiaryBooks.get(position).getDiaryBookUri())
                .into(myViewHolder.fragmentMainDiaryTitleImage);
        myViewHolder.fragmentMainDiaryTitleText.setText(allDiaryBooks.get(position).getDiaryBookTitleVertical());

        // 아이템 뷰를 눌렀을 때 할일
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 일기책 보러가기
                Intent intent = new Intent(mContext, WatchDiaryActivity.class);
                intent.putExtra("useremail", useremail);
                intent.putExtra("diarybookkey", allDiaryBooks.get(position).getDiaryBookKey());
                mainActivity.startActivityForResult(intent, WATCH_DIARY);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allDiaryBooks.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView fragmentMainDiaryTitleImage;
        TextView fragmentMainDiaryTitleText;

        public MyViewHolder(@NonNull View view) {
            super(view);
            fragmentMainDiaryTitleImage = view.findViewById(R.id.item_diary_title_image);
            fragmentMainDiaryTitleText = view.findViewById(R.id.item_diary_title_text);
        }
    }

}
