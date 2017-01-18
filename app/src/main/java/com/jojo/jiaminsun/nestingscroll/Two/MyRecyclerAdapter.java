package com.jojo.jiaminsun.nestingscroll.Two;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jojo.jiaminsun.nestingscroll.R;
import com.jojo.jiaminsun.nestingscroll.Util;

/**
 * Created by sunjiamin on 2017/1/18.
 */

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {
    @Override
    public MyRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Util.dp2px(parent.getContext(), 50)));
        textView.setBackgroundResource(R.drawable.list_item_bg_with_border_bottom);
        int paddingHor = Util.dp2px(parent.getContext(), 16);
        textView.setPadding(paddingHor, 0, paddingHor, 0);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextSize(16);
        return new MyViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
         holder.setText("item " + position);
    }

    @Override
    public int getItemCount() {
        return 50;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView mItemView;

        public MyViewHolder(TextView itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public void setText(String text) {
            mItemView.setText(text);
        }
    }
}
