package com.poupa.vinylmusicplayer.ui.fragments.mainactivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.poupa.vinylmusicplayer.ui.activities.MainActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMainActivityFragment extends Fragment {

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
