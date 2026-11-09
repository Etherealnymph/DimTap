package com.etherealnymph.dimtap;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/** 只声明 force-lock 一项策略的设备管理员。 */
public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.disable_warning);
    }
}
