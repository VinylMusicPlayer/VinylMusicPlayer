package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.TabLayoutUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.MusicLibraryPagerAdapter;
import com.poupa.vinylmusicplayer.databinding.FragmentLibraryBinding;
import com.poupa.vinylmusicplayer.dialogs.CreatePlaylistDialog;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.sort.AlbumSortOrder;
import com.poupa.vinylmusicplayer.sort.ArtistSortOrder;
import com.poupa.vinylmusicplayer.sort.SongSortOrder;
import com.poupa.vinylmusicplayer.sort.SortOrder;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.ui.activities.SearchActivity;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager.AbsLibraryPagerRecyclerViewCustomGridSizeFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager.AlbumsFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager.ArtistsFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager.PlaylistsFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager.SongsFragment;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.Util;

public class LibraryFragment
        extends AbsMainActivityFragment
        implements
            MainActivity.MainActivityFragmentCallbacks,
            ViewPager.OnPageChangeListener,
            SharedPreferences.OnSharedPreferenceChangeListener
{
    private FragmentLibraryBinding layoutBinding;

    private MusicLibraryPagerAdapter pagerAdapter;

    public static LibraryFragment newInstance() {
        return new LibraryFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        layoutBinding = FragmentLibraryBinding.inflate(inflater, container, false);
        return layoutBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        PreferenceUtil.getInstance().unregisterOnSharedPreferenceChangedListener(this);
        super.onDestroyView();
        layoutBinding.pager.removeOnPageChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);
        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpToolbar();
        setUpViewPager();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (PreferenceUtil.LIBRARY_CATEGORIES.equals(key)) {
            final Fragment current = getCurrentFragment();
            pagerAdapter.setCategoryInfos(PreferenceUtil.getInstance().getLibraryCategoryInfos());
            layoutBinding.pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);
            int position = pagerAdapter.getItemPosition(current);
            if (position < 0) position = 0;
            layoutBinding.pager.setCurrentItem(position);
            PreferenceUtil.getInstance().setLastPage(position);

            // hide the tab bar with single tab
            layoutBinding.tabs.setVisibility(pagerAdapter.getCount() == 1 ? View.GONE : View.VISIBLE);
        }
    }

    private void setUpToolbar() {
        final int primaryColor = ThemeStore.primaryColor(requireActivity());
        layoutBinding.appbar.setBackgroundColor(primaryColor);
        layoutBinding.toolbar.setBackgroundColor(primaryColor);
        layoutBinding.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        requireActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(layoutBinding.toolbar);
    }

    private void setUpViewPager() {
        pagerAdapter = new MusicLibraryPagerAdapter(requireActivity(), getChildFragmentManager());
        layoutBinding.pager.setAdapter(pagerAdapter);
        layoutBinding.pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        layoutBinding.tabs.setupWithViewPager(layoutBinding.pager);

        final int primaryColor = ThemeStore.primaryColor(requireActivity());
        final int normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(requireActivity(), primaryColor);
        final int selectedColor = ToolbarContentTintHelper.toolbarTitleColor(requireActivity(), primaryColor);
        TabLayoutUtil.setTabIconColors(layoutBinding.tabs, normalColor, selectedColor);
        layoutBinding.tabs.setTabTextColors(normalColor, selectedColor);
        layoutBinding.tabs.setSelectedTabIndicatorColor(ThemeStore.accentColor(requireActivity()));

        updateTabVisibility();

        if (PreferenceUtil.getInstance().rememberLastTab()) {
            layoutBinding.pager.setCurrentItem(PreferenceUtil.getInstance().getLastPage());
        }
        layoutBinding.pager.addOnPageChangeListener(this);
    }

    private void updateTabVisibility() {
        // hide the tab bar when only a single tab is visible
        layoutBinding.tabs.setVisibility(pagerAdapter.getCount() == 1 ? View.GONE : View.VISIBLE);
    }

    private Fragment getCurrentFragment() {
        return pagerAdapter.getFragment(layoutBinding.pager.getCurrentItem());
    }

    private boolean isPlaylistPage() {
        return getCurrentFragment() instanceof PlaylistsFragment;
    }

    public void addOnAppBarOffsetChangedListener(final AppBarLayout.OnOffsetChangedListener listener) {
        layoutBinding.appbar.addOnOffsetChangedListener(listener);
    }

    public void removeOnAppBarOffsetChangedListener(final AppBarLayout.OnOffsetChangedListener listener) {
        layoutBinding.appbar.removeOnOffsetChangedListener(listener);
    }

    public int getTotalAppBarScrollingRange() {
        return layoutBinding.appbar.getTotalScrollRange();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        if (isPlaylistPage()) {
            menu.add(0, R.id.action_new_playlist, 0, R.string.new_playlist_title);
        }
        final Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment && currentFragment.isAdded()) {

            final MenuItem gridSizeItem = menu.findItem(R.id.action_grid_size);
            if (Util.isLandscape(getResources())) {
                gridSizeItem.setTitle(R.string.action_grid_size_land);
            }
            setUpGridSizeMenu(fragment, gridSizeItem.getSubMenu());

            final MenuItem actionShowFooter = menu.findItem(R.id.action_show_footer);
            final MenuItem actionColoredFooters = menu.findItem(R.id.action_colored_footers);

            actionShowFooter.setChecked(fragment.showFooter());
            actionShowFooter.setEnabled(fragment.canUsePalette());
            actionShowFooter.setOnMenuItemClickListener(item -> {
                // item.isChecked() is inverted because this runs before the checked state updates
                actionColoredFooters.setEnabled(fragment.canUsePalette() && !item.isChecked());
                return false;
            });

            actionColoredFooters.setChecked(fragment.usePalette());
            actionColoredFooters.setEnabled(fragment.canUsePalette() && fragment.showFooter());

            setUpSortOrderMenu(fragment, menu.findItem(R.id.action_sort_order).getSubMenu());
        } else {
            menu.removeItem(R.id.action_grid_size);
            menu.removeItem(R.id.action_show_footer);
            menu.removeItem(R.id.action_colored_footers);
            menu.removeItem(R.id.action_sort_order);
        }
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
                requireActivity(),
                layoutBinding.toolbar,
                menu,
                ATHToolbarActivity.getToolbarBackgroundColor(layoutBinding.toolbar));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final Activity activity = getActivity();
        if (activity == null) {return;}
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, layoutBinding.toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment) {
            if (item.getItemId() == R.id.action_show_footer) {
                item.setChecked(!item.isChecked());
                fragment.setAndSaveShowFooter(item.isChecked());
                return true;
            } else if (item.getItemId() == R.id.action_colored_footers) {
                item.setChecked(!item.isChecked());
                fragment.setAndSaveUsePalette(item.isChecked());
                return true;
            }
            if (handleGridSizeMenuItem(fragment, item)) {
                return true;
            }
            if (handleSortOrderMenuItem(fragment, item)) {
                return true;
            }
        }

        final int id = item.getItemId();
        if (id == R.id.action_shuffle_all) {
            MusicPlayerRemote.openAndShuffleQueue(Discography.getInstance().getAllSongs(null), true);
            return true;
        } else if (id == R.id.action_new_playlist) {
            CreatePlaylistDialog.create().show(getChildFragmentManager(), "CREATE_PLAYLIST");
            return true;
        } else if (id == R.id.action_search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpGridSizeMenu(@NonNull final AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull final SubMenu gridSizeMenu) {
        switch (fragment.getGridSize()) {
            case 1:
                gridSizeMenu.findItem(R.id.action_grid_size_1).setChecked(true);
                break;
            case 2:
                gridSizeMenu.findItem(R.id.action_grid_size_2).setChecked(true);
                break;
            case 3:
                gridSizeMenu.findItem(R.id.action_grid_size_3).setChecked(true);
                break;
            case 4:
                gridSizeMenu.findItem(R.id.action_grid_size_4).setChecked(true);
                break;
            case 5:
                gridSizeMenu.findItem(R.id.action_grid_size_5).setChecked(true);
                break;
            case 6:
                gridSizeMenu.findItem(R.id.action_grid_size_6).setChecked(true);
                break;
            case 7:
                gridSizeMenu.findItem(R.id.action_grid_size_7).setChecked(true);
                break;
            case 8:
                gridSizeMenu.findItem(R.id.action_grid_size_8).setChecked(true);
                break;
        }
        final int maxGridSize = fragment.getMaxGridSize();
        if (maxGridSize < 8) {
            gridSizeMenu.findItem(R.id.action_grid_size_8).setVisible(false);
        }
        if (maxGridSize < 7) {
            gridSizeMenu.findItem(R.id.action_grid_size_7).setVisible(false);
        }
        if (maxGridSize < 6) {
            gridSizeMenu.findItem(R.id.action_grid_size_6).setVisible(false);
        }
        if (maxGridSize < 5) {
            gridSizeMenu.findItem(R.id.action_grid_size_5).setVisible(false);
        }
        if (maxGridSize < 4) {
            gridSizeMenu.findItem(R.id.action_grid_size_4).setVisible(false);
        }
        if (maxGridSize < 3) {
            gridSizeMenu.findItem(R.id.action_grid_size_3).setVisible(false);
        }
    }

    private boolean handleGridSizeMenuItem(@NonNull final AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull final MenuItem item) {
        int gridSize = 0;
        final int itemId = item.getItemId();
        if (itemId == R.id.action_grid_size_1) {
            gridSize = 1;
        } else if (itemId == R.id.action_grid_size_2) {
            gridSize = 2;
        } else if (itemId == R.id.action_grid_size_3) {
            gridSize = 3;
        } else if (itemId == R.id.action_grid_size_4) {
            gridSize = 4;
        } else if (itemId == R.id.action_grid_size_5) {
            gridSize = 5;
        } else if (itemId == R.id.action_grid_size_6) {
            gridSize = 6;
        } else if (itemId == R.id.action_grid_size_7) {
            gridSize = 7;
        } else if (itemId == R.id.action_grid_size_8) {
            gridSize = 8;
        }
        if (gridSize > 0) {
            item.setChecked(true);
            fragment.setAndSaveGridSize(gridSize);
            layoutBinding.toolbar.getMenu().findItem(R.id.action_show_footer).setEnabled(true);
            layoutBinding.toolbar.getMenu().findItem(R.id.action_colored_footers).setEnabled(fragment.canUsePalette());
            return true;
        }
        return false;
    }

    private void setUpSortOrderMenu(@NonNull final AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull final SubMenu sortOrderMenu) {
        final String currentSortOrder = fragment.getSortOrder();
        sortOrderMenu.clear();

        if (fragment instanceof AlbumsFragment) {
            AlbumSortOrder.buildMenu(sortOrderMenu, currentSortOrder);
        } else if (fragment instanceof ArtistsFragment) {
            ArtistSortOrder.buildMenu(sortOrderMenu, currentSortOrder);
        } else if (fragment instanceof SongsFragment) {
            SongSortOrder.buildMenu(sortOrderMenu, currentSortOrder);
        }

        sortOrderMenu.setGroupCheckable(0, true, true);
    }

    private boolean handleSortOrderMenuItem(@NonNull final AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull final MenuItem item) {
        String sortOrder = null;
        final int itemId = item.getItemId();
        if (fragment instanceof AlbumsFragment) {
            final SortOrder<Album> sorter = AlbumSortOrder.fromMenuResourceId(itemId);
            if (sorter != null) {sortOrder = sorter.preferenceValue;}
        } else if (fragment instanceof ArtistsFragment) {
            final SortOrder<Artist> sorter = ArtistSortOrder.fromMenuResourceId(itemId);
            if (sorter != null) {sortOrder = sorter.preferenceValue;}
        } else if (fragment instanceof SongsFragment) {
            final SortOrder<Song> sorter = SongSortOrder.fromMenuResourceId(itemId);
            if (sorter != null) {sortOrder = sorter.preferenceValue;}
        }

        if (sortOrder != null) {
            item.setChecked(true);
            fragment.setAndSaveSortOrder(sortOrder);
            return true;
        }

        return false;
    }

    @Override
    public boolean handleBackPress() {
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        PreferenceUtil.getInstance().setLastPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
