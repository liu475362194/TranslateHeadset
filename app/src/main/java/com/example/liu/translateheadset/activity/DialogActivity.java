package com.example.liu.translateheadset.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.liu.translateheadset.R;

public class DialogActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        findViewById(R.id.bt_relogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_relogin){
            ActivityCollector.removeAllActivity();
            startActivity(new Intent(DialogActivity.this,LoginActivity.class));
        }
    }
}
