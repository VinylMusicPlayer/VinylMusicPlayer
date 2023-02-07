package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import com.poupa.vinylmusicplayer.ui.fragments.AbsMusicServiceFragment;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.LibraryFragment;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AbsLibraryPagerFragment extends AbsMusicServiceFragment {
    public LibraryFragment getLibraryFragment() {
        return (LibraryFragment) getParentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onThemeColorsChanged() {}
}
