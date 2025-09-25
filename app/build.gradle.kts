import com.google.protobuf.gradle.id
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import org.gradle.api.tasks.Exec

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kapt)
    id("app.fileseeker.convention.library")
}

val kotlinDir = "src/main/kotlin"
val jniLibsDir = "src/main/jniLibs"
val nativeLibName = "native_lib"

android {
    namespace = "app.atomofiron.fileseeker"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    sourceSets {
        named("main") {
            /*proto {
                srcDir("../proto")
            }*/
            java {
                srcDirs("$kotlinDir/uniffi/$nativeLibName")
            }
            jniLibs {
                srcDirs(jniLibsDir)
            }
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.1"
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("java")
                id("kotlin")
            }
        }
    }
}

dependencies {
    implementation(libs.kotlinx.core)
    implementation(libs.kotlinx.core.android)
    implementation(libs.kotlinx.protobuf)
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.kotlin)
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
    implementation(libs.jna) { artifact { type = "aar" } }
}

val taskPreBuild = "preBuild"
val taskBuildNative = "buildNative"
val taskGenerateUniffiBindings = "generateUniffiBindings"
val taskCopyNativeBins = "copyNativeBins"
val nativeLib = "native-lib"
val nativeBin = "native-bin"

val ndkApi = android.defaultConfig.minSdk
val nativeDirPath = "$projectDir/../native"
val cargoPath = "${System.getProperty("user.home")}/.cargo/bin/cargo"
val targets = arrayOf(
    "aarch64-linux-android",
    "armv7-linux-androideabi",
    "x86_64-linux-android",
    "i686-linux-android",
)

tasks.register<Exec>(taskBuildNative) {
    group = "rust"
    workingDir(nativeDirPath)
    commandLine(
        cargoPath, "ndk",
        "-t", "arm64-v8a",
        "-t", "armeabi-v7a",
        "-t", "x86",
        "-t", "x86_64",
        "-P", "$ndkApi",
        "-o", "$projectDir/$jniLibsDir",
        "build", "--release",
        "-p", nativeLib,
        "-p", nativeBin,
    ).apply {
        println("for manual use: cd $nativeDirPath && ${commandLine.joinToString(separator = " ")}\n")
    }
    isIgnoreExitValue = false
}

tasks.register<Exec>(taskGenerateUniffiBindings) {
    group = "rust"
    workingDir(nativeDirPath)
    commandLine(
        cargoPath, "run",
        "--bin", "uniffi-bindgen", "generate",
        "--library", "target/aarch64-linux-android/release/lib$nativeLibName.so",
        "--language", "kotlin", "--no-format",
        "--out-dir", "../app/$kotlinDir",
    ).apply {
        println("for manual use: cd $nativeDirPath && ${commandLine.joinToString(separator = " ")}\n")
    }
    environment("DEVELOPER_DIR", "/Library/Developer/CommandLineTools")
    isIgnoreExitValue = false
}

tasks.register<Copy>(taskCopyNativeBins) {
    group = "rust"
    targets.associate {
        it.split('-', limit = 2).first() to "$nativeDirPath/target/$it/release/$nativeBin"
    }.forEach { (abi, sourcePath) ->
        from(sourcePath) {
            rename { _ -> abi }
        }
    }
    into("src/main/assets/$nativeBin")
}

tasks.named(taskPreBuild) {
    dependsOn(taskBuildNative, taskGenerateUniffiBindings, taskCopyNativeBins)
}

fun prepare(vararg args: String) = ProcessBuilder(*args)
    .redirectErrorStream(true)
    .start()
    .takeIf { it.waitFor() != 0 }
    ?.run { throw IllegalStateException(inputStream.bufferedReader().readText()) }

prepare("rustup", "target", "add", *targets)
prepare("cargo", "install", "cargo-ndk")
