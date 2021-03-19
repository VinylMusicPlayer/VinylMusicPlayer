# RetroFit
-keep class retrofit2.*
-keepattributes Signature
-keepattributes Exceptions

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Okio

# ButterKnife
-keep class butterknife.*
-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class !android.support.v7.internal.view.menu.**,** {*;}
