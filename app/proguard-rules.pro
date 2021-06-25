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

-keep class !android.support.v7.internal.view.menu.*,* {*;}

# javax.swing is for desktop, we are on Android -> hide this false positive
-dontwarn javax.swing.**