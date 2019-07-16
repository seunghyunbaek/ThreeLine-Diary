package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.com.example.myapplication.data.DiaryBook;

import java.util.ArrayList;

class SelectWriteDiaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private String useremail; // 사용자의 이메일
    private ArrayList<DiaryBook> userDiaryBooks; // 사용자의 모든 일기책 목록
    public SelectWriteDiaryAdapter(Context mContext, String useremail, ArrayList<DiaryBook> userDiaryBooks) {
        this.mContext = mContext;
        this.useremail = useremail;
        this.userDiaryBooks = userDiaryBooks;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.iitem_select_write_diary, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
        myViewHolder.textView.setText(userDiaryBooks.get(position).getDiaryBookTitleHorizontal());

        // 선택한 일기책에 작성한 일기가 추가된다
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 일기 작성하는 액티비티로 이동
                Intent intent = new Intent(mContext, WriteDiaryActivity.class);
                intent.putExtra("useremail", useremail); // 사용자의 이메일
                intent.putExtra("diarybookkey", userDiaryBooks.get(position).getDiaryBookKey()); // 일기책 키 값
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userDiaryBooks.size();
    } // 일기책 개수

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_select_write_diary_textview);
        }
    }
}
