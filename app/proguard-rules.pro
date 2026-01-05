# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/atomofiron/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.android.vending.billing.**

# JNA
-dontwarn com.sun.jna.**
-keep class com.sun.jna.** { *; }
-keep class java.nio.** { *; }
-keep class java.util.concurrent.** { *; }
-keep class * implements com.sun.jna.Library { *; }
-keepclassmembers class * implements com.sun.jna.Library { *; }
-keep class * extends com.sun.jna.Structure { *; }
-keep class * extends com.sun.jna.Union { *; }
-keep class * implements com.sun.jna.Callback { *; }

# UniFFI
-keep class uniffi.** { *; }
