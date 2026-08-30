# Keep model names used in local JSON diagnostics readable.
-keepattributes SourceFile,LineNumberTable
# Tink references these source-retention annotations; they are not required at runtime.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
