package com.example.liu.translate.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.liu.translate.R;

public class TipsActivity extends Activity {
    RelativeLayout relativeLayout;
    ImageView imageView;
    int mImageRes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//取消title
        setContentView(R.layout.activity_tips);
        mImageRes = getIntent().getIntExtra("imageRes",R.drawable.local_translate);
        initView();
        close();
    }

    public static void openTip(Context context, int imageRes){
        Intent intent = new Intent(context,TipsActivity.class);
        intent.putExtra("imageRes",imageRes);
        context.startActivity(intent);
    }

    private void initView(){
        relativeLayout = findViewById(R.id.relative);
        imageView = findViewById(R.id.image);
        imageView.setImageResource(mImageRes);
    }

    private void close(){
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0,0);
            }
        });
    }
}
