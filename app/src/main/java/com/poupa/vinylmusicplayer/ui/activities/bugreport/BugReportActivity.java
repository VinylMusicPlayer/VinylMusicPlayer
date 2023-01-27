package com.poupa.vinylmusicplayer.ui.activities.bugreport;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivityBugReportBinding;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.DeviceInfo;

public class BugReportActivity extends AbsThemeActivity {
    private Toolbar toolbar;

    private TextView textDeviceInfo;

    private FloatingActionButton sendFab;

    private static final String ISSUE_TRACKER_LINK = "https://github.com/vinyl2-team/vinyl2/issues/new?assignees=&labels=bug&template=bug_report.md&title=";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityBugReportBinding binding = ActivityBugReportBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        toolbar = binding.toolbar;
        sendFab = binding.buttonSend;

        textDeviceInfo = binding.bugReportCardDeviceInfo.airTextDeviceInfo;

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        initViews();

        if (TextUtils.isEmpty(getTitle()))
            setTitle(R.string.report_an_issue);

        final DeviceInfo deviceInfo = new DeviceInfo(this);
        final String extraInfo = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        textDeviceInfo.setText(deviceInfo + (extraInfo != null ? ("\n\n" + extraInfo) : ""));
    }

    private void initViews() {
        final int accentColor = ThemeStore.accentColor(this);
        final int primaryColor = ThemeStore.primaryColor(this);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textDeviceInfo.setOnClickListener(v -> copyDeviceInfoToClipBoard());

        TintHelper.setTintAuto(sendFab, accentColor, true);
        sendFab.setOnClickListener(v -> reportIssue());
    }

    private void reportIssue() {
        copyDeviceInfoToClipBoard();

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ISSUE_TRACKER_LINK));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void copyDeviceInfoToClipBoard() {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(getString(R.string.device_info), textDeviceInfo.getText());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.copied_device_info_to_clipboard, Toast.LENGTH_LONG).show();
    }
}
