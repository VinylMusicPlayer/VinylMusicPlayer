package com.poupa.vinylmusicplayer.util.ImageTheme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter.ViewHolder;
import com.poupa.vinylmusicplayer.adapter.album.AlbumAdapter;
import com.poupa.vinylmusicplayer.adapter.artist.ArtistAdapter;
import com.poupa.vinylmusicplayer.adapter.song.PlayingQueueAdapter;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.glide.GlideRequest;

public interface ThemeStyle {
    boolean showSongAlbumArt();

    void setHeaderPadding(RecyclerView recyclerView, float density);
    void setHeaderText(ViewHolder holder, AppCompatActivity activity, String title);
    void setSearchCardItemStyle(View itemView, AppCompatActivity activity);
    void setPlaylistCardItemStyle(View itemView, AppCompatActivity activity);

    SearchAdapter.ViewHolder HeaderViewHolder(SearchAdapter adapter, LayoutInflater inflater, @NonNull ViewGroup parent, boolean attachToParent);
    int getShortSeparatorVisibilityState();
}