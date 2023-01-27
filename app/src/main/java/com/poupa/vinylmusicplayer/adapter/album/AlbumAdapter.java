package com.poupa.vinylmusicplayer.adapter.album;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.base.AbsMultiSelectAdapter;
import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemGridCardHorizontalBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.sort.AlbumSortOrder;
import com.poupa.vinylmusicplayer.sort.SortOrder;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> implements FastScrollRecyclerView.SectionedAdapter {

    protected final AppCompatActivity activity;
    protected ArrayList<Album> dataSet;

    protected final int itemLayoutRes;

    protected boolean usePalette;

    public AlbumAdapter(@NonNull AppCompatActivity activity, ArrayList<Album> dataSet, @LayoutRes int itemLayoutRes,
                        boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;

        setHasStableIds(true);
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public void swapDataSet(ArrayList<Album> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public ArrayList<Album> getDataSet() {
        return dataSet;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        if (itemLayoutRes == R.layout.item_grid) {
            ItemGridBinding binding = ItemGridBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }
        else if (itemLayoutRes == R.layout.item_list) {
            ItemListBinding binding = ItemListBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }
        else {
            throw new AssertionError("Unsupported album layout=" + itemLayoutRes);
        }
    }

    protected String getAlbumTitle(Album album) {
        return album.getTitle();
    }

    protected String getAlbumText(Album album) {
        return MusicUtil.buildInfoString(
                album.getArtistName(),
                MusicUtil.getSongCountString(activity, album.songs.size())
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        final boolean isChecked = isChecked(album);
        holder.itemView.setActivated(isChecked);

        if (holder.shortSeparator != null) {
            if (holder.getAdapterPosition() == getItemCount() - 1) {
                holder.shortSeparator.setVisibility(View.GONE);
            } else {
                holder.shortSeparator.setVisibility(ThemeStyleUtil.getInstance().getShortSeparatorVisibilityState());
            }
        }

        if (holder.title != null) {
            holder.title.setText(getAlbumTitle(album));
        }
        if (holder.text != null) {
            holder.text.setText(getAlbumText(album));
        }

        loadAlbumCover(album, holder);
    }

    protected void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }
    }

    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        GlideApp.with(activity)
                .asBitmapPalette()
                .load(VinylGlideExtension.getSongModel(album.safeGetFirstSong()))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(album.safeGetFirstSong())
                .into(new VinylColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getDefaultFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                        else
                            setColors(getDefaultFooterColor(), holder);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).getId();
    }

    @Override
    protected Album getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Album> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Album> albums) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(album.songs);
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        SortOrder<Album> sortOrder = AlbumSortOrder.fromPreference(PreferenceUtil.getInstance().getAlbumSortOrder());
        return sortOrder.sectionNameBuilder.apply(dataSet.get(position));
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull final ItemListBinding binding) {
            super(binding);

            View itemView = binding.getRoot();
            ThemeStyleUtil.getInstance().setHeightListItem(itemView, activity.getResources().getDisplayMetrics().density);
            imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));

            setImageTransitionName(activity.getString(R.string.transition_album_art));
            menu.setVisibility(View.GONE);
        }

        public ViewHolder(@NonNull final ItemGridBinding binding) {
            super(binding);
            setImageTransitionName(activity.getString(R.string.transition_album_art));
            imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));
        }

        public ViewHolder(@NonNull final ItemGridCardHorizontalBinding binding) {
            super(binding);
            setImageTransitionName(activity.getString(R.string.transition_album_art));
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair<View, String>[] albumPairs = new Pair[]{
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_album_art)
                        )};
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).getId(), albumPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            setColor(ThemeStore.primaryColor(activity));
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
