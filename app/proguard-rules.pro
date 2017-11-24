
-optimizationpasses 5                               # 指定代码的压缩级别
-renamesourcefileattribute SourceFile               # 将.class信息中的类名重新定义为"SourceFile"字符串
-keepattributes SourceFile,LineNumberTable          # 保留源文件名为"SourceFile"字符串，而非原始的类名 并保留行号

# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}