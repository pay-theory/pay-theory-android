# Keep source file names and line numbers for better debugging
-keepattributes SourceFile,LineNumberTable

# Repackage classes into the top-level
-repackageclasses

# Keep Compose-related classes
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }

# Keep SSL-related classes
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE