# ProGuard / R8 rules for DownNotice
-keepattributes *Annotation*

# kotlinx.serialization
-keepattributes RuntimeVisibleAnnotations
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class com.downnotice.mobile.data.model.** {
    *;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
