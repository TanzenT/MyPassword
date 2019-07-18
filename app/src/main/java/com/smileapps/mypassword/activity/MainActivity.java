package com.smileapps.mypassword.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;

//import com.orhanobut.rootchecker.RootChecker;
import com.smileapps.mypassword.R;
import com.smileapps.mypassword.activity.PasswordGroupFragment.OnPasswordGroupSelected;
import com.smileapps.mypassword.app.BaseActivity;
import com.smileapps.mypassword.dialog.ExportDialog;
import com.smileapps.mypassword.dialog.ImportDialog;
import com.smileapps.mypassword.model.SettingKey;
import com.smileapps.mypassword.service.MainService;
import com.smileapps.mypassword.service.Mainbinder;

import cn.zdx.lib.annotation.FindViewById;

/**
 * 매인 액티비티
 *
 * @author zeus8502
 */
public class MainActivity extends BaseActivity {
    /** 데이터 소스 */
    private Mainbinder mainbinder;
    private long lastBackKeyTime;

    @FindViewById(R.id.drawer_layout)
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private PasswordListFragment passwordListFragment;
    private PasswordGroupFragment passwordGroupFragment;

    @FindViewById(R.id.navigation_drawer)
    private View drawerView;

    private OnPasswordGroupSelected onPasswordGroupSelected = new OnPasswordGroupSelected() {
        @Override
        public void onPasswordGroupSelected(String passwordGroupName) {
            drawerLayout.closeDrawer(drawerView);
            if (passwordListFragment != null)
                passwordListFragment.showPasswordGroup(passwordGroupName);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainbinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainbinder = (Mainbinder) service;
            initFragment();
        }
    };

    private void initFragment() {
        FragmentManager fragmentManager = getFragmentManager();

        passwordListFragment = (PasswordListFragment) fragmentManager.findFragmentByTag("PasswordListFragment");
        if (passwordListFragment == null)
            passwordListFragment = new PasswordListFragment();
        passwordListFragment.setDataSource(mainbinder);

        passwordGroupFragment = (PasswordGroupFragment) fragmentManager.findFragmentByTag("PasswordGroupFragment");
        if (passwordGroupFragment == null)
            passwordGroupFragment = new PasswordGroupFragment();
        passwordGroupFragment.setDataSource(mainbinder, onPasswordGroupSelected);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.navigation_drawer, passwordGroupFragment, "PasswordGroupFragment");
        fragmentTransaction.replace(R.id.container, passwordListFragment, "PasswordListFragment");
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("MyPassword를 실행하려면 다음 권한이 필요합니다.\n전화 권한과 저장공간 권한\n전화 권한 : 앱의 변조여부를 검사합니다.\n저장공간 권한 : MyPassword를 SD카드로 내보내고 불러올때 필요합니다." +
                        "\n하지만 권한이 거부되었습니다. [설정] > [권한] 에서 권한을 허용해 주세요."
                )
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .check();
        initDrawer();
		drawerLayout.openDrawer(drawerView);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		
       /* try{
            Runtime.getRuntime().exec("su");
            //Toast.makeText(this, "루팅된 디바이스 입니다.", Toast.LENGTH_SHORT).show();
            // 다이얼로그 바디
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
            // 다이얼로그 메세지
            alertdialog.setMessage("루팅되거나 커스텀롬을 사용하는 디바이스에서는 MyPassword를 사용할 수 없습니다.");

            // 확인버튼
            alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Toast.makeText(activity, "'확인'버튼을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            // 메인 다이얼로그 생성
            AlertDialog alert = alertdialog.create();
            // 아이콘 설정
            alert.setIcon(R.drawable.ic_launcher);
            // 타이틀
            alert.setTitle("경고");
            // 다이얼로그 보기
            alert.setCancelable(false);

            alert.show();
        } catch(Exception e){
            }
			*/
    }
    /*
    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Intent intent_resume;
        intent_resume = new Intent(this, EntryActivity.class);
        startActivity(intent_resume);
        finish();
    }
    */

    private void initDrawer() {
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.drawable.ic_drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
                getActionBar().setTitle(R.string.app_name);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
                if (passwordListFragment != null && !passwordListFragment.getPasswordGroupName().equals(""))
                    getActionBar().setTitle(passwordListFragment.getPasswordGroupName());
                else {
                    getActionBar().setTitle(R.string.app_name);
                }
            }
        };

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        drawerLayout.setDrawerListener(mDrawerToggle);

        if (getSetting(SettingKey.IS_SHOWED_DRAWER, "false").equals("false")) {
            putSetting(SettingKey.IS_SHOWED_DRAWER, "true");
            drawerLayout.openDrawer(drawerView);
        } else {
            String lastGroupName = getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, "");
            if (lastGroupName.equals(""))
                lastGroupName = getString(R.string.app_name);
            getActionBar().setTitle(lastGroupName);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private boolean isExistSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_password:
                if (mainbinder == null)
                    break;
                Intent intent = new Intent(this, EditPasswordActivity.class);
                if (passwordListFragment != null)
                    intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordListFragment.getPasswordGroupName());
                startActivity(intent);
                break;

            case R.id.action_import:
                // 불러오기
                if (mainbinder == null)
                    break;
                ImportDialog importDialog = new ImportDialog(getActivity(), mainbinder);
                importDialog.show();
                break;

            case R.id.action_export:
                // 내보내기
                if (mainbinder == null)
                    break;
                if (!isExistSDCard()) {
                    showToast(R.string.export_no_sdcard);
                    break;
                }
                ExportDialog exportDialog = new ExportDialog(this, mainbinder);
                exportDialog.show();
                break;

            case R.id.action_set_lock_pattern:
                // 패턴
                startActivity(new Intent(this, SetLockpatternActivity.class));
                break;
            case R.id.action_set_effect:
                // 에니메이션 전환 효과 설정
                onEffectClick();
                break;
            case R.id.action_about:
                // 정보
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_exit:
                // 나가기
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!drawerLayout.isDrawerOpen(drawerView)) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //백(취소)키가 눌렸을때 종료여부를 묻는 다이얼로그 띄움
        if((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
            d.setTitle("종료확인");
            d.setMessage("정말 종료 하시겠습니까?");
            d.setIcon(R.drawable.ic_launcher);
            d.setPositiveButton("네",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    MainActivity.this.finish();
                    Intent intent_stop_service = new Intent(MainActivity.this, MainService.class);
                    stopService(intent_stop_service);
                }
            });

            d.setNegativeButton("아니요",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.cancel();
                }
            });
            d.show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void onEffectClick() {
        if (getSetting(SettingKey.JAZZY_EFFECT_INTRODUCTION, "false").equals("false")) {
            putSetting(SettingKey.JAZZY_EFFECT_INTRODUCTION, "true");
            Builder builder = new Builder(this);
            builder.setMessage(R.string.action_jazzy_effect_introduction);
            builder.setNeutralButton(R.string.i_known, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onEffectClick();
                }
            });
            builder.show();
        } else {
            Builder builder = new Builder(this);
            builder.setTitle(R.string.action_jazzy_effect);

            final String[] effectArray = getResources().getStringArray(R.array.jazzy_effects);
            builder.setItems(effectArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().putSetting(SettingKey.JAZZY_EFFECT, which + "");
                    onEventEffect(effectArray[which]);
                }
            });
            builder.show();
        }
    }

    /**
     * 에니메이션 효과
     *
     * @param effect
     */
    private void onEventEffect(String effect) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("effect", effect);
    }
}
