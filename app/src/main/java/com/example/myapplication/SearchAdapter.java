package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
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
import com.example.myapplication.com.example.myapplication.data.DiaryBook;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context mContext; // SearchActivity Context
    private SearchActivity searchActivity;
    private String useremail; // 사용자 이메일
    private ArrayList<DiaryBook> allDiaryBooks; // 모든 일기책 데이터
    private ArrayList<DiaryBook> filteredAllDiaryBooks; // 검색된 일기책 데이터
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<DiaryBook> filteredList = new ArrayList<>(); // 검색된 일기책 얻기

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(allDiaryBooks); // 입력값이 없으면 모든 일기책을 검색된 일기책에 넣어줍니다
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim(); // 검색어 얻기

                for (DiaryBook diaryBook : allDiaryBooks) {
                    if (diaryBook.getDiaryTitle1().toLowerCase().contains(filterPattern)
                            || diaryBook.getDiaryTitle2().toLowerCase().contains(filterPattern)) { // 검색어가 일기책 제목에 들어있는지 확인합니다
                        filteredList.add(diaryBook); // 검색어에 일기책 제목이 포함되면 검색된 일기책에 추가해줍니다
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList; // 결과 값에 검색된 일기책들을 넣어줍니다

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredAllDiaryBooks.clear(); // 검색된 일기책을 넣기전에 비워줍니다
            filteredAllDiaryBooks.addAll((ArrayList<DiaryBook>) results.values); // 검색된 일기책의 데이터를 넣어줍니다
            notifyDataSetChanged();
        }
    };

    public SearchAdapter(Context context, String useremail, ArrayList<DiaryBook> allDiaryBooks) {
        super();
        mContext = context;
        searchActivity = (SearchActivity) mContext;
        this.useremail = useremail;
        this.allDiaryBooks = allDiaryBooks;
        filteredAllDiaryBooks = new ArrayList<>(allDiaryBooks);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

        Glide.with(mContext).load(filteredAllDiaryBooks.get(position).getDiaryBookUri()).into(myViewHolder.searchImageView);
        myViewHolder.searchTextView.setText(filteredAllDiaryBooks.get(position).getDiaryBookTitleVertical());

        // 선택한 일기책을 보러 이동합니다
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WatchDiaryActivity.class);
                intent.putExtra("useremail", useremail); // 사용자 이메일
                intent.putExtra("diarybookkey", filteredAllDiaryBooks.get(position).getDiaryBookKey()); // 일기책 키 값
                searchActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredAllDiaryBooks.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView searchImageView;
        TextView searchTextView;

        public MyViewHolder(@NonNull View view) {
            super(view);
            searchImageView = view.findViewById(R.id.item_diary_title_image);
            searchTextView = view.findViewById(R.id.item_diary_title_text);
        }
    }
}
