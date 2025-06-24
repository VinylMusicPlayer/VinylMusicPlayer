package com.poupa.vinylmusicplayer.ui.activities.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.SafeToast;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsBaseActivity extends AbsThemeActivity {
    public static final int PERMISSION_REQUEST = 100;

    private boolean hadPermissions;
    private String[] permissions;
    private String permissionDeniedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        permissions = getPermissionsToRequest();
        hadPermissions = hasPermissions();

        setPermissionDeniedMessage(null);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!hasPermissions()) {
            requestPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final boolean hasPermissions = hasPermissions();
        if (hasPermissions != hadPermissions) {
            hadPermissions = hasPermissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                onHasPermissionsChanged(hasPermissions);
            }
        }
    }

    protected void onHasPermissionsChanged(boolean hasPermissions) {
        // implemented by sub classes
    }

    @Nullable
    protected String[] getPermissionsToRequest() {
        return null;
    }

    public View getSnackBarContainer() {
        View retVal =  getWindow().getDecorView();
        final View bottomBar = getWindow().findViewById(R.id.sliding_panel);
        // avoid the bottom bar if the view has it, by sticking to the top of it
        return bottomBar == null ? retVal : bottomBar;
    }

    protected void setPermissionDeniedMessage(String message) {
        permissionDeniedMessage = message;
    }

    private String getPermissionDeniedMessage() {
        return permissionDeniedMessage == null ? getString(R.string.permissions_denied) : permissionDeniedMessage;
    }

    protected void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            requestPermissions(permissions, PERMISSION_REQUEST);
        }
    }

    protected boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            for (final int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    final String message = getPermissionDeniedMessage();
                    try {
                        final View container = getSnackBarContainer();
                        final Snackbar snackbar = Snackbar.make(container, message, BaseTransientBottomBar.LENGTH_INDEFINITE);

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            //User has denied from permission dialog
                            snackbar.setAction(R.string.action_grant, view -> requestPermissions())
                                    .setActionTextColor(ThemeStore.accentColor(this))
                                    .show();
                        } else {
                            // User has denied permission and checked never show permission dialog so you can redirect to Application settings page
                            snackbar.setAction(R.string.action_settings, view -> {
                                        final Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        final Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    })
                                    .setActionTextColor(ThemeStore.accentColor(this))
                                    .show();
                        }

                        return;
                    } catch (final IllegalArgumentException ignored) {
                        // Issue #908: No suitable parent found from the given view. Please provide a valid view.
                        // The error is thrown from here: Snackbar.findSuitableParent

                        // Possible cause: The UI state has changed (i.e. user navigated to another view) and not
                        // usable anymore for Snackbar implementation
                        SafeToast.show(this, message);
                    }
                }
            }
            hadPermissions = true;
            onHasPermissionsChanged(true);
        }
    }
}
