package com.example.liu.translate.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.liu.translate.R;
import com.example.liu.translate.gson.TalkAll;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pzbz025 on 2017/11/21.
 */

public class TalkAdapter extends RecyclerView.Adapter<TalkAdapter.ViewHolder> {

    private List<TalkAll> talkAlls = new ArrayList<>();
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private ItemClickListener mItemClickListener;
    private static final String TAG = "TalkAdapter";

    public interface ItemClickListener{
        void onClick(int position);
    }

    public void setmItemClickListener(ItemClickListener itemClickListener){
        mItemClickListener = itemClickListener;
    }

    public TalkAdapter(List<TalkAll> talkAlls) {
        this.talkAlls = talkAlls;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0){
            view = View.inflate(parent.getContext(),R.layout.item_message_received,null);
        } else {
            view = View.inflate(parent.getContext(),R.layout.item_message_sent,null);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return talkAlls.get(position).getWho();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        if (talkAlls.get(position).getWho() == 0){
            holder.text.setText(talkAlls.get(position).getTranslateText());
            holder.yuanWen.setText(talkAlls.get(position).getYuanWen());
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getLayoutPosition(); // 1
                    mItemClickListener.onClick(position);
                }
            });
//        } else {
//            holder.right.setTranslateText(talkAlls.get(position).getTranslateText());
//        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()){
            onBindViewHolder(holder, position);
        } else {
            String payload = (String) payloads.get(0);
            Log.d(TAG, "onBindViewHolder:payload " + payload);
            holder.yuanWen.setText(talkAlls.get(position).getYuanWen());
        }
    }

    @Override
    public int getItemCount() {
        return talkAlls.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView text;
        private TextView yuanWen;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            text = itemView.findViewById(R.id.tv_chatcontent_translate);
            yuanWen = itemView.findViewById(R.id.tv_chatcontent);
        }

    }
}
