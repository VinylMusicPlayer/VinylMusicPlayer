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

# Legend (https://stackoverflow.com/questions/24809580/noclassdeffounderror-android-support-v7-internal-view-menu-menubuilder)
# says that this is not needed anymore
#-keep class !android.support.v7.internal.view.menu.**,** {*;}
