# Compose 库自带 consumer keep 规则；手动 keep 全部成员会显著增大 APK，已移除
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn androidx.compose.**
# commons-compress 可选编解码器未打包，静音 R8 缺失类告警
-dontwarn org.apache.commons.compress.**
-dontwarn org.apache.commons.net.**
-dontwarn org.slf4j.**
-dontwarn com.github.junrar.**
-dontwarn org.brotli.**
-dontwarn com.aayushatharva.brotli4j.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.lz4.**
-dontwarn org.xerial.snappy.**
