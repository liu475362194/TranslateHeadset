package com.example.liu.translateheadset.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liu.translateheadset.R;

import java.util.List;

/**
 * Created by pzbz025 on 2018/2/5.
 */

public class FragmentAdapter extends FragmentPagerAdapter {
    private Context context;
    private List<Fragment> list;

    public FragmentAdapter(Context context, FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.context = context;
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

//    public View getTabView(int position){
//        View v = LayoutInflater.from(context).inflate(R.layout.item_tab, null);
//        ImageView imageView = v.findViewById(R.id.item_tab_img);
//        TextView textView = v.findViewById(R.id.item_tab_text);
//
//    }
}
