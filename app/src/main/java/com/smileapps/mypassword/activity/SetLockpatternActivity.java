package com.smileapps.mypassword.activity;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
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
 * 암호(패턴)잠금 설정
 *
 * @author zeus8502
 */
public class SetLockpatternActivity extends BaseActivity implements OnPatternListener, Callback {
    /** 인증 모드 */
    private static final int MODE_AUTH = 0;
    /** 첫번째 순서 */
    private static final int MODE_FIRST_STEP = 1;
    /** 두번째 순서 */
    private static final int MODE_SECOND_STEP = 2;
    private static final int MEG_AUTH_ERROR = 1;
    private static final int MEG_GOTO_SECOND_STEP = 2;
    private static final int MEG_SET_SUCCESS = 3;
    private static final int MEG_GOTO_FIRST_STEP = 4;
    @FindViewById(R.id.set_lockpattern_view)
    private LockPatternView lockPatternView;
    @FindViewById(R.id.set_lockpattern_text)
    private TextView textView;
    /** 현재 모드 */
    private int mode = MODE_AUTH;
    private Handler handler = new Handler(this);
    private List<Cell> lastCells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_lockpattern);
        initActionBar();
        lockPatternView.setOnPatternListener(this);
        initMode();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
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
     * {@link #MODE_AUTH}
     */
    private void initMode() {
        List<LockPatternView.Cell> list = LockPatternUtil.getLocalCell(this);
        if (list.size() != 0) {
            mode = MODE_AUTH;
            textView.setText(R.string.set_lock_pattern_auth);
        } else {
            mode = MODE_FIRST_STEP;
            textView.setText(R.string.set_lock_pattern_first_step);
            showFirstUserDialog();
        }
    }

    /** 처음 시작시 암호(패턴)생성 다이얼로그 */
    private void showFirstUserDialog() {
        Builder builder = new Builder(this);
        builder.setMessage(R.string.set_lock_pattern_first_message);
        builder.setNeutralButton(R.string.set_lock_pattern_first_sure, null);
        builder.show();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPatternStart() {
        textView.setText(R.string.set_lock_pattern_step_tips);
    }

    @Override
    public void onPatternCleared() {
    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {
    }

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        switch (mode) {
            case MODE_AUTH:
                if (LockPatternUtil.authPatternCell(this, pattern)) {
                    // 인증 승인됨.
                    lockPatternView.setDisplayMode(DisplayMode.Correct);
                    lockPatternView.setEnabled(false);
                    textView.setText(R.string.set_lock_pattern_auth_ok);
                    handler.sendEmptyMessageDelayed(4, 1000);
                } else {
                    // 인증거부됨, 다시시도.
                    lockPatternView.setEnabled(false);
                    lockPatternView.setDisplayMode(DisplayMode.Wrong);
                    textView.setText(R.string.set_lock_pattern_auth_error);
                    handler.sendEmptyMessageDelayed(MEG_AUTH_ERROR, 1000);
                }
                break;
            case MODE_FIRST_STEP:
                // 처음 생성
                lockPatternView.setEnabled(false);
                lastCells = new ArrayList<LockPatternView.Cell>(pattern);
                textView.setText(R.string.set_lock_pattern_first_step_tips);
                handler.sendEmptyMessageDelayed(MEG_GOTO_SECOND_STEP, 1000);
                break;
            case MODE_SECOND_STEP:
                if (LockPatternUtil.checkPatternCell(lastCells, pattern)) {
                    // 설정 완료
                    lockPatternView.setEnabled(false);
                    lockPatternView.setDisplayMode(DisplayMode.Correct);
                    textView.setText(R.string.set_lock_pattern_second_step_tips);
                    handler.sendEmptyMessageDelayed(MEG_SET_SUCCESS, 2000);
                    LockPatternUtil.savePatternCell(this, pattern);
                } else {
                    // 패턴 불일치, 처음부터 다시 설정
                    lockPatternView.setDisplayMode(DisplayMode.Wrong);
                    lockPatternView.setEnabled(false);
                    textView.setText(R.string.set_lock_pattern_second_step_error);
                    handler.sendEmptyMessageDelayed(MEG_GOTO_FIRST_STEP, 1000);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        lockPatternView.setEnabled(true);
        lockPatternView.clearPattern();
        switch (msg.what) {
            case MEG_AUTH_ERROR:
                textView.setText(R.string.set_lock_pattern_auth);
                break;
            case MEG_GOTO_SECOND_STEP:
                mode = MODE_SECOND_STEP;
                textView.setText(R.string.set_lock_pattern_second_step);
                break;
            case MEG_SET_SUCCESS:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case MEG_GOTO_FIRST_STEP:
                mode = MODE_FIRST_STEP;
                textView.setText(R.string.set_lock_pattern_first_step);
                break;
        }
        return true;
    }
}
