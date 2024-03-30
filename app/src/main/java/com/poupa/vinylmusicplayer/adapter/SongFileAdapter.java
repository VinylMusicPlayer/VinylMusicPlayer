package com.poupa.vinylmusicplayer.adapter;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.base.AbsMultiSelectAdapter;
import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.audiocover.FileCover;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.sort.FileSortOrder;
import com.poupa.vinylmusicplayer.sort.SortOrder;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.ImageUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class SongFileAdapter extends AbsMultiSelectAdapter<SongFileAdapter.ViewHolder, File> implements FastScrollRecyclerView.SectionedAdapter {

    private static final int FILE = 0;
    private static final int FOLDER = 1;

    final AppCompatActivity activity;
    List<File> dataSet;
    @Nullable
    final Callbacks callbacks;

    public SongFileAdapter(@NonNull final AppCompatActivity activity, @NonNull final List<File> songFiles,
                           @Nullable final Callbacks callback, @Nullable final CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        dataSet = songFiles;
        callbacks = callback;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position).isDirectory() ? FOLDER : FILE;
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    public void swapDataSet(@NonNull List<File> songFiles) {
        dataSet = songFiles;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        ItemListBinding binding = ItemListBinding.inflate(LayoutInflater.from(activity), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int index) {
        final File file = dataSet.get(index);

        holder.itemView.setActivated(isChecked(index));

        if (holder.getBindingAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(ThemeStyleUtil.getInstance().getShortSeparatorVisibilityState());
            }
        }

        if (holder.title != null) {
            holder.title.setText(getFileTitle(file));
        }
        if (holder.text != null) {
            if (holder.getItemViewType() == FILE) {
                holder.text.setText(getFileText(file));
            } else {
                holder.text.setVisibility(View.GONE);
            }
        }

        if (holder.image != null) {
            loadFileImage(file, holder);
        }
    }

    private static String getFileTitle(@NonNull final File file) {
        return file.getName();
    }

    private static String getFileText(@NonNull final File file) {
        return file.isDirectory() ? null : readableFileSize(file.length());
    }

    private void loadFileImage(@NonNull final File file, @NonNull final ViewHolder holder) {
        final int iconColor = ATHUtil.resolveColor(activity, R.attr.iconColor);
        if (file.isDirectory()) {
            holder.image.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            holder.image.setImageResource(R.drawable.ic_folder_white_24dp);
        } else {
            final Drawable error = ImageUtil.getTintedVectorDrawable(activity, R.drawable.ic_file_music_white_24dp, iconColor);
            holder.image.setImageDrawable(error);

            GlideApp.with(activity)
                    .load(new FileCover(file))
                    .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .error(error)
                            .placeholder(error)
                            .signature(new MediaStoreSignature("", file.lastModified(), 0)))
                    .into(holder.image);
        }
    }

    private static String readableFileSize(long size) {
        if (size <= 0) {return size + " B";}
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected File getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(@NonNull final MenuItem menuItem, @NonNull final Map<Integer, File> selection) {
        if (callbacks == null) return;
        callbacks.onMultipleItemAction(menuItem, selection);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        final SortOrder<File> sortOrder = FileSortOrder.fromPreference(PreferenceUtil.getInstance().getFileSortOrder());
        return sortOrder.sectionNameBuilder.apply(dataSet.get(position));
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull final ItemListBinding binding) {
            super(binding);

            final View itemView = binding.getRoot();
            ThemeStyleUtil.getInstance().setHeightListItem(itemView, activity.getResources().getDisplayMetrics().density);
            imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));

            if (menu != null && callbacks != null) {
                menu.setOnClickListener(v -> {
                    final int position = getBindingAdapterPosition();
                    if (isPositionInRange(position)) {
                        callbacks.onFileMenuClicked(position, dataSet.get(position), v);
                    }
                });
            }
        }

        @Override
        public void onClick(final View v) {
            final int position = getAdapterPosition();
            if (isPositionInRange(position)) {
                if (isInQuickSelectMode()) {
                    toggleChecked(position);
                } else {
                    if (callbacks != null) {
                        callbacks.onFileSelected(position, dataSet.get(position));
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(final View view) {
            final int position = getAdapterPosition();
            return isPositionInRange(position) && toggleChecked(position);
        }

        private boolean isPositionInRange(int position) {
            return position >= 0 && position < dataSet.size();
        }
    }

    public interface Callbacks {
        void onFileSelected(final int position, @NonNull final File file);

        void onFileMenuClicked(final int position, @NonNull final File file, @NonNull final View view);

        void onMultipleItemAction(@NonNull final MenuItem item, @NonNull final Map<Integer, File> files);
    }
}
