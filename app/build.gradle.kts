plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
}

android {
    val fileProviderName = ".FileProvider"
    val packageName = "app.atomofiron.searchboxapp"
    namespace = packageName

    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = packageName
        minSdk = 24
        targetSdk = 35
        versionCode = 13
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "version_name", "v$versionName ($versionCode)")
        buildConfigField("String", "YANDEX_API_KEY", "null")
        buildConfigField("boolean", "DEBUG_BUILD", "true")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "DEBUG_BUILD", "true")
            buildConfigField("String", "AUTHORITY", "\"${packageName + applicationIdSuffix + fileProviderName}\"")
            buildConfigField("debug.LeakWatcher", "leakWatcher", "new debug.LeakWatcherImpl()")
            manifestPlaceholders["PACKAGE_NAME"] = packageName + applicationIdSuffix
            manifestPlaceholders["PROVIDER"] = packageName + applicationIdSuffix + fileProviderName
        }
        create("alpha") {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ""
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "AUTHORITY", "\"${packageName + applicationIdSuffix + fileProviderName}\"")
            buildConfigField("debug.LeakWatcher", "leakWatcher", "null")
            manifestPlaceholders["PACKAGE_NAME"] = packageName + applicationIdSuffix
            manifestPlaceholders["PROVIDER"] = packageName + applicationIdSuffix + fileProviderName
        }
        create("beta") {
            isDebuggable = false
            isMinifyEnabled = true
            applicationIdSuffix = ""
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "AUTHORITY", "\"${packageName + applicationIdSuffix + fileProviderName}\"")
            buildConfigField("debug.LeakWatcher", "leakWatcher", "null")
            manifestPlaceholders["PACKAGE_NAME"] = packageName + applicationIdSuffix
            manifestPlaceholders["PROVIDER"] = packageName + applicationIdSuffix + fileProviderName
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = true
            buildConfigField("String", "AUTHORITY", "\"${packageName + fileProviderName}\"")
            buildConfigField("debug.LeakWatcher", "leakWatcher", "null")
            manifestPlaceholders["PACKAGE_NAME"] = packageName
            manifestPlaceholders["PROVIDER"] = packageName + fileProviderName
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.core)
    implementation(libs.kotlinx.core.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.lifecycle.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.work)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.flexbox)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.exif)
    implementation(libs.androidx.async.inflater)
    implementation(libs.androidx.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.processor)
    implementation(libs.material)
    implementation(libs.play.core)
    implementation(libs.play.core.ktx)
    implementation(libs.insets)
    debugImplementation(libs.leakcanary)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}