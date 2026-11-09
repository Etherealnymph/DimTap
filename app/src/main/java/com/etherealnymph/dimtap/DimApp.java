package com.etherealnymph.dimtap;

import android.app.Application;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 把未捕获异常的堆栈写到本地，下一次启动时显示出来。
 * 这个应用没有界面，崩溃时如果只“闪一下”，用户和开发者都拿不到原因。
 */
public class DimApp extends Application {

    static final String PREFS = "crash";
    static final String KEY_CRASH = "last";

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler previous =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter writer = new StringWriter();
                throwable.printStackTrace(new PrintWriter(writer));
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_CRASH, writer.toString())
                        .commit();
            } catch (Throwable ignored) {
                // 记录失败也不能影响系统原有的崩溃处理。
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            }
        });
    }
}
