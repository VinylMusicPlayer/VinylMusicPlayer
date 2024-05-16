package com.poupa.vinylmusicplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.base.AbsMultiSelectAdapter;
import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemListSingleRowBinding;
import com.poupa.vinylmusicplayer.dialogs.ClearSmartPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.DeletePlaylistDialog;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.PlaylistMenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.misc.WeakContextAsyncTask;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.model.smartplaylist.AbsSmartPlaylist;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {

    private static final int SMART_PLAYLIST = 0;
    private static final int DEFAULT_PLAYLIST = 1;

    protected final AppCompatActivity activity;
    protected ArrayList<Playlist> dataSet;

    public PlaylistAdapter(@NonNull final AbsThemeActivity activity, ArrayList<Playlist> dataSet, @Nullable final PaletteColorHolder palette) {
        super(activity, palette, R.menu.menu_playlists_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        setHasStableIds(true);
    }

    public ArrayList<Playlist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Playlist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListSingleRowBinding binding = ItemListSingleRowBinding.inflate(LayoutInflater.from(activity), parent, false);
        return new ViewHolder(binding, viewType);
    }

    protected String getPlaylistTitle(Playlist playlist) {
        return playlist.name;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);

        holder.itemView.setActivated(isChecked(position));

        if (holder.title != null) {
            holder.title.setText(getPlaylistTitle(playlist));
        }
        if (holder.text != null) {
            // This operation lasts seconds
            // long enough to block the UI thread, but short enough to accept the context leak in an async operation
            new AsyncTask<Playlist, Void, String>() {
                @Override
                protected String doInBackground(Playlist... params) {
                    return params[0].getInfoString(activity);
                }

                @Override
                protected void onPostExecute(String info) {
                    holder.text.setText(info);
                }
            }.execute(playlist);
        }

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null && !(dataSet.get(position) instanceof AbsSmartPlaylist)) {
                holder.shortSeparator.setVisibility(ThemeStyleUtil.getInstance().getShortSeparatorVisibilityState());
            }
        }

        if (holder.image != null) {
            holder.image.setImageResource(getIconRes(playlist));
        }
    }

    private int getIconRes(Playlist playlist) {
        if (playlist instanceof AbsSmartPlaylist) {
            return ((AbsSmartPlaylist) playlist).iconRes;
        }
        return MusicUtil.isFavoritePlaylist(activity, playlist) ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_queue_music_white_24dp;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) instanceof AbsSmartPlaylist ? SMART_PLAYLIST : DEFAULT_PLAYLIST;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Playlist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(@NonNull final MenuItem menuItem, @NonNull final Map<Integer, Playlist> selection) {
        if (R.id.action_delete_playlist == menuItem.getItemId()) {
            final List<Playlist> staticPlaylists = new ArrayList<>();
            for (final Playlist playlist : selection.values()) {
                if (playlist instanceof AbsSmartPlaylist absSmartPlaylist) {
                    if (absSmartPlaylist.isClearable()) {
                        ClearSmartPlaylistDialog.create(absSmartPlaylist).show(activity.getSupportFragmentManager(), "CLEAR_PLAYLIST_" + absSmartPlaylist.name);
                    }
                }
                else {
                    staticPlaylists.add(playlist);
                }
            }
            if (!staticPlaylists.isEmpty()) {
                DeletePlaylistDialog.create(new ArrayList<>(staticPlaylists)).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
            }
        } else if (R.id.action_save_playlist == menuItem.getItemId()) {
            ArrayList<Playlist> playlists = new ArrayList<>(selection.values());
            if (playlists.size() == 1) {
                PlaylistMenuHelper.handleMenuClick(activity, playlists.get(0), menuItem);
            } else {
                new SavePlaylistsAsyncTask(activity).execute(playlists);
            }
        } else {
            SongsMenuHelper.handleMenuClick(activity, getSongList(selection.values().iterator()), menuItem.getItemId());
        }
    }

    private static class SavePlaylistsAsyncTask extends WeakContextAsyncTask<ArrayList<Playlist>, String, String> {
        public SavePlaylistsAsyncTask(Context context) {
            super(context);
        }

        @SafeVarargs
        @Override
        protected final String doInBackground(ArrayList<Playlist>... params) {
            int successes = 0;
            int failures = 0;

            String dir = "";
            final Context context = getContext();

            for (Playlist playlist : params[0]) {
                try {
                    dir = PlaylistsUtil.savePlaylist(context, playlist);
                    if (dir != null) {
                        successes++;
                    } else {
                        failures++;
                    }
                } catch (IOException e) {
                    OopsHandler.collectStackTrace(e);
                    failures++;
                }
            }

            return failures == 0
                    ? String.format(context.getString(R.string.saved_x_playlists_to_x), successes, dir)
                    : String.format(context.getString(R.string.saved_x_playlists_to_x_failed_to_save_x), successes, dir, failures);
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Context context = getContext();
            if (context != null) {
                SafeToast.show(context, string);
            }
        }
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull Iterator<Playlist> playlists) {
        final ArrayList<Song> songs = new ArrayList<>();
        playlists.forEachRemaining(playlist -> songs.addAll(playlist.getSongs(activity)));
        return songs;
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull ItemListSingleRowBinding binding, int itemViewType) {
            super(binding);

            if (itemViewType == SMART_PLAYLIST) {
                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
                View itemView = binding.getRoot();
                ThemeStyleUtil.getInstance().setPlaylistCardItemStyle(itemView, activity);
            }

            if (image != null) {
                int iconPadding = activity.getResources().getDimensionPixelSize(R.dimen.list_item_image_icon_padding);
                image.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
                image.setColorFilter(ATHUtil.resolveColor(activity, R.attr.iconColor), PorterDuff.Mode.SRC_IN);
            }

            if (menu != null) {
                menu.setOnClickListener(view -> {
                    final Playlist playlist = dataSet.get(getBindingAdapterPosition());
                    final PopupMenu popupMenu = new PopupMenu(activity, view);

                    if (itemViewType == SMART_PLAYLIST) {
                        popupMenu.inflate(R.menu.menu_item_smart_playlist);
                        PlaylistMenuHelper.hideShowSmartPlaylistMenuItems(popupMenu.getMenu(), (AbsSmartPlaylist)playlist);
                    }
                    else {
                        popupMenu.inflate(R.menu.menu_item_playlist);
                    }

                    MenuHelper.decorateDestructiveItems(popupMenu.getMenu(), activity);
                    popupMenu.setOnMenuItemClickListener(item -> PlaylistMenuHelper.handleMenuClick(
                            activity, dataSet.get(getBindingAdapterPosition()), item));
                    popupMenu.show();
                });
            }
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getAdapterPosition());
                NavigationUtil.goToPlaylist(activity, playlist);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
