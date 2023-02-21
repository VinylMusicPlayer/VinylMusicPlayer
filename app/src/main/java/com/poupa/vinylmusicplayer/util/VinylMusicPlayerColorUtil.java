package com.poupa.vinylmusicplayer.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.poupa.vinylmusicplayer.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function3;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class VinylMusicPlayerColorUtil {

    @Nullable
    public static Palette generatePalette(Bitmap bitmap) {
        if (bitmap == null) return null;
        return Palette.from(bitmap).generate();
    }

    @ColorInt
    public static int getColor(@Nullable Palette palette, int fallback) {
        if (palette != null) {
            if (palette.getVibrantSwatch() != null) {
                return palette.getVibrantSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                return palette.getMutedSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                return palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                return palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                return palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                return palette.getLightMutedSwatch().getRgb();
            } else if (!palette.getSwatches().isEmpty()) {
                return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
            }
        }
        return fallback;
    }

    private static class SwatchComparator implements Comparator<Palette.Swatch> {
        private static SwatchComparator sInstance;

        static SwatchComparator getInstance() {
            if (sInstance == null) {
                sInstance = new SwatchComparator();
            }
            return sInstance;
        }

        @Override
        public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
            return lhs.getPopulation() - rhs.getPopulation();
        }
    }

    public static boolean isSystemThemeSupported() {
        // Inspired from https://stackoverflow.com/questions/55787035/is-there-an-api-to-detect-which-theme-the-os-is-using-dark-or-light-or-other
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            // OS theme not supported
            return false;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Not clear, might depend on wallpaper hinting
            return true;
        } else {
            // Supported, use Configuration.uiMode
            return true;
        }
    }

    public static int getSystemNightMode(@NonNull final Context context) {
        if (VinylMusicPlayerColorUtil.isSystemThemeSupported()) {
            return context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        }
        return Configuration.UI_MODE_NIGHT_UNDEFINED;
    }

    public static int adjustLightness(int color, float lightnessFactor) {
        float[] outHsl = new float[3];
        ColorUtils.colorToHSL(color, outHsl);

        outHsl[2] = outHsl[2] * lightnessFactor;
        return ColorUtils.HSLToColor(outHsl);
    }

    public static int getContrastedColor(int foreground, int background) {
        double contrast = ColorUtils.calculateContrast(foreground, background);

        if (contrast < 4.5) {
            float maxDark = 0.5f;  //empirical value
            float maxLight = 2.2f; //same

            // create a linear curve to try to better the contrast
            int darkenColor = adjustLightness(foreground, 1.0f + (maxDark - 1.0f) * (4.5f - (float) contrast) / (4.5f - 1.0f));
            int lighterColor = adjustLightness(foreground, 1.0f + (maxLight - 1.0f) * (4.5f - (float) contrast) / (4.5f - 1.0f));

            double darkerContrast = ColorUtils.calculateContrast(darkenColor, background);
            double lighterContrast = ColorUtils.calculateContrast(lighterColor, background);

            return (darkerContrast > lighterContrast) ? darkenColor : lighterColor;
        } else {
            return foreground;
        }
    }

    @ColorInt
    private static int deriveAccentColorFromPrimaryColor_ImplByContrast(@NonNull final Context context, @ColorInt final int primaryColor) {
        final Function3<Integer, Integer, Integer, Boolean> isContrastedEnough =
                (@ColorInt Integer color, @ColorInt Integer foreground, @ColorInt Integer background) -> {
                    final float minContrastRatioVsForeground = 2.3f;
                    final float minContrastRatioVsBackground = 2f;
                    final float minContrastRatioVsPrimary = 1.7f;

                    return ((ColorUtils.calculateContrast(color, foreground) > minContrastRatioVsForeground)
                            && (ColorUtils.calculateContrast(color, background) > minContrastRatioVsBackground)
                            && (ColorUtils.calculateContrast(color, primaryColor) > minContrastRatioVsPrimary));
                };

        final Function3<Integer, Integer, Integer, Integer> deriveColorByContrast =
                (@ColorInt Integer color, @ColorInt Integer foreground, @ColorInt Integer background) -> {
                    for (float step=0.1f; step < 1.0; step += 0.1) {
                        int lighten = ColorUtil.shiftColor(color, 1.0f + step);
                        if (isContrastedEnough.invoke(lighten, foreground, background)) {return lighten;}

                        int darken = ColorUtil.shiftColor(color, 1.0f - step);
                        if (isContrastedEnough.invoke(darken, foreground, background)) {return darken;}
                    }

                    // fallback, use a neutral color
                    return ContextCompat.getColor(context, R.color.md_grey_500);
                };

        final Supplier<Integer> foregroundColor = () -> {
            final TypedValue typedColor = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.textColor, typedColor, true);
            return ColorUtils.setAlphaComponent(typedColor.data, 255);
        };

        final Supplier<Integer> backgroundColor = () -> {
            final TypedValue typedColor = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.itemBackground, typedColor, true);
            return ColorUtils.setAlphaComponent(typedColor.data, 255);
        };

        return deriveColorByContrast.invoke(primaryColor, foregroundColor.get(), backgroundColor.get());
    }

    @ColorInt
    public static int deriveAccentColorFromPrimaryColor(@NonNull final Context context, @ColorInt int primaryColor) {
        //return deriveAccentColorFromPrimaryColor_ImplByRule(context, primaryColor);
        return deriveAccentColorFromPrimaryColor_ImplByContrast(context, primaryColor);
    }
}
