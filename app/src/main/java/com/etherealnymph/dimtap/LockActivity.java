package com.etherealnymph.dimtap;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 没有界面的入口。
 *
 * <p>点击桌面图标 → 如果已经激活设备管理员，立即锁屏并退出；
 * 否则打开系统自带的激活页，等用户激活回来后直接锁屏。
 *
 * <p>这里不使用 Theme.NoDisplay：部分机型上无窗口的 Activity 会在系统页面
 * 打开前就被结束，看起来像闪退。透明窗口则始终存在。
 */
public class LockActivity extends Activity {

    private static final int REQUEST_ADMIN = 1;

    private ComponentName admin;
    private DevicePolicyManager dpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        admin = new ComponentName(this, AdminReceiver.class);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        if (showLastCrash()) {
            return;
        }
        run();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        run();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_ADMIN) {
            return;
        }
        if (isAdminActive()) {
            lockNow();
            return;
        }
        // 少数系统在返回的瞬间还没提交激活状态，稍等再确认一次。
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdminActive()) {
                lockNow();
            } else {
                finish();
            }
        }, 250L);
    }

    private void run() {
        try {
            if (isAdminActive()) {
                lockNow();
            } else {
                requestAdmin();
            }
        } catch (Throwable t) {
            showError(t);
        }
    }

    private boolean isAdminActive() {
        return dpm != null && dpm.isAdminActive(admin);
    }

    @SuppressWarnings("deprecation")
    private void lockNow() {
        try {
            if (dpm != null) {
                dpm.lockNow();
            }
        } catch (Throwable t) {
            showError(t);
            return;
        }
        finishAndRemoveTask();
        overridePendingTransition(0, 0);
    }

    private void requestAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.admin_explanation));
        startActivityForResult(intent, REQUEST_ADMIN);
    }

    /** 上一次运行崩溃过：把堆栈直接显示出来，方便截图反馈。 */
    private boolean showLastCrash() {
        SharedPreferences sp = getSharedPreferences(DimApp.PREFS, Context.MODE_PRIVATE);
        String trace = sp.getString(DimApp.KEY_CRASH, null);
        if (trace == null || trace.isEmpty()) {
            return false;
        }
        sp.edit().remove(DimApp.KEY_CRASH).apply();
        showText(getString(R.string.crash_title) + "\n\n" + trace);
        return true;
    }

    private void showError(Throwable t) {
        String reason = t.getClass().getSimpleName();
        if (t.getMessage() != null) {
            reason += "：" + t.getMessage();
        }
        showText(getString(R.string.error_title) + "\n\n" + reason);
    }

    private void showText(String body) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        TextView view = new TextView(this);
        view.setTextColor(Color.WHITE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        view.setLineSpacing(dp(5), 1f);
        view.setPadding(dp(20), dp(60), dp(20), dp(24));
        view.setText(body + "\n\n" + getString(R.string.error_hint));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(10, 12, 17));
        scroll.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
        Toast.makeText(this, R.string.error_title, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
