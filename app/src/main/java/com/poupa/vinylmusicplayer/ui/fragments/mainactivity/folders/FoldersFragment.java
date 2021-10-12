package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.folders;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.SongFileAdapter;
import com.poupa.vinylmusicplayer.databinding.FragmentFolderBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.SongMenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.misc.UpdateToastMediaScannerCompletionListener;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.poupa.vinylmusicplayer.util.FileUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.ViewUtil;
import com.poupa.vinylmusicplayer.views.BreadCrumbLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class FoldersFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks, CabHolder, BreadCrumbLayout.SelectionCallback, SongFileAdapter.Callbacks, AppBarLayout.OnOffsetChangedListener, LoaderManager.LoaderCallbacks<List<File>> {

    private static final int LOADER_ID = LoaderIds.FOLDERS_FRAGMENT;

    protected static final String PATH = "path";
    protected static final String CRUMBS = "crumbs";

    CoordinatorLayout coordinatorLayout;
    View container;
    View empty;
    Toolbar toolbar;
    BreadCrumbLayout breadCrumbs;
    AppBarLayout appbar;
    FastScrollRecyclerView recyclerView;

    private SongFileAdapter adapter;
    private MaterialCab cab;

    public FoldersFragment() {
    }

    public static FoldersFragment newInstance(Context context) {
        return newInstance(PreferenceUtil.getInstance().getStartDirectory());
    }

    public static FoldersFragment newInstance(File directory) {
        FoldersFragment frag = new FoldersFragment();
        Bundle b = new Bundle();
        b.putSerializable(PATH, directory);
        frag.setArguments(b);
        return frag;
    }

    public void setCrumb(BreadCrumbLayout.Crumb crumb, boolean addToHistory) {
        if (crumb == null) return;
        saveScrollPosition();
        breadCrumbs.setActiveOrAdd(crumb, false);
        if (addToHistory) {
            breadCrumbs.addHistory(crumb);
        }
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }

    private void saveScrollPosition() {
        BreadCrumbLayout.Crumb crumb = getActiveCrumb();
        if (crumb != null) {
            crumb.setScrollPosition(((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        }
    }

    @Nullable
    private BreadCrumbLayout.Crumb getActiveCrumb() {
        return breadCrumbs != null && breadCrumbs.size() > 0 ? breadCrumbs.getCrumb(breadCrumbs.getActiveIndex()) : null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CRUMBS, breadCrumbs.getStateWrapper());
    }

    private void restoreBreadcrumb(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            setCrumb(new BreadCrumbLayout.Crumb(FileUtil.safeGetCanonicalFile((File) getArguments().getSerializable(PATH))), true);
        } else {
            breadCrumbs.restoreFromStateWrapper(savedInstanceState.getParcelable(CRUMBS));
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentFolderBinding binding = FragmentFolderBinding.inflate(inflater, container, false);
        coordinatorLayout = binding.coordinatorLayout;
        this.container = binding.container;
        empty = binding.empty;
        toolbar = binding.toolbar;
        breadCrumbs = binding.breadCrumbs;
        appbar = binding.appbar;
        recyclerView = binding.recyclerView;

        restoreBreadcrumb(savedInstanceState);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpAppbarColor();
        setUpToolbar();
        setUpBreadCrumbs();
        setUpRecyclerView();
        setUpAdapter();
    }

    private void setUpAppbarColor() {
        int primaryColor = ThemeStore.primaryColor(getActivity());
        appbar.setBackgroundColor(primaryColor);
        toolbar.setBackgroundColor(primaryColor);
        breadCrumbs.setBackgroundColor(primaryColor);
        breadCrumbs.setActivatedContentColor(ToolbarContentTintHelper.toolbarTitleColor(getActivity(), primaryColor));
        breadCrumbs.setDeactivatedContentColor(ToolbarContentTintHelper.toolbarSubtitleColor(getActivity(), primaryColor));
    }

    private void setUpToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(toolbar);
    }

    private void setUpBreadCrumbs() {
        breadCrumbs.setCallback(this);
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(getActivity(), recyclerView, ThemeStore.accentColor(getActivity()));

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        appbar.addOnOffsetChangedListener(this);
    }

    private void setUpAdapter() {
        adapter = new SongFileAdapter(getMainActivity(), new LinkedList<>(), this, this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
        recyclerView.setAdapter(adapter);
        checkIsEmpty();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveScrollPosition();
    }

    @Override
    public void onDestroyView() {
        appbar.removeOnOffsetChangedListener(this);
        super.onDestroyView();
    }

    @Override
    public boolean handleBackPress() {
        if (cab != null && cab.isActive()) {
            cab.finish();
            return true;
        }
        if (breadCrumbs.popHistory()) {
            setCrumb(breadCrumbs.lastHistory(), false);
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public MaterialCab openCab(int menuRes, MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        adapter.setColor(ThemeStore.primaryColor(getActivity()));
        cab = MenuHelper.setOverflowMenu(getMainActivity(), menuRes, ThemeStore.primaryColor(getMainActivity()))
                .start(callback);

        MenuHelper.decorateDestructiveItems(cab.getMenu(), this.getContext());

        return cab;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_folders, menu);
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(getActivity(), toolbar, menu, ATHToolbarActivity.getToolbarBackgroundColor(toolbar));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(getActivity(), toolbar);
    }

    public static final FileFilter AUDIO_FILE_FILTER = file -> !file.isHidden() && (file.isDirectory() ||
                        FileUtil.fileIsMimeType(file, "audio/*", MimeTypeMap.getSingleton()) ||
                        FileUtil.fileIsMimeType(file, "application/opus", MimeTypeMap.getSingleton()) ||
                        FileUtil.fileIsMimeType(file, "application/ogg", MimeTypeMap.getSingleton()));

    @Override
    public void onCrumbSelection(BreadCrumbLayout.Crumb crumb, int index) {
        setCrumb(crumb, true);
    }

    public static File getDefaultStartDirectory() {
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File startFolder;
        if (musicDir.exists() && musicDir.isDirectory()) {
            startFolder = musicDir;
        } else {
            File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage.exists() && externalStorage.isDirectory()) {
                startFolder = externalStorage;
            } else {
                startFolder = new File("/"); // root
            }
        }
        return startFolder;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_go_to_start_directory) {
            setCrumb(new BreadCrumbLayout.Crumb(FileUtil.safeGetCanonicalFile(PreferenceUtil.getInstance().getStartDirectory())), true);
            return true;
        } else if (itemId == R.id.action_scan) {
            BreadCrumbLayout.Crumb crumb = getActiveCrumb();
            if (crumb != null) {
                if (((MainActivity) getActivity()).isNotScanning()) {
                    ((MainActivity) getActivity()).setScanning(true);
                    new ListPathsAsyncTask(getActivity(), this::scanPaths).execute(new ListPathsAsyncTask.LoadingInfo(crumb.getFile(), AUDIO_FILE_FILTER));
                }

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFileSelected(File file) {
        final File canonicalFile = FileUtil.safeGetCanonicalFile(file); // important as we compare the path value later
        if (canonicalFile.isDirectory()) {
            setCrumb(new BreadCrumbLayout.Crumb(canonicalFile), true);
        } else {
            FileFilter fileFilter = pathname -> !pathname.isDirectory() && AUDIO_FILE_FILTER.accept(pathname);
            new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
                int startIndex = -1;
                for (int i = 0; i < songs.size(); i++) {
                    if (canonicalFile.getPath().equals(songs.get(i).data)) {
                        startIndex = i;
                        break;
                    }
                }
                if (startIndex > -1) {
                    MusicPlayerRemote.openQueue(songs, startIndex, true);
                } else {
                    Snackbar.make(coordinatorLayout, Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store), canonicalFile.getName())), Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_scan, v -> scanPaths(new String[]{canonicalFile.getPath()}))
                            .setActionTextColor(ThemeStore.accentColor(getActivity()))
                            .show();
                }
            }).execute(new ListSongsAsyncTask.LoadingInfo(toList(canonicalFile.getParentFile()), fileFilter, getFileComparator()));
        }
    }

    @Override
    public void onMultipleItemAction(MenuItem item, ArrayList<File> files) {
        final int itemId = item.getItemId();
        new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
            if (!songs.isEmpty()) {
                SongsMenuHelper.handleMenuClick(getActivity(), songs, itemId);
            }
            if (songs.size() != files.size()) {
                Snackbar.make(coordinatorLayout, R.string.some_files_are_not_listed_in_the_media_store, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_scan, v -> {
                            String[] paths = new String[files.size()];
                            for (int i = 0; i < files.size(); i++) {
                                paths[i] = FileUtil.safeGetCanonicalPath(files.get(i));
                            }
                            scanPaths(paths);
                        })
                        .setActionTextColor(ThemeStore.accentColor(getActivity()))
                        .show();
            }
        }).execute(new ListSongsAsyncTask.LoadingInfo(files, AUDIO_FILE_FILTER, getFileComparator()));
    }

    private ArrayList<File> toList(File file) {
        ArrayList<File> files = new ArrayList<>(1);
        files.add(file);
        return files;
    }

    Comparator<File> fileComparator = (lhs, rhs) -> {
        if (lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        } else if (!lhs.isDirectory() && rhs.isDirectory()) {
            return 1;
        } else {
            return lhs.getAbsolutePath().compareToIgnoreCase
                    (rhs.getAbsolutePath());
        }
    };

    private Comparator<File> getFileComparator() {
        return fileComparator;
    }

    @Override
    public void onFileMenuClicked(final File file, View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        if (file.isDirectory()) {
            popupMenu.inflate(R.menu.menu_item_directory);
            popupMenu.setOnMenuItemClickListener(item -> {
                final int itemId = item.getItemId();
                if (itemId == R.id.action_play_next
                        || itemId == R.id.action_add_to_current_playing
                        || itemId == R.id.action_add_to_playlist
                        || itemId == R.id.action_delete_from_device) {
                    new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
                        if (!songs.isEmpty()) {
                            SongsMenuHelper.handleMenuClick(getActivity(), songs, itemId);
                        }
                    }).execute(new ListSongsAsyncTask.LoadingInfo(toList(file), AUDIO_FILE_FILTER, getFileComparator()));
                    return true;
                } else if (itemId == R.id.action_set_as_start_directory) {
                    PreferenceUtil.getInstance().setStartDirectory(file);
                    Toast.makeText(getActivity(), String.format(getString(R.string.new_start_directory), file.getPath()), Toast.LENGTH_SHORT).show();
                    
                    // Rescan if whitelist enabled
                    if (PreferenceUtil.getInstance().getWhitelistEnabled()) {
                        getContext().sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
                    }  
                  
                    return true;
                } else if (itemId == R.id.action_scan) {
                    if (((MainActivity) getActivity()).isNotScanning()) {
                        ((MainActivity) getActivity()).setScanning(true);
                        new ListPathsAsyncTask(getActivity(), this::scanPaths).execute(new ListPathsAsyncTask.LoadingInfo(file, AUDIO_FILE_FILTER));
                    }

                    return true;
                }
                return false;
            });
        } else {
            popupMenu.inflate(R.menu.menu_item_file);
            popupMenu.setOnMenuItemClickListener(item -> {
                final int itemId = item.getItemId();
                if (itemId == R.id.action_play_next || itemId == R.id.action_add_to_current_playing || itemId == R.id.action_add_to_playlist || itemId == R.id.action_go_to_album || itemId == R.id.action_go_to_artist || itemId == R.id.action_share || itemId == R.id.action_tag_editor || itemId == R.id.action_details || itemId == R.id.action_set_as_ringtone || itemId == R.id.action_delete_from_device) {
                    new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
                        if (!songs.isEmpty()) {
                            SongMenuHelper.handleMenuClick(getActivity(), songs.get(0), itemId);
                        } else {
                            Snackbar.make(coordinatorLayout, Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store), file.getName())), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_scan, v -> scanPaths(new String[]{FileUtil.safeGetCanonicalPath(file)}))
                                    .setActionTextColor(ThemeStore.accentColor(getActivity()))
                                    .show();
                        }
                    }).execute(new ListSongsAsyncTask.LoadingInfo(toList(file), AUDIO_FILE_FILTER, getFileComparator()));
                    return true;
                } else if (itemId == R.id.action_scan) {
                    scanPaths(new String[]{FileUtil.safeGetCanonicalPath(file)});
                    return true;
                }
                return false;
            });
        }
        popupMenu.show();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(), appbar.getTotalScrollRange() + verticalOffset);
    }

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void scanPaths(@Nullable String[] toBeScanned) {
        if (getActivity() == null) return;
        if (toBeScanned == null || toBeScanned.length < 1) {
            Toast.makeText(getActivity(), R.string.nothing_to_scan, Toast.LENGTH_SHORT).show();
        } else {
            MediaScannerConnection.scanFile(getActivity().getApplicationContext(), toBeScanned, null, new UpdateToastMediaScannerCompletionListener(getActivity(), toBeScanned));
        }
    }

    private void updateAdapter(@NonNull List<File> files) {
        adapter.swapDataSet(files);
        BreadCrumbLayout.Crumb crumb = getActiveCrumb();
        if (crumb != null && recyclerView != null) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(crumb.getScrollPosition(), 0);
        }
    }

    @Override
    @NonNull
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        return new AsyncFileLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<File>> loader, List<File> data) {
        updateAdapter(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<File>> loader) {
        updateAdapter(new LinkedList<>());
    }

    private static class AsyncFileLoader extends WrappedAsyncTaskLoader<List<File>> {
        private final WeakReference<FoldersFragment> fragmentWeakReference;

        public AsyncFileLoader(FoldersFragment foldersFragment) {
            super(foldersFragment.getActivity());
            fragmentWeakReference = new WeakReference<>(foldersFragment);
        }

        @Override
        public List<File> loadInBackground() {
            FoldersFragment foldersFragment = fragmentWeakReference.get();
            File directory = null;
            if (foldersFragment != null) {
                BreadCrumbLayout.Crumb crumb = foldersFragment.getActiveCrumb();
                if (crumb != null) {
                    directory = crumb.getFile();
                }
            }
            if (directory != null) {
                List<File> files = FileUtil.listFiles(directory, AUDIO_FILE_FILTER);
                Collections.sort(files, foldersFragment.getFileComparator());
                return files;
            } else {
                return new LinkedList<>();
            }
        }
    }

    private static class ListSongsAsyncTask extends ListingFilesDialogAsyncTask<ListSongsAsyncTask.LoadingInfo, Void, ArrayList<Song>> {
        private final WeakReference<Context> contextWeakReference;
        private final WeakReference<OnSongsListedCallback> callbackWeakReference;
        private final Object extra;

        public ListSongsAsyncTask(Context context, Object extra, OnSongsListedCallback callback) {
            super(context, 500);
            this.extra = extra;
            contextWeakReference = new WeakReference<>(context);
            callbackWeakReference = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkCallbackReference();
            checkContextReference();
        }

        @Override
        protected ArrayList<Song> doInBackground(LoadingInfo... params) {
            try {
                LoadingInfo info = params[0];
                List<File> files = FileUtil.listFilesDeep(info.files, info.fileFilter);

                if (isCancelled() || checkContextReference() == null || checkCallbackReference() == null)
                    return null;

                Collections.sort(files, info.fileComparator);

                Context context = checkContextReference();
                if (isCancelled() || context == null || checkCallbackReference() == null)
                    return null;

                return FileUtil.matchFilesWithMediaStore(files);
            } catch (Exception e) {
                e.printStackTrace();
                cancel(false);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Song> songs) {
            super.onPostExecute(songs);
            OnSongsListedCallback callback = checkCallbackReference();
            if (songs != null && callback != null)
                callback.onSongsListed(songs, extra);
        }

        private Context checkContextReference() {
            Context context = contextWeakReference.get();
            if (context == null) {
                cancel(false);
            }
            return context;
        }

        private OnSongsListedCallback checkCallbackReference() {
            OnSongsListedCallback callback = callbackWeakReference.get();
            if (callback == null) {
                cancel(false);
            }
            return callback;
        }

        public static class LoadingInfo {
            public final Comparator<File> fileComparator;
            public final FileFilter fileFilter;
            public final List<File> files;

            public LoadingInfo(@NonNull List<File> files, @NonNull FileFilter fileFilter, @NonNull Comparator<File> fileComparator) {
                this.fileComparator = fileComparator;
                this.fileFilter = fileFilter;
                this.files = files;
            }
        }

        public interface OnSongsListedCallback {
            void onSongsListed(@NonNull ArrayList<Song> songs, Object extra);
        }
    }

    public static class ListPathsAsyncTask extends ListingFilesDialogAsyncTask<ListPathsAsyncTask.LoadingInfo, String, String[]> {
        private final OnPathsListedCallback onPathsListedCallback;

        public ListPathsAsyncTask(Context context, OnPathsListedCallback callback) {
            super(context, 500);
            this.onPathsListedCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isContextStillInMemory();
        }

        @Override
        protected String[] doInBackground(LoadingInfo... params) {
            try {
                if (isCancelled() || !isContextStillInMemory()) {
                    return null;
                }

                LoadingInfo info = params[0];

                final String[] paths;

                if (info.file.isDirectory()) {
                    List<File> files = FileUtil.listFilesDeep(info.file, info.fileFilter);

                    if (isCancelled() || !isContextStillInMemory()) {
                        return null;
                    }

                    paths = new String[files.size()];
                    for (int i = 0; i < files.size(); i++) {
                        File f = files.get(i);
                        paths[i] = FileUtil.safeGetCanonicalPath(f);

                        if (isCancelled() || !isContextStillInMemory()) {
                            return null;
                        }
                    }
                } else {
                    paths = new String[1];
                    paths[0] = FileUtil.safeGetCanonicalPath(info.file);
                }

                return paths;
            } catch (Exception e) {
                e.printStackTrace();
                cancel(false);
                return null;
            }
        }

        @Override
        protected void onCancelled(String[] result) {
            disableScanning();
            super.onCancelled(result);
        }

        @Override
        protected void onPostExecute(String[] paths) {
            super.onPostExecute(paths);
            disableScanning();
            if (onPathsListedCallback != null && paths != null) {
                onPathsListedCallback.onPathsListed(paths);
            }
        }

        private void disableScanning() {
            Context context = getContext();
            if (context instanceof MainActivity) {
                ((MainActivity) context).setScanning(false);
            }
        }

        private boolean isContextStillInMemory() {
            if (getContext() == null) {
                cancel(false);
                return false;
            } else {
                return true;
            }
        }

        public static class LoadingInfo {
            public final File file;
            public final FileFilter fileFilter;

            public LoadingInfo(File file, FileFilter fileFilter) {
                this.file = file;
                this.fileFilter = fileFilter;
            }
        }

        public interface OnPathsListedCallback {
            void onPathsListed(@NonNull String[] paths);
        }
    }

    private static abstract class ListingFilesDialogAsyncTask<Params, Progress, Result> extends DialogAsyncTask<Params, Progress, Result> {

        public ListingFilesDialogAsyncTask(Context context, int showDelay) {
            super(context, showDelay);
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return new MaterialDialog.Builder(context)
                    .title(R.string.listing_files)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .cancelListener(dialog -> cancel(false))
                    .dismissListener(dialog -> cancel(false))
                    .negativeText(android.R.string.cancel)
                    .onNegative((dialog, which) -> cancel(false))
                    .show();
        }
    }
}
