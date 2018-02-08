package com.example.liu.translateheadset.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;


import com.example.liu.translateheadset.R;
import com.example.liu.translateheadset.activity.adapter.NewFriendsMsgAdapter;
import com.example.liu.translateheadset.db.InviteMessage;
import com.example.liu.translateheadset.db.InviteMessgeDao;

import java.util.List;


/**
 * 申请与通知
 *
 */
public class NewFriendsMsgActivity extends Fragment {
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_new_friends_msg);

//		ImageView imageView = this.findViewById(R.id.title_bar_right);
//		imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_add_pressed));
//		imageView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(NewFriendsMsgActivity.this, AddContactActivity.class));
//			}
//		});
//
//		listView = (ListView) findViewById(R.id.list);
//		InviteMessgeDao dao = new InviteMessgeDao(this);
//		List<InviteMessage> msgs = dao.getMessagesList();
// 		NewFriendsMsgAdapter adapter = new NewFriendsMsgAdapter(this, 1, msgs);
//		listView.setAdapter(adapter);
//		dao.saveUnreadMessageCount(0);
		
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_new_friends_msg,container,false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

//		ImageView imageView = view.findViewById(R.id.title_bar_right);
//		imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_add_pressed));
//		imageView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(getActivity(), AddContactActivity.class));
//			}
//		});

		listView = (ListView) view.findViewById(R.id.list);
		InviteMessgeDao dao = new InviteMessgeDao(getActivity());
		List<InviteMessage> msgs = dao.getMessagesList();
		NewFriendsMsgAdapter adapter = new NewFriendsMsgAdapter(getActivity(), 1, msgs);
		listView.setAdapter(adapter);
		dao.saveUnreadMessageCount(0);
	}

	public void back(View view) {
		getActivity().finish();
	}
	
	
}
