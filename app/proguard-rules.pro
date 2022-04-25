#压缩级别0-7，Android一般为5(对代码迭代优化的次数)
-optimizationpasses 5
-dontoptimize

-keep class androidx.fake.stub.e {
    public GetContext();
    public fs();
    native <methods>;
}

-keepattributes Signature
-keep class **.R$* {*;}

-keep class android.app.Activity.**{*;}
-keep class androidx.appcompat.app.AppCompatActivity{*;}
-keep class com.mrctf.android2022.**{*;}