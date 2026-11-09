plugins {
    id("com.android.application")
}

android {
    namespace = "com.etherealnymph.dimtap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.etherealnymph.dimtap"
        minSdk = 29
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// 没有第三方依赖：整个应用只有两个类和一个无界面 Activity。
