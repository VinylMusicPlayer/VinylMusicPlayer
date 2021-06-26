package com.poupa.vinylmusicplayer.util.ImageTheme;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter.ViewHolder;
import com.poupa.vinylmusicplayer.databinding.SubHeaderMaterialBinding;


class MaterialTheme implements ThemeStyle {

    public boolean showSongAlbumArt() {
        return true;
    }

    public float getAlbumRadiusImage(Activity activity) {
        return activity.getResources().getDimension(R.dimen.album_corner_radius);
    }

    public float getArtistRadiusImage(Activity activity) {
        return activity.getResources().getDimension(R.dimen.artist_corner_radius);
    }

    public void setHeightListItem(View itemView, float density) {
        int padding_in_dp = 64;  // 64 dps
        int padding_in_px = (int) (padding_in_dp * density + 0.5f);

        itemView.getLayoutParams().height = padding_in_px;
    }

    public void setHeaderPadding(RecyclerView recyclerView, float density) {
        int padding_in_dp = 12;  // 12 dps
        int padding_in_px = (int) (padding_in_dp * density + 0.5f);
        recyclerView.setPadding(0, padding_in_px, 0, 0);
    }

    public void setHeaderText(@NonNull ViewHolder holder, AppCompatActivity activity, String title) {
        holder.title.setText(title.toUpperCase());

        int accentColor = ThemeStore.accentColor(activity);
        holder.title.setTextColor(accentColor);
        holder.title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    public void setSearchCardItemStyle(View itemView, AppCompatActivity activity) {
        //do nothing
    }

    public void setPlaylistCardItemStyle(View itemView, AppCompatActivity activity) {
        itemView.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
    }

    public void setDragView(AppCompatImageView dragView) {
        dragView.setImageResource(R.drawable.ic_drag_vertical_material_white_24dp);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) dragView.getLayoutParams();
        p.leftMargin = 0;
    }

    public SearchAdapter.ViewHolder HeaderViewHolder(SearchAdapter adapter, LayoutInflater inflater, @NonNull ViewGroup parent, boolean attachToParent) {
        return adapter.new ViewHolder(SubHeaderMaterialBinding.inflate(inflater, parent, attachToParent));
    }

    public int getShortSeparatorVisibilityState() {
        return View.GONE;
    }
}