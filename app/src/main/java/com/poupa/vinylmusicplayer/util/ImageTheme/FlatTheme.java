package com.poupa.vinylmusicplayer.util.ImageTheme;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter.ViewHolder;
import com.poupa.vinylmusicplayer.databinding.SubHeaderBinding;


class FlatTheme implements ThemeStyle {

    public boolean showSongAlbumArt() {
        return false;
    }

    public float getAlbumRadiusImage(Activity activity) {
        return 0;
    }

    public float getArtistRadiusImage(Activity activity) {
        return 0;
    }

    public void setHeightListItem(View itemView, float density) {
        //do nothing
    }

    public void setHeaderPadding(RecyclerView recyclerView, float density) {
        //do nothing
    }

    public void setHeaderText(@NonNull ViewHolder holder, AppCompatActivity activity, String title) {
        holder.title.setText(title);
    }

    public void setSearchCardItemStyle(View itemView, AppCompatActivity activity) {
        itemView.setBackgroundColor(ATHUtil.resolveColor(activity, androidx.cardview.R.attr.cardBackgroundColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
        }
    }

    public void setPlaylistCardItemStyle(View itemView, AppCompatActivity activity) {
        setSearchCardItemStyle(itemView, activity);
    }

    public void setDragView(AppCompatImageView dragView) {
        //do nothing
    }

    public SearchAdapter.ViewHolder HeaderViewHolder(SearchAdapter adapter, LayoutInflater inflater, @NonNull ViewGroup parent, boolean attachToParent) {
        return adapter.new ViewHolder(SubHeaderBinding.inflate(inflater, parent, attachToParent));
    }

    public int getShortSeparatorVisibilityState() {
        return View.VISIBLE;
    }

    public int getBottomSheetStyle() { return R.style.BottomSheetDialog_Flat; }
}