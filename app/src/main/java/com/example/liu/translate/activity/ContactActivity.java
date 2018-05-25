package com.example.liu.translate.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.liu.translate.DemoApplication;
import com.example.liu.translate.R;
import com.example.liu.translate.db.EaseUser;
import com.example.liu.translate.util.EaseCommonUtils;
import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ContactActivity extends Fragment {

	protected List<EaseUser> contactList = new ArrayList<EaseUser>();
	protected ListView listView;
	private Map<String, EaseUser> contactsMap;
	private ContactAdapter adapter;

	private static final String TAG = "ContactActivity";
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
//		setContentView(R.layout.activity_contact);
//		this.findViewById(R.id.btn_add).setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(ContactActivity.this, AddContactActivity.class));
//			}
//
//		});
//		ImageView imageView = this.findViewById(R.id.title_bar_right);
//		imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_add_pressed));
//		imageView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(ContactActivity.this, AddContactActivity.class));
//			}
//		});



//		listView = (ListView) this.findViewById(R.id.listView);
//		getContactList();
//		adapter = new ContactAdapter(this, contactList);
//		listView.setAdapter(adapter);
//		listView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				startActivity(new Intent(ContactActivity.this,ChatActivity.class).putExtra("username", adapter.getItem(arg2).getUsername()));
//				finish();
//			}
//
//		});


	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_contact,container,false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.listView);
		getContactList();
		adapter = new ContactAdapter(getActivity(), contactList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				startActivity(new Intent(getActivity(),ChatActivity.class).putExtra("username", adapter.getItem(arg2).getUsername()));
//				getActivity().finish();
			}

		});

		EMClient.getInstance().contactManager().setContactListener(new EMContactListener() {


			@Override
			public void onContactInvited(String username, String reason) {
				//收到好友邀请
				Log.d(TAG, "onContactInvited: 收到好友邀请");
			}

			@Override
			public void onFriendRequestAccepted(String s) {
				Log.d(TAG, "onFriendRequestAccepted: 好友请求被同意");
			}

			@Override
			public void onFriendRequestDeclined(String s) {
				Log.d(TAG, "onFriendRequestDeclined: 好友请求被拒绝");
			}

			@Override
			public void onContactDeleted(String username) {
				//被删除时回调此方法
				Log.d(TAG, "onContactDeleted: 被删除时回调此方法");
			}


			@Override
			public void onContactAdded(String username) {
				//增加了联系人时回调此方法
				getFriends();
				getContactList();
				listView.post(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
				Log.d(TAG, "onContactAdded:增加了联系人时回调此方法 ");
			}
		});

	}

	/**
	 * 获取联系人列表，并过滤掉黑名单和排序
	 */
	protected void getContactList() {
		contactList.clear();
		// 获取联系人列表
		contactsMap = DemoApplication.getInstance().getContactList();
		if (contactsMap == null) {
			return;
		}
		synchronized (this.contactsMap) {
			Iterator<Entry<String, EaseUser>> iterator = contactsMap.entrySet().iterator();
			List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();
			while (iterator.hasNext()) {
				Entry<String, EaseUser> entry = iterator.next();
				// 兼容以前的通讯录里的已有的数据显示，加上此判断，如果是新集成的可以去掉此判断
				if (!entry.getKey().equals("item_new_friends") && !entry.getKey().equals("item_groups")
						&& !entry.getKey().equals("item_chatroom") && !entry.getKey().equals("item_robots")) {
					if (!blackList.contains(entry.getKey())) {
						// 不显示黑名单中的用户
						EaseUser user = entry.getValue();
						EaseCommonUtils.setUserInitialLetter(user);
						contactList.add(user);
					}
				}
			}
		}

		// 排序
		Collections.sort(contactList, new Comparator<EaseUser>() {

			@Override
			public int compare(EaseUser lhs, EaseUser rhs) {
				if (lhs.getInitialLetter().equals(rhs.getInitialLetter())) {
					return lhs.getNick().compareTo(rhs.getNick());
				} else {
					if ("#".equals(lhs.getInitialLetter())) {
						return 1;
					} else if ("#".equals(rhs.getInitialLetter())) {
						return -1;
					}
					return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
				}

			}
		});

	}

	private  void  getFriends(){
		try {
			List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
			Map<String ,EaseUser>users=new HashMap<String ,EaseUser>();
			for(String username:usernames){
				EaseUser user=new EaseUser(username);
				users.put(username, user);


			}

			DemoApplication.getInstance().setContactList(users);


		} catch (HyphenateException e) {
			e.printStackTrace();
		}

	}

	class ContactAdapter extends BaseAdapter {
		private Context context;
		private List<EaseUser> users;
		private LayoutInflater inflater;

		public ContactAdapter(Context context_, List<EaseUser> users) {

			this.context = context_;
			this.users = users;
			inflater = LayoutInflater.from(context);

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return users.size();
		}

		@Override
		public EaseUser getItem(int position) {
			// TODO Auto-generated method stub
			return users.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {

				convertView = inflater.inflate(R.layout.item_contact, parent, false);

			}

			TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			tv_name.setText(getItem(position).getUsername());

			return convertView;
		}

	}

}
