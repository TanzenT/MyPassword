package com.smileapps.mypassword.activity;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.app.BaseActivity;
import cn.zdx.lib.annotation.FindViewById;

/**
 * 정보 액티비티
 *
 * @author zeus8502
 */
public class AboutActivity extends BaseActivity {

    /** 개발자 블로그 */
    private static final String DEVELOPER_BLOG = "http://smileapps.ga";

    /** 앱 버전 */
    @FindViewById(R.id.about_version)
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initActionBar();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        /** 앱 버전 */
        textView.setText(getMyApplication().getVersionName());
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Intent intent_resume;
        intent_resume = new Intent(this, EntryActivity.class);
        startActivity(intent_resume);
        finish();
    }

    public void onFeedbackClick(View view) {

    }

    public void onBlogClick(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(DEVELOPER_BLOG));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showToast(R.string.about_source_open_failed);
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}