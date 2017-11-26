/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.List;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.app.BaseActivity;
import com.smileapps.mypassword.view.LockPatternUtil;
import com.smileapps.mypassword.view.LockPatternView;
import com.smileapps.mypassword.view.LockPatternView.Cell;
import com.smileapps.mypassword.view.LockPatternView.DisplayMode;
import com.smileapps.mypassword.view.LockPatternView.OnPatternListener;
import cn.zdx.lib.annotation.FindViewById;

/**
 * 매인 인트로
 *
 * @author zeus8502
 */
public class EntryActivity extends BaseActivity implements Callback, OnPatternListener {
    private final int MESSAGE_START_MAIN = 1;
    private final int MESSAGE_CLEAR_LOCKPATTERNVIEW = 3;
    private final int MESSAGE_START_SETLOCKPATTERN = 4;
    @FindViewById(R.id.entry_activity_iconview)
    private View iconView;
    private Handler handler;
    @FindViewById(R.id.entry_activity_bg)
    private View backgroundView;

    @FindViewById(R.id.entry_activity_lockPatternView)
    private LockPatternView lockPatternView;

    @FindViewById(R.id.entry_activity_tips)
    private TextView tipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        handler = new Handler(this);
        lockPatternView.setOnPatternListener(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        List<Cell> cells = LockPatternUtil.getLocalCell(this);
        if (cells.size() == 0) {
            // 처음으로 앱이 시작됨, 암호(패턴)잠금 생성 요구
            lockPatternView.setEnabled(false);
            handler.sendEmptyMessageDelayed(MESSAGE_START_SETLOCKPATTERN, 2000);
        }

        tipsView.setText("");
        initAnimation();
        checkPackageName();
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Intent intent_resume;
        intent_resume = new Intent(this, EntryActivity.class);
        startActivity(intent_resume);
        finish();
    }

    /**
     * 불법 위조, 패키지명 변경 방지, 패키지명 변경 발견시 앱 종료
     */
    private void checkPackageName() {
        if (!getPackageName().equals(getString(R.string.package_name)))
            finish();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_START_SETLOCKPATTERN:
                startActivity(new Intent(this, SetLockpatternActivity.class));
                finish();
                break;

            case MESSAGE_START_MAIN:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;

            case MESSAGE_CLEAR_LOCKPATTERNVIEW:
                lockPatternView.clearPattern();
                tipsView.setText("");
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 아이콘 애니메이션
     */
    private void initAnimation() {
        Animation iconAnimation = AnimationUtils.loadAnimation(this, R.anim.entry_animation_icon);
        iconView.startAnimation(iconAnimation);

        backgroundView.startAnimation(getAlpAnimation());
        lockPatternView.startAnimation(getAlpAnimation());
        tipsView.startAnimation(getAlpAnimation());
    }

    private Animation getAlpAnimation() {
        return AnimationUtils.loadAnimation(this, R.anim.entry_animation_alpha_from_0_to_1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPatternStart() {
        handler.removeMessages(MESSAGE_CLEAR_LOCKPATTERNVIEW);
        tipsView.setText("");
    }

    @Override
    public void onPatternCleared() {
    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {
    }

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        if (LockPatternUtil.checkPatternCell(LockPatternUtil.getLocalCell(this), pattern)) {
            // 인증 승인
            lockPatternView.setDisplayMode(DisplayMode.Correct);
            handler.sendEmptyMessage(MESSAGE_START_MAIN);
        } else {
            // 인증 거부
            lockPatternView.setDisplayMode(DisplayMode.Wrong);
            tipsView.setText(R.string.lock_pattern_error);
            handler.sendEmptyMessageDelayed(MESSAGE_CLEAR_LOCKPATTERNVIEW, 1000);
        }

    }
}
