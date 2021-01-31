package com.poupa.vinylmusicplayer.discog;

import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.poupa.vinylmusicplayer.App;

/**
 * @author SC (soncaokim)
 */

public class SnackbarUtil {
    private Snackbar progressBar = null;
    private View viewContainer;

    public SnackbarUtil(View view) {
        viewContainer = view;
    }

    @NonNull
    private static CharSequence buildMessageWithIcon(@NonNull final CharSequence message) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {return message;}

        SpannableStringBuilder messageWithIcon = new SpannableStringBuilder();
        messageWithIcon.append(
                " ",
                new ImageSpan(App.getInstance().getApplicationContext(), Discography.ICON),
                0);
        messageWithIcon.append(" "); // some extra space before the text message
        messageWithIcon.append(message);

        return messageWithIcon;
    }

    public void showProgress(@NonNull final CharSequence message) {
        if (progressBar == null) {
            progressBar = Snackbar.make(
                    viewContainer,
                    buildMessageWithIcon(message),
                    Snackbar.LENGTH_INDEFINITE);
            progressBar.show();
        } else {
            progressBar.setText(message);
            if (!progressBar.isShownOrQueued()) {
                progressBar.show();
            }
        }
    }

    void showResult(@NonNull final CharSequence message) {
        dismiss();

        progressBar = Snackbar.make(
                viewContainer,
                buildMessageWithIcon(message),
                Snackbar.LENGTH_LONG);
        progressBar.show();
    }

    public void dismiss() {
        if ((progressBar != null) && progressBar.isShownOrQueued()) {
            progressBar.dismiss();
        }
        progressBar = null;
    }
}
