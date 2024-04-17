package com.poupa.vinylmusicplayer.adapter.song;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.afollestad.materialcab.attached.AttachedCab;
import com.afollestad.materialcab.attached.AttachedCabKt;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.menu.SongMenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabCallbacks;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PlayingSongDecorationUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSongAdapter extends ArrayAdapter<Song> implements CabCallbacks {
    @Nullable
    private final CabHolder cabHolder;
    private AttachedCab cab;
    private ArrayList<Song> dataSet;
    private final ArrayList<Song> checked;

    private int color;

    @NonNull
    final AppCompatActivity activity;

    public ArtistSongAdapter(@NonNull AppCompatActivity activity, @NonNull ArrayList<Song> dataSet, @Nullable CabHolder cabHolder) {
        super(activity, R.layout.item_list, dataSet);
        this.activity = activity;
        this.cabHolder = cabHolder;
        this.dataSet = dataSet;
        checked = new ArrayList<>();
    }

    public ArrayList<Song> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Song> dataSet) {
        this.dataSet = dataSet;
        clear();
        addAll(dataSet);
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }

        final TextView songTitle = convertView.findViewById(R.id.title);
        final TextView songInfo = convertView.findViewById(R.id.text);
        final ImageView albumArt = convertView.findViewById(R.id.image);
        final com.google.android.material.card.MaterialCardView imageBorderTheme = convertView.findViewById(R.id.imageBorderTheme);
        final View shortSeparator = convertView.findViewById(R.id.short_separator);

        if (shortSeparator != null) {
            if ((position == getCount() - 1)) {
                shortSeparator.setVisibility(View.GONE);
            } else {
                shortSeparator.setVisibility(ThemeStyleUtil.getInstance().getShortSeparatorVisibilityState());
            }
        }

        songTitle.setText(song.getTitle());
        songInfo.setText(MusicUtil.getSongInfoString(song));

        // TODO This album art loading can be factorized with the decorate() helper function
        if (!MusicPlayerRemote.isPlaying(song)) {
            GlideApp.with(activity)
                .asDrawable()
                .load(VinylGlideExtension.getSongModel(song))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(song)
                .into(albumArt);
        }
        PlayingSongDecorationUtil.decorate(songTitle, albumArt, null, song, activity, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            albumArt.setTransitionName(activity.getString(R.string.transition_album_art));
        }
        ThemeStyleUtil.getInstance().setHeightListItem(convertView, activity.getResources().getDisplayMetrics().density);
        imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));

        final ImageView overflowButton = convertView.findViewById(R.id.menu);
        overflowButton.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
            @Override
            public Song getSong() {
                return song;
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_go_to_album) {
                    Pair<View, String>[] albumPairs = new Pair[]{
                            Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_art))
                    };
                    NavigationUtil.goToAlbum(activity, song.albumId, albumPairs);
                    return true;
                }
                return super.onMenuItemClick(item);
            }
        });

        convertView.setActivated(isChecked(song));
        convertView.setOnClickListener(view -> {
            if (isInQuickSelectMode()) {
                toggleChecked(song);
            } else {
                MusicPlayerRemote.enqueueSongsWithConfirmation(activity, dataSet, position);
            }
        });
        convertView.setOnLongClickListener(view -> {
            toggleChecked(song);
            return true;
        });

        return convertView;
    }

    private void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
        SongsMenuHelper.handleMenuClick(activity, selection, menuItem.getItemId());
    }

    protected void toggleChecked(Song song) {
        if (cabHolder != null) {
            if (!checked.remove(song)) {checked.add(song);}
            notifyDataSetChanged();

            cab = CabHolder.updateCab(
                    activity,
                    cab,
                    () -> cabHolder.openCab(R.menu.menu_media_selection, this),
                    checked.size());
        }
    }

    private void unCheckAll() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(Song song) {
        return checked.contains(song);
    }

    protected boolean isInQuickSelectMode() {
        return cab != null && AttachedCabKt.isActive(cab);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void onCabCreate(AttachedCab materialCab, Menu menu) {
        AbsThemeActivity.static_setStatusbarColor(activity, VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(color));
    }

    @Override
    public boolean onCabSelection(@NonNull MenuItem menuItem) {
        onMultipleItemAction(menuItem, new ArrayList<>(checked));
        AttachedCabKt.destroy(cab);
        unCheckAll();
        return true;
    }

    @Override
    public boolean onCabDestroy(AttachedCab materialCab) {
        AbsThemeActivity.static_setStatusbarColor(activity, color);
        unCheckAll();
        return true;
    }
}
