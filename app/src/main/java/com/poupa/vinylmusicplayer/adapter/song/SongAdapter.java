package com.poupa.vinylmusicplayer.adapter.song;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.base.AbsMultiSelectAdapter;
import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListSingleRowBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.menu.SongMenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.sort.SongSortOrder;
import com.poupa.vinylmusicplayer.sort.SortOrder;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PlayingSongDecorationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongAdapter
        extends AbsMultiSelectAdapter<SongAdapter.ViewHolder, Song>
        implements FastScrollRecyclerView.SectionedAdapter
{

    protected final AppCompatActivity activity;
    protected List<? extends Song> dataSet;

    protected final int itemLayoutRes;

    protected boolean usePalette;
    protected final boolean showSectionName;
    protected boolean showAlbumImage = true;

    public RecyclerView recyclerView;

    public SongAdapter(AppCompatActivity activity, List<? extends Song> dataSet, @LayoutRes int itemLayoutRes,
                       boolean usePalette, @Nullable CabHolder cabHolder) {
        this(activity, dataSet, itemLayoutRes, usePalette, cabHolder, true);
    }

    public SongAdapter(AppCompatActivity activity, List<? extends Song> dataSet, @LayoutRes int itemLayoutRes,
                       boolean usePalette, @Nullable CabHolder cabHolder, boolean showSectionName) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
        this.showSectionName = showSectionName;
        setHasStableIds(true);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rV) {
        super.onAttachedToRecyclerView(rV);
        recyclerView = rV;
    }

    public void swapDataSet(List<? extends Song> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public boolean isUsePalette() {
        return this.usePalette;
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public boolean isShowAlbumImage() {
        return this.showAlbumImage;
    }

    public List<? extends Song> getDataSet() {
        return dataSet;
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        if (itemLayoutRes == R.layout.item_list) {
            ItemListBinding binding = ItemListBinding.inflate(inflater, parent, false);
            return createViewHolder(binding);
        }
        else if (itemLayoutRes == R.layout.item_grid) {
            ItemGridBinding binding = ItemGridBinding.inflate(inflater, parent, false);
            return createViewHolder(binding);
        }
        else {
            throw new AssertionError("Unsupported song layout=" + itemLayoutRes);
        }
    }

    @NonNull
    protected ViewHolder createViewHolder(@NonNull ItemListSingleRowBinding binding) {
        return new ViewHolder(binding);
    }

    @NonNull
    protected ViewHolder createViewHolder(@NonNull ItemListBinding binding) {
        return new ViewHolder(binding);
    }

    @NonNull
    protected ViewHolder createViewHolder(@NonNull ItemGridBinding binding) {
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        boolean isChecked = isChecked(song);
        holder.itemView.setActivated(isChecked);

        if (holder.shortSeparator != null) {
            if (holder.getBindingAdapterPosition() == getItemCount() - 1) {
                holder.shortSeparator.setVisibility(View.GONE);
            } else {
                holder.shortSeparator.setVisibility(ThemeStyleUtil.getInstance().getShortSeparatorVisibilityState());
            }
        }

        if (holder.title != null) {
            holder.title.setText(song.title);
        }
        if (holder.text != null) {
            holder.text.setText(getSongText(song));
        }

        PlayingSongDecorationUtil.decorate(this, holder, song, activity);
    }

    public void setColors(int color, ViewHolder holder) {
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

    protected String getSongText(Song song) {
        return MusicUtil.getSongInfoString(song);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Song getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
        SongsMenuHelper.handleMenuClick(activity, selection, menuItem.getItemId());
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (!showSectionName) {
            return "";
        }

        SortOrder<Song> sortOrder = SongSortOrder.fromPreference(PreferenceUtil.getInstance().getSongSortOrder());
        return sortOrder.sectionNameBuilder.apply(dataSet.get(position));
    }

    public class ViewHolder extends MediaEntryViewHolder {
        protected int DEFAULT_MENU_RES = SongMenuHelper.MENU_RES;

        public ViewHolder(@NonNull ItemListSingleRowBinding binding) {
            super(binding);

            setImageTransitionName(activity.getString(R.string.transition_album_art));
            setupMenuHandlers();
        }

        public ViewHolder(@NonNull ItemListBinding binding) {
            super(binding);

            View itemView = binding.getRoot();
            ThemeStyleUtil.getInstance().setHeightListItem(itemView, activity.getResources().getDisplayMetrics().density);
            imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));

            setImageTransitionName(activity.getString(R.string.transition_album_art));
            setupMenuHandlers();
        }

        public ViewHolder(@NonNull ItemGridBinding binding) {
            super(binding);

            setImageTransitionName(activity.getString(R.string.transition_album_art));
            imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));
        }

        private void setupMenuHandlers() {
            menu.setOnTouchListener((v, ev) -> {
                menu.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
            menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                @Override
                public Song getSong() {
                    return ViewHolder.this.getSong();
                }

                @Override
                public int getMenuRes() {
                    return getSongMenuRes();
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onSongMenuItemClick(item) || super.onMenuItemClick(item);
                }
            });
        }

        protected Song getSong() {
            final int position = getBindingAdapterPosition();
            if (position < 0 || position >= dataSet.size()) {return Song.EMPTY_SONG;}

            return dataSet.get(getBindingAdapterPosition());
        }

        protected int getSongMenuRes() {
            return DEFAULT_MENU_RES;
        }

        protected boolean onSongMenuItemClick(MenuItem item) {
            if ((image != null) && (image.getVisibility() == View.VISIBLE) && (item.getItemId() == R.id.action_go_to_album)) {
                Pair<View, String>[] albumPairs = new Pair[]{
                        Pair.create(image, activity.getResources().getString(R.string.transition_album_art))
                };
                NavigationUtil.goToAlbum(activity, getSong().albumId, albumPairs);
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getBindingAdapterPosition());
            } else {
                MusicPlayerRemote.enqueueSongsWithConfirmation(v.getContext(), dataSet, getBindingAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return toggleChecked(getBindingAdapterPosition());
        }
    }
}
