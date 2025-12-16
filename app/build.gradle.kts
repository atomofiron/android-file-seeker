import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

val ndkApi = android.defaultConfig.minSdk
val cargoPath = "${System.getProperty("user.home")}/.cargo/bin/cargo"
val kotlinDir = "src/main/kotlin"
val jniLibsDir = "src/main/jniLibs"
val jniLibsPath = "$projectDir/$jniLibsDir"
val nativePath = "$projectDir/../native"
val nativeLibName = "native_lib"
val nativeLibSo = "lib$nativeLibName.so"
val nativeLib = "native-lib"
val nativeBin = "native-bin"
val targets = arrayOf(
    "aarch64-linux-android",
    "armv7-linux-androideabi",
    "x86_64-linux-android",
    "i686-linux-android",
)
val binSrcPaths = targets.map { "$nativePath/target/$it/release/$nativeBin" }
val binDstDir = "src/main/assets/$nativeBin"
val debugSoFile = "target/${targets.first()}/debug/$nativeLibSo"
val debugBinFile = "target/${targets.first()}/debug/$nativeBin"
val debugLibPath = "$nativePath/$debugSoFile"
val debugBinPath = "$nativePath/$debugBinFile"
val ktBindingFile = "$kotlinDir/uniffi/$nativeLibName/$nativeLibName.kt"

android {
    namespace = "app.atomofiron.fileseeker"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NATIVE_BIN", "\"$nativeBin\"")
        buildConfigField("String", "NATIVE_LIB", "\"$nativeLibName\"")
        buildConfigField("String", "NATIVE_LIB_SO", "\"$nativeLibSo\"")
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
    api(libs.androidx.appcompat)
    api(libs.androidx.datastore)
    implementation(libs.kotlinx.core)
    implementation(libs.kotlinx.core.android)
    implementation(libs.kotlinx.protobuf)
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.kotlin)
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
    implementation(libs.androidx.documentfile)
    implementation(libs.dagger)
    implementation(libs.material)
    implementation(libs.play.core)
    implementation(libs.play.core.ktx)
    implementation(libs.ktor.negotiation)
    implementation(libs.ktor.core)
    implementation(libs.ktor.json)
    implementation(libs.ktor.cio)
    implementation(libs.insets)
    implementation(libs.glide)
    implementation(libs.jna) { artifact { type = "aar" } }
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.processor)
    ksp(libs.androidx.room.compiler)
    ksp(libs.glide.ksp)
    debugImplementation(libs.leakcanary)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
}

val taskPreBuild = "preBuild"
val taskBuildNativeDebug = "buildNativeDebug"
val taskGenerateNativeBindings = "generateNativeBindings"
val taskBuildNative = "buildNative"
val taskCopyNativeBins = "copyNativeBins"

afterEvaluate {
    taskGenerateNativeBindings {
        dependsOn(taskBuildNativeDebug)
    }
    taskBuildNative {
        mustRunAfter(taskGenerateNativeBindings)
        dependsOn(taskBuildNativeDebug) // don't run if failed
    }
    taskCopyNativeBins {
        dependsOn(taskBuildNative)
    }
    taskPreBuild {
        dependsOn(taskGenerateNativeBindings)
        dependsOn(taskCopyNativeBins)
    }
}

val groupUniffi = "uniffi"

tasks.register<Exec>(taskBuildNativeDebug) {
    group = groupUniffi
    // always run

    cargoTermColor()
    workingDir(nativePath)
    commandLine(
        cargoPath, "ndk",
        "-t", "arm64-v8a",
        "-P", "$ndkApi",
        "build",
        "-p", nativeLib,
        "-p", nativeBin, // is needed for inputs in next tasks
    )
    print()
    isIgnoreExitValue = false
    errorOutput = System.out
    standardOutput = System.out
}

tasks.register<Exec>(taskGenerateNativeBindings) {
    group = groupUniffi
    inputs.file(debugLibPath)
    outputs.file(ktBindingFile)
    outputs.upToDateWhen { true } // ignore outputs

    cargoTermColor()
    environment("DEVELOPER_DIR", "/Library/Developer/CommandLineTools") // for MacOS only
    workingDir(nativePath)
    commandLine(
        cargoPath, "run",
        "--bin", "uniffi-gen", "generate",
        "--library", debugSoFile,
        "--language", "kotlin", "--no-format",
        "--out-dir", "../app/$kotlinDir",
    )
    print()
    isIgnoreExitValue = false
    errorOutput = System.out
    standardOutput = System.out
    doLast {
        val emptyCompanion = "    companion object\n"
        val cleaner = "interface UniffiCleaner {"
        val file = File(projectDir, ktBindingFile)
        file.readText()
            .replace("    object", "    data object")
            .replace(emptyCompanion, "")
            .replace(cleaner, "$cleaner\n$emptyCompanion") // put it back
            .let { file.writeText(it) }
    }
}

tasks.register<Exec>(taskBuildNative) {
    group = groupUniffi
    inputs.file(debugLibPath)
    inputs.file(debugBinPath)
    outputs.dir(jniLibsPath)
    // don't check the bins

    cargoTermColor()
    workingDir(nativePath)
    commandLine(
        cargoPath, "ndk",
        "-t", "arm64-v8a",
        "-t", "armeabi-v7a",
        "-t", "x86",
        "-t", "x86_64",
        "-P", "$ndkApi",
        "-o", jniLibsPath,
        "build", "--release",
        "-p", nativeLib,
        "-p", nativeBin,
    )
    print()
    isIgnoreExitValue = false
    errorOutput = System.out
    standardOutput = System.out
}

tasks.register<Copy>(taskCopyNativeBins) {
    group = groupUniffi
    binSrcPaths.forEach { inputs.file(it) }
    outputs.dir(binDstDir)

    binSrcPaths.associateBy {
        it.split('-', limit = 2).first()
    }.forEach { (abi, sourcePath) ->
        from(sourcePath) {
            rename { _ -> abi }
        }
    }
    into(binDstDir)
}

operator fun String.invoke(action: Task.() -> Unit) = tasks.named(this).configure(action)

fun Exec.cargoTermColor() = environment("CARGO_TERM_COLOR", "always")

fun Exec.print() = println("for manual use: cd $workingDir && ${commandLine.joinToString(separator = " ")}")

fun prepare(vararg args: String) = ProcessBuilder(*args)
    .redirectErrorStream(true)
    .start()
    .takeIf { it.waitFor() != 0 }
    ?.run { throw IllegalStateException(inputStream.bufferedReader().readText()) }

prepare("rustup", "target", "add", *targets)
prepare("cargo", "install", "cargo-ndk")
