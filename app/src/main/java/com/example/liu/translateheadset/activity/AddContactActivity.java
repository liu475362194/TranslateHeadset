package com.example.liu.translateheadset.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liu.translateheadset.DemoApplication;
import com.example.liu.translateheadset.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;


public class AddContactActivity extends Fragment {

 
	private ProgressDialog progressDialog;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_add_contact);
//		final EditText et_username = (EditText) this.findViewById(R.id.et_username);
//		Button btn_add = (Button) this.findViewById(R.id.btn_add);
//		btn_add.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				String username = et_username.getText().toString().trim();
//
//				if (TextUtils.isEmpty(username)) {
//
//					Toast.makeText(getApplicationContext(), "请输入内容...", Toast.LENGTH_SHORT).show();
//					return;
//
//				}
//				addContact(username);
//			}
//
//		});

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_add_contact,container,false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final EditText et_username = (EditText) view.findViewById(R.id.et_username);
		Button btn_add = (Button) view.findViewById(R.id.btn_add);
		btn_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = et_username.getText().toString().trim();

				if (TextUtils.isEmpty(username)) {

					Toast.makeText(getActivity(), "请输入内容...", Toast.LENGTH_SHORT).show();
					return;

				}
				addContact(username);
			}

		});
		Button btn_login_out = view.findViewById(R.id.btn_login_out);
		btn_login_out.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("提示");
				builder.setMessage("是否确定退出登录？");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						logout();
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				});
				builder.show();
			}
		});
	}

	private void logout() {
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st = getResources().getString(R.string.Are_logged_out);
		pd.setMessage(st);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		DemoApplication.getInstance().logout(false, new EMCallBack() {

			@Override
			public void onSuccess() {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						// 重新显示登陆页面
						getActivity().finish();
						startActivity(new Intent(getActivity(), LoginActivity.class));

					}
				});
			}

			@Override
			public void onProgress(int progress, String status) {

			}

			@Override
			public void onError(int code, String message) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						pd.dismiss();
						Toast.makeText(getActivity(), "unbind devicetokens failed", Toast.LENGTH_SHORT).show();


					}
				});
			}
		});
	}



	/**
	 * 添加contact
	 * 
	 * @param
	 */
	public void addContact(final String username) {
		progressDialog = new ProgressDialog(getActivity());
		String stri = getResources().getString(R.string.Is_sending_a_request);
		progressDialog.setMessage(stri);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		new Thread(new Runnable() {
			public void run() {

				try {
					// demo写死了个reason，实际应该让用户手动填入
					String s = getResources().getString(R.string.Add_a_friend);
					EMClient.getInstance().contactManager().addContact(username, s);
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s1 = getResources().getString(R.string.send_successful);
							Toast.makeText(getActivity(), s1, Toast.LENGTH_SHORT).show();
						}
					});
				} catch (final Exception e) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s2 = getResources().getString(R.string.Request_add_buddy_failure);
							Toast.makeText(getActivity(), s2 + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}

	public void back(View v) {
		getActivity().finish();
	}
}
