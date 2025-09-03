plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.kapt)
    id("app.fileseeker.convention.library")
}

android {
    namespace = "app.atomofiron.fileseeker"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.kotlinx.core)
    implementation(libs.kotlinx.core.android)
    implementation(libs.kotlinx.protobuf)
    api(libs.androidx.appcompat)
    api(libs.androidx.datastore)
    implementation(libs.androidx.core.ktx)
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
    implementation(libs.ktor.negotiation)
    implementation(libs.ktor.core)
    implementation(libs.ktor.json)
    implementation(libs.ktor.cio)
    implementation(libs.insets)
    implementation(libs.glide)
    debugImplementation(libs.leakcanary)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
}