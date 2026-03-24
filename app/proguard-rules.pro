# Improve app ProGuard / R8 rules

# Keep source line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# ── Firebase Realtime Database ─────────────────────────────────────────────────
# Keep all DTO/model classes used for Firebase deserialization
-keep class dev.danielholmberg.improve.data.firebase.dto.** { *; }
-keep class dev.danielholmberg.improve.domain.model.** { *; }

# Firebase internals
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── Google Play Services / Auth ────────────────────────────────────────────────
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── Google API Client (Drive) ──────────────────────────────────────────────────
-keep class com.google.api.** { *; }
-keep class com.google.apis.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.apis.**
-dontwarn org.apache.httpcomponents.**

# ── Gson (used by Google API client) ──────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ── Jetpack Compose ────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Coil ──────────────────────────────────────────────────────────────────────
-dontwarn coil.**

# ── Crashlytics ───────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class com.google.firebase.crashlytics.** { *; }

# ── General AndroidX ──────────────────────────────────────────────────────────
-dontwarn androidx.**
