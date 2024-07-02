package com.poupa.vinylmusicplayer.adapter.base;

import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemGridCardHorizontalBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListNoImageBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListSingleRowBinding;
import com.poupa.vinylmusicplayer.databinding.SubHeaderBinding;
import com.poupa.vinylmusicplayer.databinding.SubHeaderMaterialBinding;
import com.poupa.vinylmusicplayer.views.TouchInterceptHorizontalScrollView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MediaEntryViewHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener, View.OnLongClickListener {
    @Nullable
    public final FrameLayout dummyContainer;

    @Nullable
    public com.google.android.material.card.MaterialCardView imageBorderTheme;

    @Nullable
    public final ImageView image;

    @Nullable
    public final TextView imageText;

    @Nullable
    public final TextView title;

    @Nullable
    public final TouchInterceptHorizontalScrollView titleScrollview;

    @Nullable
    public final TextView text;

    @Nullable
    public final View menu;

    @Nullable
    public final View separator;

    @Nullable
    public final View shortSeparator;

    @Nullable
    public final View dragView;

    @Nullable
    public final View paletteColorContainer;

    public MediaEntryViewHolder(@NonNull ItemListBinding binding) {
        super(binding.getRoot());

        dummyContainer = binding.touchInterceptFramelayout;
        imageBorderTheme = binding.imageBorderTheme;
        image = binding.image;
        imageText = binding.imageText;
        title = binding.title;
        titleScrollview = binding.titleScrollview;
        text = binding.text;
        menu = binding.menu;
        separator = binding.separator;
        shortSeparator  = binding.shortSeparator;
        dragView = binding.dragView;
        paletteColorContainer = null;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public MediaEntryViewHolder(@NonNull ItemGridBinding binding) {
        super(binding.getRoot());

        dummyContainer = binding.touchInterceptFramelayout;
        imageBorderTheme = binding.imageBorderTheme;
        image = binding.image;
        imageText = null;
        title = binding.title;
        titleScrollview = binding.titleScrollview;
        text = binding.text;
        menu = null;
        separator = null;
        shortSeparator  = null;
        dragView = null;
        paletteColorContainer = binding.paletteColorContainer;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public MediaEntryViewHolder(@NonNull ItemGridCardHorizontalBinding binding) {
        super(binding.getRoot());

        dummyContainer = binding.touchInterceptFramelayout;
        imageBorderTheme = binding.imageBorderTheme;
        image = binding.image;
        imageText = null;
        title = binding.title;
        titleScrollview = binding.titleScrollview;
        text = binding.text;
        menu = null;
        separator = null;
        shortSeparator  = null;
        dragView = null;
        paletteColorContainer = binding.paletteColorContainer;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public MediaEntryViewHolder(@NonNull SubHeaderBinding binding) {
        super(binding.getRoot());

        dummyContainer = null;
        imageBorderTheme = null;
        image = null;
        imageText = null;
        title = binding.title;
        titleScrollview = null;
        text = null;
        menu = null;
        separator = null;
        shortSeparator  = null;
        dragView = null;
        paletteColorContainer = null;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public MediaEntryViewHolder(@NonNull SubHeaderMaterialBinding binding) {
        super(binding.getRoot());

        dummyContainer = null;
        image = null;
        imageText = null;
        title = binding.title;
        titleScrollview = null;
        text = null;
        menu = null;
        separator = null;
        shortSeparator  = null;
        dragView = null;
        paletteColorContainer = null;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public MediaEntryViewHolder(@NonNull ItemListSingleRowBinding binding) {
        super(binding.getRoot());

        dummyContainer = binding.touchInterceptFramelayout;
        imageBorderTheme = binding.imageBorderTheme;
        image = binding.image;
        imageText = binding.imageText;
        title = binding.title;
        titleScrollview = binding.titleScrollview;
        text = binding.text;
        menu = binding.menu;
        separator = binding.separator;
        shortSeparator  = binding.shortSeparator;
        dragView = binding.dragView;
        paletteColorContainer = null;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public MediaEntryViewHolder(@NonNull ItemListNoImageBinding binding) {
        super(binding.getRoot());

        dummyContainer = binding.touchInterceptFramelayout;
        imageBorderTheme = null;
        image = null;
        imageText = null;
        title = binding.title;
        titleScrollview = binding.titleScrollview;
        text = binding.text;
        menu = binding.menu;
        separator = binding.separator;
        shortSeparator  = binding.shortSeparator;
        dragView = binding.dragView;
        paletteColorContainer = null;

        final View itemView = binding.getRoot();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public View getSwipeableContainerView() {
        return null;
    }

    protected void setImageTransitionName(@NonNull String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && image != null) {
            image.setTransitionName(transitionName);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {
    }
}
