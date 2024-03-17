package com.poupa.vinylmusicplayer.adapter.album;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ItemGridCardHorizontalBinding;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.HorizontalAdapterHelper;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class HorizontalAlbumAdapter extends AlbumAdapter {

    public HorizontalAlbumAdapter(@NonNull AppCompatActivity activity, ArrayList<Album> dataSet, boolean showFooter, boolean usePalette,
                                  @Nullable CabHolder cabHolder) {
        super(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, showFooter, usePalette, cabHolder);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ItemGridCardHorizontalBinding binding = ItemGridCardHorizontalBinding.inflate(inflater, parent, false);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.getRoot().getLayoutParams();
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType);
        return new ViewHolder(binding);
    }

    protected void updateDetails(int color, ViewHolder holder) {
        CardView card = (CardView) holder.itemView;
        card.setCardBackgroundColor(color);
        if (showFooter) {
            if (holder.imageBorderTheme != null) {
                ViewGroup.LayoutParams params = holder.imageBorderTheme.getLayoutParams();
                params.width = activity.getResources().getDimensionPixelSize(R.dimen.item_grid_card_horizontal_width);
                params.height = activity.getResources().getDimensionPixelSize(R.dimen.item_grid_card_horizontal_width) + activity.getResources().getDimensionPixelSize(R.dimen.item_grid_color_container_height);
                holder.imageBorderTheme.setLayoutParams(params);
                holder.imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));
            }
            if (holder.paletteColorContainer != null) {
                holder.paletteColorContainer.setVisibility(View.VISIBLE);
                holder.paletteColorContainer.setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, activity.getResources().getDimensionPixelSize(R.dimen.item_grid_color_container_height))
                );
            }

            if (holder.title != null) {
               holder.title.setVisibility(showFooter ? View.VISIBLE : View.GONE);
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setVisibility(showFooter ? View.VISIBLE : View.GONE);
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        } else {
            if (holder.paletteColorContainer != null) {
                holder.paletteColorContainer.setVisibility(View.GONE);
                holder.paletteColorContainer.setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
                );
            }
            if (holder.imageBorderTheme != null) {
                ViewGroup.LayoutParams params = holder.imageBorderTheme.getLayoutParams();
                params.width = activity.getResources().getDimensionPixelSize(R.dimen.item_grid_card_horizontal_width);
                params.height = activity.getResources().getDimensionPixelSize(R.dimen.item_grid_card_horizontal_width);
                holder.imageBorderTheme.setLayoutParams(params);
                holder.imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));
            }
        }
    }

    @Override
    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        if (holder.imageBorderTheme != null) {
            holder.imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(activity));
        }

        GlideApp.with(activity)
                .asBitmapPalette()
                .load(VinylGlideExtension.getSongModel(album.safeGetFirstSong()))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(album.safeGetFirstSong())
                .into(new VinylColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        updateDetails(getAlbumArtistFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            updateDetails(color, holder);
                        else
                            updateDetails(getAlbumArtistFooterColor(), holder);
                    }
                });
    }

    @Override
    protected String getAlbumText(Album album) {
        return MusicUtil.getYearString(album.getYear());
    }

    @Override
    public int getItemViewType(int position) {
        return HorizontalAdapterHelper.getItemViewtype(position, getItemCount());
    }
}
