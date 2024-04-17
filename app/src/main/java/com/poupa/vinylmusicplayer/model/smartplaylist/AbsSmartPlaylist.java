package com.poupa.vinylmusicplayer.model.smartplaylist;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.model.AbsCustomPlaylist;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.service.MusicService;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsSmartPlaylist extends AbsCustomPlaylist {
    @DrawableRes
    public final int iconRes;

    public AbsSmartPlaylist(final String name, final int iconRes) {
        super(-Math.abs(31 * name.hashCode() + (iconRes * name.hashCode() * 31 * 31)), name);
        this.iconRes = iconRes;
    }

    public boolean isClearable() {
        return true;
    }

    public void clear(@NonNull Context context) {
        // Notify app of clear event, so that the smart playlists are refreshed
        if (isClearable()) context.sendBroadcast(new Intent(MusicService.META_CHANGED));
    }

    public boolean canImport() {return false;}

    public void importPlaylist(@NonNull Context context, @NonNull Playlist playlist) {
        // Notify app of the event, so that the smart playlists are refreshed
        if (canImport()) context.sendBroadcast(new Intent(MusicService.META_CHANGED));
    }

    @Nullable
    public String getPlaylistPreference() {return null;}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + iconRes;
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (super.equals(obj)) {
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AbsSmartPlaylist other = (AbsSmartPlaylist) obj;
            return iconRes == other.iconRes;
        }
        return false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.iconRes);
    }

    protected AbsSmartPlaylist(Parcel in) {
        super(in);
        this.iconRes = in.readInt();
    }
}
