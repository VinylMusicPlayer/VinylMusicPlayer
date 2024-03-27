package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;

import java.util.Objects;

/**
 * @author SC (soncaokim)
 */

class SnackbarUtil {
    @DrawableRes
    private static final int ICON = R.drawable.ic_bookmark_music_white_24dp;

    @Nullable
    private Snackbar progressBar = null;
    private final View viewContainer;

    SnackbarUtil(@NonNull final View view) {
        viewContainer = view;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    private static Drawable tintedIcon(@NonNull final Snackbar snackbar) {
        final Context context = App.getInstance().getApplicationContext();

        // Pick the color from the text view...
        final TextView tv = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        final int color = tv.getCurrentTextColor();

        // ... and apply the color on the icon
        final Drawable icon = AppCompatResources.getDrawable(context, ICON);
        Objects.requireNonNull(icon);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        icon.setTint(color);
        return icon;
    }

    @NonNull
    private static CharSequence buildMessageWithIcon(@NonNull final CharSequence message, @NonNull final Snackbar snackbar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {return message;}

        final SpannableStringBuilder messageWithIcon = new SpannableStringBuilder();
        messageWithIcon.append(
                " ",
                new ImageSpan(tintedIcon(snackbar)),
                0);
        messageWithIcon.append(" "); // some extra space before the text message
        messageWithIcon.append(message);

        return messageWithIcon;
    }

    private void adjustPosition(@NonNull final Snackbar snackbar) {
        final View bottomBar = viewContainer.findViewById(R.id.sliding_panel);
        if (bottomBar != null) {
            // avoid the bottom bar if the view has it, by sticking to the top of it
            snackbar.setAnchorView(bottomBar);
        }
    }

    void showProgress(@NonNull final CharSequence text) {
        if (progressBar == null) {
            progressBar = Snackbar.make(
                    viewContainer,
                    "",
                    BaseTransientBottomBar.LENGTH_LONG);
        }

        progressBar.setText(buildMessageWithIcon(text, progressBar));
        if (!progressBar.isShownOrQueued()) {
            adjustPosition(progressBar);
            progressBar.show();
        }
    }

    void dismiss() {
        if ((progressBar != null) && progressBar.isShownOrQueued()) {
            progressBar.dismiss();
        }
        progressBar = null;
    }
}
