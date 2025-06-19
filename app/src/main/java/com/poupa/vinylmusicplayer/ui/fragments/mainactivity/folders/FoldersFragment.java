package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.folders;

import android.app.Activity;
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
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.poupa.vinylmusicplayer.helper.menu.SongMenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.misc.UpdateToastMediaScannerCompletionListener;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.sort.FileSortOrder;
import com.poupa.vinylmusicplayer.sort.SortOrder;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.poupa.vinylmusicplayer.util.FileUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;
import com.poupa.vinylmusicplayer.util.ViewUtil;
import com.poupa.vinylmusicplayer.views.BreadCrumbLayout;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FoldersFragment
        extends AbsMainActivityFragment
        implements
            MainActivity.MainActivityFragmentCallbacks,
            BreadCrumbLayout.SelectionCallback,
            SongFileAdapter.Callbacks,
            AppBarLayout.OnOffsetChangedListener,
            LoaderManager.LoaderCallbacks<List<File>>
{

    private static final int LOADER_ID = LoaderIds.FOLDERS_FRAGMENT;

    private static final String PATH = "path";
    private static final String CRUMBS = "crumbs";
    private static int accentColor;

    private FragmentFolderBinding layoutBinding;

    private SongFileAdapter adapter;

    private String sortOrder;

    private FoldersFragment() {
        accentColor = PreferenceUtil.getInstance().getAccentColor();
    }

    public static FoldersFragment newInstance() {
        return newInstance(PreferenceUtil.getInstance().getStartDirectory());
    }

    public static FoldersFragment newInstance(final File directory) {
        final FoldersFragment frag = new FoldersFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(PATH, directory);
        frag.setArguments(bundle);
        return frag;
    }

    private void setCrumb(final BreadCrumbLayout.Crumb crumb, boolean addToHistory) {
        if (crumb == null) {return;}
        saveScrollPosition();
        layoutBinding.breadCrumbs.setActiveOrAdd(crumb, false);
        if (addToHistory) {
            layoutBinding.breadCrumbs.addHistory(crumb);
        }
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }

    private void saveScrollPosition() {
        final BreadCrumbLayout.Crumb crumb = getActiveCrumb();
        if (crumb != null) {
            crumb.setScrollPosition(((LinearLayoutManager) layoutBinding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        }
    }

    @Nullable
    BreadCrumbLayout.Crumb getActiveCrumb() {
        return layoutBinding.breadCrumbs.size() > 0
                ? layoutBinding.breadCrumbs.getCrumb(layoutBinding.breadCrumbs.getActiveIndex())
                : null;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CRUMBS, layoutBinding.breadCrumbs.getStateWrapper());
    }

    private void restoreBreadcrumb(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            setCrumb(new BreadCrumbLayout.Crumb(FileUtil.safeGetCanonicalFile((File) getArguments().getSerializable(PATH))), true);
        } else {
            layoutBinding.breadCrumbs.restoreFromStateWrapper(savedInstanceState.getParcelable(CRUMBS));
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        layoutBinding = FragmentFolderBinding.inflate(inflater, container, false);

        restoreBreadcrumb(savedInstanceState);

        return layoutBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
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
        final int primaryColor = ThemeStore.primaryColor(requireActivity());
        layoutBinding.appbar.setBackgroundColor(primaryColor);
        layoutBinding.toolbar.setBackgroundColor(primaryColor);
        layoutBinding.breadCrumbs.setBackgroundColor(primaryColor);
        layoutBinding.breadCrumbs.setActivatedContentColor(ToolbarContentTintHelper.toolbarTitleColor(requireActivity(), primaryColor));
        layoutBinding.breadCrumbs.setDeactivatedContentColor(ToolbarContentTintHelper.toolbarSubtitleColor(requireActivity(), primaryColor));
    }

    private void setUpToolbar() {
        layoutBinding.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        requireActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(layoutBinding.toolbar);
    }

    private void setUpBreadCrumbs() {
        layoutBinding.breadCrumbs.setCallback(this);
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
                getActivity(),
                layoutBinding.recyclerView,
                accentColor);

        layoutBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        layoutBinding.appbar.addOnOffsetChangedListener(this);
    }

    private void setUpAdapter() {
        adapter = new SongFileAdapter(
                getMainActivity(),
                new LinkedList<>(),
                this,
                getMainActivity());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
        layoutBinding.recyclerView.setAdapter(adapter);
        checkIsEmpty();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveScrollPosition();
    }

    @Override
    public void onDestroyView() {
        layoutBinding.appbar.removeOnOffsetChangedListener(this);
        super.onDestroyView();
    }

    @Override
    public boolean handleBackPress() {
        if (layoutBinding.breadCrumbs.popHistory()) {
            setCrumb(layoutBinding.breadCrumbs.lastHistory(), false);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_folders, menu);
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
                requireActivity(),
                layoutBinding.toolbar,
                menu,
                ATHToolbarActivity.getToolbarBackgroundColor(layoutBinding.toolbar));

        setUpSortOrderMenu(menu.findItem(R.id.action_sort_order).getSubMenu());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(requireActivity(), layoutBinding.toolbar);
    }

    static final FileFilter AUDIO_FILE_FILTER = file -> !file.isHidden() && (file.isDirectory() ||
                        FileUtil.fileIsMimeType(file, "audio/*", MimeTypeMap.getSingleton()) ||
                        FileUtil.fileIsMimeType(file, "application/opus", MimeTypeMap.getSingleton()) ||
                        FileUtil.fileIsMimeType(file, "application/ogg", MimeTypeMap.getSingleton()));

    @Override
    public void onCrumbSelection(final BreadCrumbLayout.Crumb crumb, int index) {
        setCrumb(crumb, true);
    }

    public static File getDefaultStartDirectory() {
        final File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        final File startFolder;
        if (musicDir.exists() && musicDir.isDirectory()) {
            startFolder = musicDir;
        } else {
            final File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage.exists() && externalStorage.isDirectory()) {
                startFolder = externalStorage;
            } else {
                startFolder = new File("/"); // root
            }
        }
        return startFolder;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_go_to_start_directory) {
            setCrumb(new BreadCrumbLayout.Crumb(FileUtil.safeGetCanonicalFile(PreferenceUtil.getInstance().getStartDirectory())), true);
            return true;
        } else if (itemId == R.id.action_scan) {
            final BreadCrumbLayout.Crumb crumb = getActiveCrumb();
            if (crumb != null) {
                new ListPathsAsyncTask(getActivity(), this::scanPaths)
                        .execute(new ListPathsAsyncTask.LoadingInfo(crumb.getFile(), AUDIO_FILE_FILTER));
            }
            return true;
        }
        if (handleSortOrderMenuItem(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFileSelected(final int position, @NonNull final File file) {
        final File canonicalFile = FileUtil.safeGetCanonicalFile(file); // important as we compare the path value later
        if (canonicalFile.isDirectory()) {
            setCrumb(new BreadCrumbLayout.Crumb(canonicalFile), true);
        } else {
            final FileFilter fileFilter = pathname -> !pathname.isDirectory() && AUDIO_FILE_FILTER.accept(pathname);
            new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
                int startIndex = -1;
                int size = songs.size();
                for (int i = 0; i < size; i++) {
                    if (canonicalFile.getPath().equals(songs.get(i).data)) {
                        startIndex = i;
                        break;
                    }
                }
                if (startIndex > -1) {
                    MusicPlayerRemote.enqueueSongsWithConfirmation(requireActivity(), songs, startIndex);
                } else {
                    Snackbar.make(layoutBinding.coordinatorLayout,
                                    Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store), canonicalFile.getName())),
                                    Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_scan, v -> scanPaths(new String[]{canonicalFile.getPath()}))
                            .setActionTextColor(accentColor)
                            .show();
                }
            }).execute(new ListSongsAsyncTask.LoadingInfo(position, canonicalFile.getParentFile(), fileFilter, getFileComparator()));
        }
    }

    @Override
    public void onMultipleItemAction(@NonNull final MenuItem item, @NonNull final Map<Integer, File> files) {
        final int itemId = item.getItemId();
        new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
            if (!songs.isEmpty()) {
                SongsMenuHelper.handleMenuClick(requireActivity(), songs, itemId);
            }
            if (songs.size() != files.size()) {
                Snackbar.make(layoutBinding.coordinatorLayout, R.string.some_files_are_not_listed_in_the_media_store, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_scan, v -> {
                            final int size = files.size();
                            final String[] paths = new String[size];
                            for (int i = 0; i < size; i++) {
                                paths[i] = FileUtil.safeGetCanonicalPath(files.get(i));
                            }
                            scanPaths(paths);
                        })
                        .setActionTextColor(accentColor)
                        .show();
            }
        }).execute(new ListSongsAsyncTask.LoadingInfo(files, AUDIO_FILE_FILTER, getFileComparator()));
    }

    Comparator<File> getFileComparator() {
        final SortOrder<File> fileSortOrder = FileSortOrder.fromPreference(getSortOrder());
        return fileSortOrder.comparator;
    }

    @Override
    public void onFileMenuClicked(final int position, @NonNull final File file, @NonNull final View view) {
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        if (file.isDirectory()) {
            popupMenu.inflate(R.menu.menu_item_directory);
            popupMenu.setOnMenuItemClickListener(item -> {
                final int itemId = item.getItemId();
                if (itemId == R.id.action_play_next
                        || itemId == R.id.action_add_to_current_playing
                        || itemId == R.id.action_add_to_playlist
                        || itemId == R.id.action_delete_from_device)
                {
                    new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
                        if (!songs.isEmpty()) {
                            SongsMenuHelper.handleMenuClick(requireActivity(), songs, itemId);
                        }
                    }).execute(new ListSongsAsyncTask.LoadingInfo(position, file, AUDIO_FILE_FILTER, getFileComparator()));
                    return true;
                } else if (itemId == R.id.action_set_as_start_directory) {
                    PreferenceUtil.getInstance().setStartDirectory(file);
                    SafeToast.show(requireActivity(), String.format(getString(R.string.new_start_directory), file.getPath()));
                    
                    // Rescan if whitelist enabled
                    if (PreferenceUtil.getInstance().getWhitelistEnabled()) {
                        requireContext().sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
                    }  
                  
                    return true;
                } else if (itemId == R.id.action_scan) {
                    new ListPathsAsyncTask(getActivity(), this::scanPaths)
                            .execute(new ListPathsAsyncTask.LoadingInfo(file, AUDIO_FILE_FILTER));
                    return true;
                }
                return false;
            });
        } else {
            popupMenu.inflate(R.menu.menu_item_file);
            popupMenu.setOnMenuItemClickListener(item -> {
                final int itemId = item.getItemId();
                if (itemId == R.id.action_play_next
                        || itemId == R.id.action_add_to_current_playing
                        || itemId == R.id.action_add_to_playlist
                        || itemId == R.id.action_go_to_album
                        || itemId == R.id.action_go_to_artist
                        || itemId == R.id.action_share
                        || itemId == R.id.action_tag_editor
                        || itemId == R.id.action_details
                        || itemId == R.id.action_set_as_ringtone
                        || itemId == R.id.action_delete_from_device)
                {
                    new ListSongsAsyncTask(getActivity(), null, (songs, extra) -> {
                        if (!songs.isEmpty()) {
                            SongMenuHelper.handleMenuClick(requireActivity(), songs.get(0), itemId);
                        } else {
                            Snackbar.make(layoutBinding.coordinatorLayout,
                                            Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store), file.getName())),
                                            Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_scan, v -> scanPaths(new String[]{FileUtil.safeGetCanonicalPath(file)}))
                                    .setActionTextColor(accentColor)
                                    .show();
                        }
                    }).execute(new ListSongsAsyncTask.LoadingInfo(position, file, AUDIO_FILE_FILTER, getFileComparator()));
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
    public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
        layoutBinding.container.setPadding(
                layoutBinding.container.getPaddingLeft(),
                layoutBinding.container.getPaddingTop(),
                layoutBinding.container.getPaddingRight(),
                layoutBinding.appbar.getTotalScrollRange() + verticalOffset);
    }

    void checkIsEmpty() {
        layoutBinding.empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void scanPaths(@Nullable final String[] toBeScanned) {
        final Activity activity = getActivity();
        if (activity == null) {return;}

        if (toBeScanned == null || toBeScanned.length < 1) {
            SafeToast.show(activity, R.string.nothing_to_scan);
        } else {
            MediaScannerConnection.scanFile(
                    activity,
                    toBeScanned,
                    null,
                    new UpdateToastMediaScannerCompletionListener(activity, toBeScanned)
            );
        }
    }

    private void updateAdapter(@NonNull final List<File> files) {
        adapter.swapDataSet(files);
        final BreadCrumbLayout.Crumb crumb = getActiveCrumb();
        if (crumb != null) {
            ((LinearLayoutManager) layoutBinding.recyclerView.getLayoutManager()).scrollToPositionWithOffset(crumb.getScrollPosition(), 0);
        }
    }

    public final String getSortOrder() {
        if (sortOrder == null) {
            sortOrder = loadSortOrder();
        }
        return sortOrder;
    }

    private void setSortOrder(String sortOrder) {
        reload();
    }

    private static String loadSortOrder() {return PreferenceUtil.getInstance().getFileSortOrder();}

    private void saveSortOrder(final String newSortOrder) {
        PreferenceUtil.getInstance().setFileSortOrder(newSortOrder);
    }

    private void setAndSaveSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
        saveSortOrder(sortOrder);
        setSortOrder(sortOrder);
    }

    private void setUpSortOrderMenu(@NonNull final SubMenu sortOrderMenu) {
        final String currentSortOrder = getSortOrder();
        sortOrderMenu.clear();
        FileSortOrder.buildMenu(sortOrderMenu, currentSortOrder);
        sortOrderMenu.setGroupCheckable(0, true, true);
    }

    private boolean handleSortOrderMenuItem(@NonNull final MenuItem item) {
        String sortOrderStr = null;
        final int itemId = item.getItemId();
        final SortOrder<File> sorter = FileSortOrder.fromMenuResourceId(itemId);
        if (sorter != null) {sortOrderStr = sorter.preferenceValue;}

        if (sortOrderStr != null) {
            item.setChecked(true);
            setAndSaveSortOrder(sortOrderStr);
            return true;
        }

        return false;
    }

    @Override
    @NonNull
    public Loader<List<File>> onCreateLoader(int id, final Bundle args) {
        return new AsyncFileLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<List<File>> loader, final List<File> data) {
        updateAdapter(data);
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<List<File>> loader) {
        updateAdapter(new LinkedList<>());
    }

    public void reload() {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }

    private static class AsyncFileLoader extends WrappedAsyncTaskLoader<List<File>> {
        private final WeakReference<FoldersFragment> fragmentWeakReference;

        AsyncFileLoader(@NonNull final FoldersFragment foldersFragment) {
            super(foldersFragment.getActivity());
            fragmentWeakReference = new WeakReference<>(foldersFragment);
        }

        @Override
        @NonNull
        public List<File> loadInBackground() {
            final FoldersFragment foldersFragment = fragmentWeakReference.get();
            File directory = null;
            if (foldersFragment != null) {
                final BreadCrumbLayout.Crumb crumb = foldersFragment.getActiveCrumb();
                if (crumb != null) {
                    directory = crumb.getFile();
                }
            }
            if (directory != null) {
                final List<File> files = FileUtil.listFiles(directory, AUDIO_FILE_FILTER);
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

        ListSongsAsyncTask(final Context context, final Object extra, final OnSongsListedCallback callback) {
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

        @Nullable
        @Override
        protected ArrayList<Song> doInBackground(final LoadingInfo... params) {
            try {
                final LoadingInfo info = params[0];
                final List<File> files = FileUtil.listFilesDeep(info.files.values(), info.fileFilter);

                if (isCancelled() || checkContextReference() == null || checkCallbackReference() == null)
                    return null;

                Collections.sort(files, info.fileComparator);

                final Context context = checkContextReference();
                if (isCancelled() || context == null || checkCallbackReference() == null) {
                    return null;
                }

                return FileUtil.matchFilesWithMediaStore(files);
            } catch (Exception e) {
                e.printStackTrace();
                cancel(false);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ArrayList<Song> songs) {
            super.onPostExecute(songs);
            final OnSongsListedCallback callback = checkCallbackReference();
            if (songs != null && callback != null) {
                callback.onSongsListed(songs, extra);
            }
        }

        private Context checkContextReference() {
            final Context context = contextWeakReference.get();
            if (context == null) {
                cancel(false);
            }
            return context;
        }

        private OnSongsListedCallback checkCallbackReference() {
            final OnSongsListedCallback callback = callbackWeakReference.get();
            if (callback == null) {
                cancel(false);
            }
            return callback;
        }

        public static class LoadingInfo {
            final Comparator<File> fileComparator;
            final FileFilter fileFilter;
            public final Map<Integer, File> files;

            LoadingInfo(int position, @NonNull final File file, @NonNull final FileFilter fileFilter, @NonNull final Comparator<File> fileComparator) {
                this(Map.of(position, file), fileFilter, fileComparator);
            }

            LoadingInfo(@NonNull final Map<Integer, File> files, @NonNull final FileFilter fileFilter, @NonNull final Comparator<File> fileComparator) {
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

        private static boolean scanningGuard; // avoid piling up scan jobs

        public ListPathsAsyncTask(final Context context, final OnPathsListedCallback callback) {
            super(context, 500);
            onPathsListedCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isContextStillInMemory()) {return;}
            if (!checkAndSetScanningGuard(true)) {
                cancel(false);
            }
        }

        @Override
        protected String[] doInBackground(final LoadingInfo... params) {
            try {
                if (isCancelled() || !isContextStillInMemory()) {
                    return null;
                }

                final LoadingInfo info = params[0];

                final String[] paths;

                if (info.file.isDirectory()) {
                    final List<File> files = FileUtil.listFilesDeep(info.file, info.fileFilter);

                    if (isCancelled() || !isContextStillInMemory()) {
                        return null;
                    }

                    final int size = files.size();
                    paths = new String[size];
                    for (int i = 0; i < size; i++) {
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
        protected void onCancelled(final String[] result) {
            checkAndSetScanningGuard(false);
            super.onCancelled(result);
        }

        @Override
        protected void onPostExecute(final String[] paths) {
            super.onPostExecute(paths);
            checkAndSetScanningGuard(false);
            if (onPathsListedCallback != null && paths != null) {
                onPathsListedCallback.onPathsListed(paths);
            }
        }

        private synchronized boolean checkAndSetScanningGuard(boolean value) {
            if (value && !scanningGuard) {
                scanningGuard = true;
                return true; // success
            } else if (!value) {
                scanningGuard = false;
                return true; // success
            }
            return false; // failure
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
            final FileFilter fileFilter;

            public LoadingInfo(final File file, final FileFilter fileFilter) {
                this.file = file;
                this.fileFilter = fileFilter;
            }
        }

        public interface OnPathsListedCallback {
            void onPathsListed(@NonNull String[] paths);
        }
    }

    private abstract static class ListingFilesDialogAsyncTask<Params, Progress, Result> extends DialogAsyncTask<Params, Progress, Result> {

        ListingFilesDialogAsyncTask(final Context context, int showDelay) {
            super(context, showDelay);
        }

        @Override
        protected Dialog createDialog(@NonNull final Context context) {
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
