package com.poupa.vinylmusicplayer.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

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
import java.util.function.BiFunction;
import java.util.function.Function;

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

    @ColorInt
    public static int shiftBackgroundColorForLightText(@ColorInt int backgroundColor) {
        while (ColorUtil.isColorLight(backgroundColor)) {
            backgroundColor = ColorUtil.darkenColor(backgroundColor);
        }
        return backgroundColor;
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
    private static int deriveAccentColorFromPrimaryColor_ImplByRule(@NonNull final Context context, @ColorInt int primaryColor) {
        // Choice of accent color w.r.t the primary color
        // _____________
        //              \ General theme | Dark    | Light
        // Primary color \______________|_________|_______
        //   Dark                       | Lighten | Black
        //   Light                      | White   | Darken

        Function<Integer, Boolean> isColorDark = (@ColorInt Integer color) -> {
            double darkness = 1.0 -
                    (
                            0.299 * (double)Color.red(color) +
                            0.587 * (double)Color.green(color) +
                            0.114 * (double)Color.blue(color)
                    ) / 255.0;
            return darkness > 0.5;
        };

        final boolean themeDark = PreferenceUtil.getInstance().isGeneralThemeDark();
        final boolean primaryDark = isColorDark.apply(primaryColor);

        if (themeDark && primaryDark) {return ColorUtil.shiftColor(primaryColor, 1.8F);} // lighten
        else if (!themeDark && !primaryDark) {return ColorUtil.shiftColor(primaryColor, 0.2F);} // darken
        else if (themeDark && !primaryDark) {return ContextCompat.getColor(context, R.color.md_white_1000);}
        else /*if (!themeDark && primaryDark)*/ {return ContextCompat.getColor(context, R.color.md_black_1000);}
    }

    @ColorInt
    private static int deriveAccentColorFromPrimaryColor_ImplByContrast(@NonNull final Context context, @ColorInt final int primaryColor) {
        final BiFunction<Integer, Integer, Boolean> isContrastedEnough = (@ColorInt Integer color, @ColorInt Integer background) -> {
            final float minContrastRatioVsBackground = 1.7f;
            final float minContrastRatioVsPrimary = 1.7f;

            return (ColorUtils.calculateContrast(color, background) > minContrastRatioVsBackground)
                    && (ColorUtils.calculateContrast(color, primaryColor) > minContrastRatioVsPrimary);
        };

        final BiFunction<Integer, Integer, Integer> deriveColorByContrast = (@ColorInt Integer color, @ColorInt Integer background) -> {
            for (float step=0.1f; step < 1.0; step += 0.1) {
                int lighten = ColorUtil.shiftColor(color, 1.0f + step);
                if (isContrastedEnough.apply(lighten, background)) {return lighten;}

                int darken = ColorUtil.shiftColor(color, 1.0f - step);
                if (isContrastedEnough.apply(darken, background)) {return darken;}
            }

            // fallback, use a neutral color
            return ContextCompat.getColor(context, R.color.md_grey_500);
        };

        final boolean themeDark = PreferenceUtil.getInstance().isGeneralThemeDark();
        @ColorInt final int themeBackgroundColor = ContextCompat.getColor(
                context,
                themeDark ? R.color.md_black_1000 : R.color.md_white_1000);

        return deriveColorByContrast.apply(primaryColor, themeBackgroundColor);
    }

    @ColorInt
    public static int deriveAccentColorFromPrimaryColor(@NonNull final Context context, @ColorInt int primaryColor) {
        //return deriveAccentColorFromPrimaryColor_ImplByRule(context, primaryColor);
        return deriveAccentColorFromPrimaryColor_ImplByContrast(context, primaryColor);
    }
}
