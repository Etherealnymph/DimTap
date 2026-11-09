# 一键熄屏 · DimTap

<p align="center">
  <strong>The smallest possible screen-off shortcut for Android.</strong><br>
  <em>Tap the launcher icon, authenticate once, and lock the screen.</em>
</p>

> **中文**：DimTap 是 DimScreen 的极简 companion app：没有主界面、没有网络、没有权限，只保留桌面图标一键熄屏。  
> **English**: DimTap is the minimal companion to DimScreen: no main UI, no network, no declared permissions — only a launcher shortcut that locks the screen.

## 亮点 Highlights

- **一步完成 / One-step action**：首次激活设备管理员后，之后每次点击图标立即熄屏。
- **极小体积 / Tiny footprint**：面向 Android 10–14，专注、快速、可审计。
- **安全边界清晰 / Narrow security boundary**：只请求 `force-lock`，不涉及擦除数据、密码或摄像头。

## English

DimTap is a deliberately constrained Android utility for people who want a launcher icon that does exactly one thing. The transparent activity handles the first-time device-admin flow, waits for the result, calls `lockNow()`, and exits. Crash details are surfaced on screen instead of being silently swallowed.

> 点击桌面图标 → 屏幕立刻熄灭。没有界面，没有其他功能。

版本 1.0.1：修复 1.0.0 在部分机型上“点开一闪而过、设备管理员页没打开”的问题。

原来的完整版 [DimScreen](../DimScreen) 保留不变，两个包名不同，可以同时安装。

## 安装

```
DimTap/dist/DimTap-1.0.1.apk
```

```
adb install -r DimTap/dist/DimTap-1.0.1.apk
```

- 包名：`com.etherealnymph.dimtap`
- 版本：`1.0.1`（versionCode 2）
- minSdk 29（Android 10）/ targetSdk 34（Android 14）
- 大小：21,353 字节（约 21 KB）
- 权限：**无**（没有任何 `uses-permission`）
- SHA-256：`08a07acb19fc58819e0b51e2d50321d0ce9a26979a11cb3792138c336798fdcc`

如果手机里装过 1.0.0，直接覆盖安装即可。

## 第一次使用

1. 点击桌面上的「一键熄屏」图标。
2. 系统弹出设备管理员说明，点「激活」。
3. 激活成功后**屏幕立即熄灭**，不需要再点第二次。
4. 之后每次点击图标都是一步熄屏。

## 1.0.1 改了什么

1.0.0 使用的是 `Theme.NoDisplay`，并在 `onCreate()` 里立刻 `finish()`。部分系统会在系统激活页真正打开之前就把这个无窗口的 Activity 结束掉，用户看到的就是“闪退、没息屏”。

1.0.1 的改动：

- 换成 `Theme.Translucent.NoTitleBar` 的透明窗口（有真实窗口，不再是无窗口 Activity）。
- 用 `startActivityForResult` 打开系统激活页，等结果返回；激活成功后直接调 `lockNow()` 熄屏。
- 返回瞬间如果还查不到管理员状态，会延迟 250ms 再确认一次。
- 任何异常不再让应用闪退，而是显示在屏幕上（可截图反馈）。
- `DimApp` 会记录未捕获的崩溃堆栈，下次点图标时直接显示出来。

## 实现

```
LockActivity     透明 Activity：已激活 → lockNow() → 退出；
                 未激活 → 系统激活页 → 返回后熄屏
AdminReceiver    只声明 force-lock 一项策略
DimApp           崩溃堆栈记录
```

设备管理员只使用 `force-lock`（`res/xml/device_admin.xml`），不涉及数据擦除、密码、摄像头等能力。

## 撤销 / 卸载

设备管理员激活时系统会阻止直接卸载。先撤销：

`设置 → 安全 → 设备管理应用 → 一键熄屏 → 取消激活`

然后再正常卸载。

## 构建

```powershell
powershell -ExecutionPolicy Bypass -File .\build-apk.ps1
```

需要 JDK 17 与 Android SDK build-tools 34.0.0 / platform android-34。也可以直接用 Android Studio 打开 `DimTap/`。

公开仓库不包含签名证书。侧载构建时请使用自己的 keystore，并通过 `APK_KEYSTORE_PASSWORD` 环境变量提供口令。

## 验证

- `aapt2` / `javac` / `d8` / `zipalign` / `apksigner` 全部通过，v3 签名校验通过。
- `aapt2 dump badging`：0 个权限，`targetSdk 34`。
- dex 中包含 `LockActivity`、`AdminReceiver`、`DimApp` 与 R 类。
- 未在真机测试（当前环境没有 adb 设备 / 模拟器）。

如果 1.0.1 仍然有问题，点开图标后会把上次的崩溃堆栈显示在屏幕上，截图发我即可定位。
