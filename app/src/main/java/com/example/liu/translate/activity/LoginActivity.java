package com.example.liu.translate.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liu.translate.DemoApplication;
import com.example.liu.translate.R;
import com.example.liu.translate.TranslateActivity;
import com.example.liu.translate.db.DemoDBManager;
import com.example.liu.translate.db.EaseUser;
import com.example.liu.translate.util.EaseCommonUtils;
import com.example.liu.translate.view.LayoutTitleBar;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登陆页面
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private ViewStub viewStub;

	private boolean progressShow;
	private boolean autoLogin = false;

	private String currentUsername;
	private String currentPassword;

	//此处更改是否启用登陆功能。
	private boolean isCanChat = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initPermission();


		setContentView(R.layout.activity_login);


		if (isCanChat) {
			// 如果登录成功过，直接进入主页面
			if (EMClient.getInstance().isLoggedInBefore()) {
				autoLogin = true;
				startActivity(new Intent(LoginActivity.this, MainTabActivity.class));
				finish();
				return;
			}
			viewStub = findViewById(R.id.view_stub);
			viewStub.inflate();

			usernameEditText = (EditText) findViewById(R.id.username);
			passwordEditText = (EditText) findViewById(R.id.password);

			// 如果用户名改变，清空密码
			usernameEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					passwordEditText.setText(null);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});
		} else {
			LayoutTitleBar layoutTitleBar = findViewById(R.id.title_bar);
			layoutTitleBar.setTitleBarTitle("翻译");
		}
		 
	}

	/**
	 * 登录 
	 */
	public void login(View view) {
		if (!EaseCommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
			return;
		}
		currentUsername = usernameEditText.getText().toString().trim();
		currentPassword = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(currentUsername)) {
			Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(currentPassword)) {
			Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}

		progressShow = true;
		final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "EMClient.getInstance().onCancel");
				progressShow = false;
			}
		});
		pd.setMessage(getString(R.string.Is_landing));
		pd.show();
 		// close it before login to make sure DemoDB not overlap
        DemoDBManager.getInstance().closeDB();
        // reset current user name before login
        DemoApplication.getInstance().setCurrentUserName(currentUsername);
		// 调用sdk登陆方法登陆聊天服务器
		Log.d(TAG, "EMClient.getInstance().login");
		EMClient.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "login: onSuccess");

				if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
					pd.dismiss();
				}

				// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
				// ** manually load all local groups and
			    EMClient.getInstance().groupManager().loadAllGroups();
			    EMClient.getInstance().chatManager().loadAllConversations();
				getFriends();
 
				// 进入主页面
				Intent intent = new Intent(LoginActivity.this,
						MainTabActivity.class);
				startActivity(intent);

				finish();
			}

			@Override
			public void onProgress(int progress, String status) {
				Log.d(TAG, "login: onProgress");
			}

			@Override
			public void onError(final int code, final String message) {
				Log.d(TAG, "login: onError: " + code);
				if (!progressShow) {
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + "用户名或密码错误",
								Toast.LENGTH_SHORT).show();
					}
				});
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

	/**
	 * android 6.0 以上需要动态申请权限
	 */
	private void initPermission() {
		String permissions[] = {
				Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.ACCESS_NETWORK_STATE,
//                Manifest.permission.MODIFY_AUDIO_SETTINGS,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_SETTINGS,
				Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.ACCESS_WIFI_STATE,
//                Manifest.permission.CHANGE_WIFI_STATE
		};

		ArrayList<String> toApplyList = new ArrayList<String>();

		for (String perm : permissions) {
			if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
				toApplyList.add(perm);
				//进入到这里代表没有权限.
//                Toast.makeText(SplashActivity.this, "未获取到权限，请重新打开！", Toast.LENGTH_SHORT).show();
//                finish();
//                Toast.makeText(SplashActivity.this,"没权限",Toast.LENGTH_SHORT).show();
			}
		}
		String tmpList[] = new String[toApplyList.size()];
		if (!toApplyList.isEmpty()) {
			ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
		} else {

		}
	}
	
	
	/**
	 * 注册
	 * 
	 * @param view
	 */
	public void register(View view) {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	public void local(View view){
		Intent intent = new Intent(this, TranslateActivity.class);
		intent.putExtra("left",true);
		intent.putExtra("right",true);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}

	public void local1in1out(View view) {
		Intent intent = new Intent(this, TranslateActivity.class);
		intent.putExtra("left",false);
		intent.putExtra("right",true);
		startActivity(intent);
	}
}
