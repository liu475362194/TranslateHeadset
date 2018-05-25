package com.example.liu.translate.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liu.translate.R;
import com.example.liu.translate.adapter.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainTabActivity extends AppCompatActivity {

//    @BindView(R.id.view_pager)
    ViewPager viewPager;
//    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    private List<Fragment> list;
    private FragmentPagerAdapter adapter;

    private String [] titles = {"会话","通讯录","添加好友","申请列表"};
    private int [] images = {R.drawable.message,R.drawable.friend_list,R.drawable.add_friend,R.drawable.friend_new};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
//        ButterKnife.bind(this);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        list = new ArrayList<>();
        list.add(new ConversationActivity());
        list.add(new ContactActivity());
//        list.add(new NewFriendsMsgActivity());
        list.add(new AddContactActivity());

//        for (int i = 0; i < 4; i++) {
//            tabLayout.addTab(tabLayout.newTab().setText(titles[i]).setIcon(images[i]));
//        }

        adapter = new FragmentAdapter(this,getSupportFragmentManager(),list);
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(getTabView(i));
        }
    }

    private View getTabView(int position){
        View v = LayoutInflater.from(MainTabActivity.this).inflate(R.layout.item_tab, null);
        TextView textView = v.findViewById(R.id.item_tab_text);
        ImageView imageView = v.findViewById(R.id.item_tab_img);
        textView.setText(titles[position]);
        textView.setTextColor(tabLayout.getTabTextColors());
        imageView.setImageResource(images[position]);
//        textView.setTextColor(Color.RED);
        return v;
    }
}
