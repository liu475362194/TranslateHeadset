package com.example.liu.translateheadset.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.liu.translateheadset.R;

public class DialogActivity extends BaseActivity implements View.OnClickListener {

//    @BindView(R.id.tv_content)
    TextView tvContent;
//    @BindView(R.id.bt_relogin)
    Button btRelogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
//        ButterKnife.bind(this);
        tvContent = findViewById(R.id.tv_content);
        btRelogin = findViewById(R.id.bt_relogin);
        btRelogin.setOnClickListener(this);
        Intent intent = getIntent();
        String str = intent.getStringExtra("error_str");
        if (null != str){
            tvContent.setText(str);
        } else {
            tvContent.setText("出现异常，请重新登录。");
        }

    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_relogin) {
            ActivityCollector.removeAllActivity();
            startActivity(new Intent(DialogActivity.this, LoginActivity.class));
        }
    }
}
