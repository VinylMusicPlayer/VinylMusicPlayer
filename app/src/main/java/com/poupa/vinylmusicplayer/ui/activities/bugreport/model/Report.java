package com.poupa.vinylmusicplayer.ui.activities.bugreport.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.github.ExtraInfo;

public class Report {
    private final CharSequence title;
    private final CharSequence description;
    private final CharSequence collectedInfo;
    private final ExtraInfo extraInfo;

    public Report(@NonNull final CharSequence title, @NonNull final CharSequence description, @Nullable final CharSequence collectedInfo,
                  final ExtraInfo extraInfo) {
        this.title = title;
        this.description = description;
        this.collectedInfo = collectedInfo;
        this.extraInfo = extraInfo;
    }

    public String getTitle() {
        return title.toString();
    }

    public String getDescription() {
        return description + "\n\n"
                + (collectedInfo != null ? collectedInfo : "") + "\n\n"
                + extraInfo.toMarkdown();
    }
}
