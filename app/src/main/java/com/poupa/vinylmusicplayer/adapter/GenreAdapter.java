package com.poupa.vinylmusicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.databinding.ItemListNoImageBinding;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    @NonNull
    final AppCompatActivity activity;
    ArrayList<Genre> dataSet;

    public GenreAdapter(@NonNull AppCompatActivity activity, ArrayList<Genre> dataSet) {
        this.activity = activity;
        this.dataSet = dataSet;
    }

    public ArrayList<Genre> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Genre> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListNoImageBinding binding = ItemListNoImageBinding.inflate(LayoutInflater.from(activity), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Genre genre = dataSet.get(position);

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.separator != null) {
                holder.separator.setVisibility(View.GONE);
            }
        } else {
            if (holder.separator != null) {
                holder.separator.setVisibility(View.VISIBLE);
            }
        }
        if (holder.shortSeparator != null) {
            holder.shortSeparator.setVisibility(View.GONE);
        }
        if (holder.menu != null) {
            holder.menu.setVisibility(View.GONE);
        }
        if (holder.title != null) {
            holder.title.setText(genre.getName());
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getGenreInfoString(activity, genre));
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        final Genre genre = dataSet.get(position);
        final String name = genre.id == -1 ? Genre.UNKNOWN_GENRE_DISPLAY_NAME : dataSet.get(position).getName();
        return String.valueOf(name.charAt(0)).toUpperCase();
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull ItemListNoImageBinding binding) {
            super(binding);
        }

        @Override
        public void onClick(View view) {
            Genre genre = dataSet.get(getAdapterPosition());
            NavigationUtil.goToGenre(activity, genre);
        }
    }
}
