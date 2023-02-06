package com.poupa.vinylmusicplayer.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;

/**
 * @author SC (soncaokim)
 */
public class MarkdownViewDialog extends DialogFragment {
    @NonNull
    private final String assetName;

    public MarkdownViewDialog(@NonNull final String assetName) {
        super();
        this.assetName = assetName;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = requireActivity();

        final View customView = getLayoutInflater().inflate(R.layout.dialog_markdown_view, null);
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .build();

        final Markwon markwon = Markwon.builder(activity)
                .usePlugin(GlideImagesPlugin.create(activity)) // image loader
                .usePlugin(new GithubLinkify())
                .build();
        final TextView markdownView = customView.findViewById(R.id.markdown_view);
        StringBuilder buf = loadFileFromAssets(activity, assetName);
        markwon.setMarkdown(markdownView, buf.toString());

        return dialog;
    }

    @NonNull
    private StringBuilder loadFileFromAssets(@NonNull Activity activity, @NonNull final String name) {
        StringBuilder buf = new StringBuilder();
        try {
            InputStream json = activity.getAssets().open(name);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
                buf.append('\n');
            }
            in.close();
        }
        catch (IOException ioe) {
            buf.append("Unable to load ").append(name)
                .append("\n\n")
                .append(ioe.getLocalizedMessage());
        }
        return buf;
    }

    // Github link shortener plugin
    private static class GithubLinkify extends AbstractMarkwonPlugin {
        @NonNull
        @Override
        public String processMarkdown(@NonNull String markdown) {
            return markdown
                    .replaceAll(
                            "(https://github.com/AdrienPoupa/VinylMusicPlayer/pull/)([0-9]+)",
                            "[PR #$2]($1$2)")
                    .replaceAll(
                            "(https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/)([0-9]+)",
                            "[PR #$2]($1$2)");
        }
    }
}
