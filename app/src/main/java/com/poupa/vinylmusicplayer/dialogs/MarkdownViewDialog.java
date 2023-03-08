package com.poupa.vinylmusicplayer.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

/**
 * @author SC (soncaokim)
 */
public class MarkdownViewDialog extends MaterialDialog {
    private String markdownContent;

    protected MarkdownViewDialog(Builder builder) {
        super(builder);
    }

    public MarkdownViewDialog setMarkdownContent(@NonNull final Context context, @NonNull final String content) {
        markdownContent = content;

        if (isShowing()) {
            final Markwon markwon = Markwon.builder(context)
                    .usePlugin(GlideImagesPlugin.create(context)) // image loader
                    .usePlugin(HtmlPlugin.create()) // basic Html tags
                    .usePlugin(new GithubLinkify())
                    .usePlugin(new CustomStyles(context))
                    .build();
            final TextView markdownView = getCustomView().findViewById(R.id.markdown_view);
            markwon.setMarkdown(markdownView, markdownContent);
        } else {
            setOnShowListener(dialog -> ((MarkdownViewDialog)dialog).setMarkdownContent(context, markdownContent));
        }

        return this;
    }

    public MarkdownViewDialog setMarkdownContentFromAsset(@NonNull final Context context, @NonNull final String assetName) {
        final String content = loadFileFromAssets(context, assetName);
        return setMarkdownContent(context, content);
    }

    @NonNull
    private String loadFileFromAssets(@NonNull final Context context, @NonNull final String name) {
        StringBuilder buf = new StringBuilder();
        try {
            InputStream json = context.getAssets().open(name);
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
        return buf.toString();
    }

    public static class Builder extends MaterialDialog.Builder {
        public Builder(@NonNull Context context) {
            super(context);
        }

        @Override
        public Builder title(@NonNull final CharSequence title) {
            super.title(title);
            return this;
        }

        @Override
        @UiThread
        public MarkdownViewDialog build() {
            final View customView = LayoutInflater.from(context).inflate(R.layout.dialog_markdown_view, null);
            customView(customView, true);
            positiveText(android.R.string.ok);

            return new MarkdownViewDialog(this);
        }
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

    private static class CustomStyles extends AbstractMarkwonPlugin {
        private @NonNull final Context context;
        public CustomStyles(@NonNull final Context context) {
            super();
            this.context = context;
        }

        @Override
        public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
            final TypedValue typedColor = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.dividerColor, typedColor, true);

            builder.headingBreakColor(Color.parseColor("#00ffffff"))
                    .thematicBreakColor(typedColor.data)
                    .thematicBreakHeight(2)
                    .bulletWidth(12)
                    .headingTextSizeMultipliers(
                            new float[] { 2.F, 1.5F, 1F, .83F, .67F, .55F });
        }
    }
}
