package com.poupa.vinylmusicplayer.ui.activities.bugreport;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivityBugReportBinding;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.DeviceInfo;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.Report;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.github.ExtraInfo;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.github.GithubLogin;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.model.github.GithubTarget;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BugReportActivity extends AbsThemeActivity {
    private static final int STATUS_BAD_CREDENTIALS = 401;
    private static final int STATUS_ISSUES_NOT_ENABLED = 410;

    @StringDef({RESULT_OK, RESULT_BAD_CREDENTIALS, RESULT_INVALID_TOKEN, RESULT_ISSUES_NOT_ENABLED,
            RESULT_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Result {
    }

    private static final String RESULT_OK = "RESULT_OK";
    private static final String RESULT_BAD_CREDENTIALS = "RESULT_BAD_CREDENTIALS";
    private static final String RESULT_INVALID_TOKEN = "RESULT_INVALID_TOKEN";
    private static final String RESULT_ISSUES_NOT_ENABLED = "RESULT_ISSUES_NOT_ENABLED";
    private static final String RESULT_UNKNOWN = "RESULT_UNKNOWN";

    private Toolbar toolbar;

    private TextInputLayout inputLayoutTitle;
    private TextInputEditText inputTitle;
    private TextInputLayout inputLayoutDescription;
    private TextInputEditText inputDescription;
    private TextView textDeviceInfo;

    private TextInputLayout inputLayoutUsername;
    private TextInputEditText inputUsername;
    private TextInputLayout inputLayoutPassword;
    private TextInputEditText inputPassword;
    private RadioButton optionUseAccount;
    private RadioButton optionManual;

    private FloatingActionButton sendFab;

    private static final String ISSUE_TRACKER_LINK = "https://github.com/vinyl2-team/vinyl2/issues";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityBugReportBinding binding = ActivityBugReportBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        toolbar = binding.toolbar;
        sendFab = binding.buttonSend;

        textDeviceInfo = binding.bugReportCardDeviceInfo.airTextDeviceInfo;

        inputLayoutTitle = binding.bugReportCardReport.inputLayoutTitle;
        inputTitle = binding.bugReportCardReport.inputTitle;
        inputLayoutDescription = binding.bugReportCardReport.inputLayoutDescription;
        inputDescription = binding.bugReportCardReport.inputDescription;
        inputLayoutUsername = binding.bugReportCardReport.inputLayoutUsername;
        inputUsername = binding.bugReportCardReport.inputUsername;
        inputLayoutPassword = binding.bugReportCardReport.inputLayoutPassword;
        inputPassword = binding.bugReportCardReport.inputPassword;
        optionUseAccount = binding.bugReportCardReport.optionUseAccount;
        optionManual = binding.bugReportCardReport.optionAnonymous;

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        initViews();

        if (TextUtils.isEmpty(getTitle()))
            setTitle(R.string.report_an_issue);

        final String subject = getIntent().getStringExtra(Intent.EXTRA_SUBJECT);
        if (!TextUtils.isEmpty(subject)) {
            inputTitle.setText(subject);
        }

        final DeviceInfo deviceInfo = new DeviceInfo(this);
        final String extraInfo = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        textDeviceInfo.setText(deviceInfo + (extraInfo != null ? ("\n\n" + extraInfo) : ""));
    }

    private void initViews() {
        final int accentColor = ThemeStore.accentColor(this);
        final int primaryColor = ThemeStore.primaryColor(this);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TintHelper.setTintAuto(optionUseAccount, accentColor, false);
        optionUseAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                inputTitle.setEnabled(true);
                inputDescription.setEnabled(true);
                inputUsername.setEnabled(true);
                inputPassword.setEnabled(true);

                optionManual.setChecked(false);
                sendFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(final FloatingActionButton fab) {
                        super.onHidden(fab);
                        sendFab.setImageResource(R.drawable.ic_send_white_24dp);
                        sendFab.show();
                    }
                });
            }
        });
        TintHelper.setTintAuto(optionManual, accentColor, false);
        optionManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                inputTitle.setEnabled(false);
                inputDescription.setEnabled(false);
                inputUsername.setEnabled(false);
                inputPassword.setEnabled(false);

                optionUseAccount.setChecked(false);
                sendFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(final FloatingActionButton fab) {
                        super.onHidden(fab);
                        sendFab.setImageResource(R.drawable.ic_open_in_browser_white_24dp);
                        sendFab.show();
                    }
                });
            }
        });

        inputPassword.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                reportIssue();
                return true;
            }
            return false;
        });

        textDeviceInfo.setOnClickListener(v -> copyDeviceInfoToClipBoard());

        TintHelper.setTintAuto(sendFab, accentColor, true);
        sendFab.setOnClickListener(v -> reportIssue());

        TintHelper.setTintAuto(inputTitle, accentColor, false);
        TintHelper.setTintAuto(inputDescription, accentColor, false);
        TintHelper.setTintAuto(inputUsername, accentColor, false);
        TintHelper.setTintAuto(inputPassword, accentColor, false);
    }

    private void reportIssue() {
        if (optionUseAccount.isChecked()) {
            if (!validateInput()) return;
            final String username = inputUsername.getText().toString();
            final String password = inputPassword.getText().toString();
            sendBugReport(new GithubLogin(username, password));
        } else {
            copyDeviceInfoToClipBoard();

            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(ISSUE_TRACKER_LINK));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    private void copyDeviceInfoToClipBoard() {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(getString(R.string.device_info), textDeviceInfo.getText());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.copied_device_info_to_clipboard, Toast.LENGTH_LONG).show();
    }

    private boolean validateInput() {
        boolean hasErrors = false;

        if (optionUseAccount.isChecked()) {
            if (TextUtils.isEmpty(inputUsername.getText())) {
                setError(inputLayoutUsername, R.string.bug_report_no_username);
                hasErrors = true;
            } else {
                removeError(inputLayoutUsername);
            }

            if (TextUtils.isEmpty(inputPassword.getText())) {
                setError(inputLayoutPassword, R.string.bug_report_no_password);
                hasErrors = true;
            } else {
                removeError(inputLayoutPassword);
            }
        }

        if (TextUtils.isEmpty(inputTitle.getText())) {
            setError(inputLayoutTitle, R.string.bug_report_no_title);
            hasErrors = true;
        } else {
            removeError(inputLayoutTitle);
        }

        if (TextUtils.isEmpty(inputDescription.getText())) {
            setError(inputLayoutDescription, R.string.bug_report_no_description);
            hasErrors = true;
        } else {
            removeError(inputLayoutDescription);
        }

        return !hasErrors;
    }

    private void setError(final TextInputLayout editTextLayout, @StringRes final int errorRes) {
        editTextLayout.setError(getString(errorRes));
    }

    private void removeError(TextInputLayout editTextLayout) {
        editTextLayout.setError(null);
    }

    private void sendBugReport(final GithubLogin login) {
        if (!validateInput()) return;

        final String bugTitle = inputTitle.getText().toString();
        final String bugDescription = inputDescription.getText().toString();

        final Report report = new Report(bugTitle, bugDescription, textDeviceInfo.getText(), new ExtraInfo());
        final GithubTarget target = new GithubTarget("vinyl2-team", "vinyl2");

        ReportIssueAsyncTask.report(this, report, target, login);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ReportIssueAsyncTask extends DialogAsyncTask<Void, Void, String> {
        private final Report report;
        private final GithubTarget target;
        private final GithubLogin login;

        static void report(final Activity activity, final Report report, final GithubTarget target,
                           final GithubLogin login) {
            new ReportIssueAsyncTask(activity, report, target, login).execute();
        }

        private ReportIssueAsyncTask(final Activity activity, final Report report, final GithubTarget target,
                                     final GithubLogin login) {
            super(activity);
            this.report = report;
            this.target = target;
            this.login = login;
        }

        @Override
        protected Dialog createDialog(@NonNull final Context context) {
            return new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .title(R.string.bug_report_uploading)
                    .show();
        }

        @Override
        @Result
        protected String doInBackground(final Void... params) {
            final GitHubClient client;
            if (login.shouldUseApiToken()) {
                client = new GitHubClient().setOAuth2Token(login.getApiToken());
            } else {
                client = new GitHubClient().setCredentials(login.getUsername(), login.getPassword());
            }

            final Issue issue = new Issue().setTitle(report.getTitle()).setBody(report.getDescription());
            try {
                new IssueService(client).createIssue(target.getUsername(), target.getRepository(), issue);
                return RESULT_OK;
            } catch (final RequestException e) {
                switch (e.getStatus()) {
                    case STATUS_BAD_CREDENTIALS:
                        if (login.shouldUseApiToken())
                            return RESULT_INVALID_TOKEN;
                        return RESULT_BAD_CREDENTIALS;
                    case STATUS_ISSUES_NOT_ENABLED:
                        return RESULT_ISSUES_NOT_ENABLED;
                    default:
                        e.printStackTrace();
                        return RESULT_UNKNOWN;
                }
            } catch (final IOException e) {
                e.printStackTrace();
                return RESULT_UNKNOWN;
            }
        }

        @Override
        protected void onPostExecute(@Result final String result) {
            super.onPostExecute(result);

            final Context context = getContext();
            if (context == null) return;

            switch (result) {
                case RESULT_OK:
                    tryToFinishActivity();
                    break;
                case RESULT_BAD_CREDENTIALS:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_wrong_credentials)
                            .positiveText(android.R.string.ok)
                            .show();
                    break;
                case RESULT_INVALID_TOKEN:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_invalid_token)
                            .positiveText(android.R.string.ok)
                            .show();
                    break;
                case RESULT_ISSUES_NOT_ENABLED:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_issues_not_available)
                            .positiveText(android.R.string.ok)
                            .show();
                    break;
                default:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_unknown)
                            .positiveText(android.R.string.ok)
                            .onPositive((dialog, which) -> tryToFinishActivity())
                            .cancelListener(dialog -> tryToFinishActivity())
                            .show();
                    break;
            }
        }

        private void tryToFinishActivity() {
            Context context = getContext();
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                ((Activity) context).finish();
            }
        }
    }
}
