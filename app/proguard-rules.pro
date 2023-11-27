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

# Desktop Java classes, we are on Android -> hide this false positive
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.lang.model.**
-dontwarn javax.swing.**
