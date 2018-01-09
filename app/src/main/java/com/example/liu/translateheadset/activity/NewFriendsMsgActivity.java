package com.example.liu.translateheadset.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
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
public class NewFriendsMsgActivity extends BaseActivity {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_friends_msg);

		listView = (ListView) findViewById(R.id.list);
		InviteMessgeDao dao = new InviteMessgeDao(this);
		List<InviteMessage> msgs = dao.getMessagesList();
 		NewFriendsMsgAdapter adapter = new NewFriendsMsgAdapter(this, 1, msgs);
		listView.setAdapter(adapter);
		dao.saveUnreadMessageCount(0);
		
	}

	public void back(View view) {
		finish();
	}
	
	
}
