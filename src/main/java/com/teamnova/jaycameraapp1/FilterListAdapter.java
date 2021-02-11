package com.teamnova.jaycameraapp1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
// 메인액티비의 필터 리사이클러뷰 어댑터. FilterBtn클래스를 item으로 사용한다.
public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.FilterViewHolder> {
    //필터버튼을 담는 어레이 리스트.
    private ArrayList<FilterBtn> itemList;

    //어댑터 생성시에 액티비티의 컨텍스트를 가져온다.
    private Context context;

    //리스너 객체를 외부에서 받아올 수 있도록 변수를 생성해 주었다.
    private View.OnClickListener onClickItem;

    public FilterListAdapter(ArrayList<FilterBtn> itemList, Context context, View.OnClickListener onClickItem) {
        this.itemList = itemList;
        this.context = context;
        this.onClickItem = onClickItem;
    }

    @NonNull
    @Override
    public FilterListAdapter.FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_filter_btn,parent,false);

        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterListAdapter.FilterViewHolder holder, int position) {
        FilterBtn item = itemList.get(position);

        //버튼의 텍스트에 필터이름을 입력해준다.
        holder.textView.setText(item.name);
        //FilterBtn 클래스를
        holder.textView.setTag(item);
        holder.textView.setOnClickListener(onClickItem);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    //static 의 장점은 다른 어댑터에서도 사용할 수 있다는 것이 었다. 다른 곳에서 사용하지않고, 큰의미가 없으르로 생략함.
    public class FilterViewHolder extends  RecyclerView.ViewHolder{
        public TextView textView;
        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_filter_btn_name);
        }
    }
}
