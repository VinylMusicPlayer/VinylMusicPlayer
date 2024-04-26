package com.poupa.vinylmusicplayer.dialogs;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.DialogSleepTimerBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.PendingIntentCompat;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;
import com.triggertrap.seekarc.SeekArc;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SleepTimerDialog extends DialogFragment {
    SeekArc seekArc;
    private EditText timerDisplay;
    private CheckBox shouldFinishLastSong;

    int seekArcProgress;
    AlertDialog materialDialog;
    private TimerUpdater timerUpdater;
    final AtomicBoolean changingText = new AtomicBoolean(false);

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        timerUpdater.cancel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = requireActivity();
        DialogSleepTimerBinding binding = DialogSleepTimerBinding.inflate(getLayoutInflater());
        seekArc = binding.seekArc;
        timerDisplay = binding.timerDisplay;
        shouldFinishLastSong = binding.shouldFinishLastSong;

        timerUpdater = new TimerUpdater();
        materialDialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getResources().getString(R.string.action_sleep_timer))
                .setPositiveButton(R.string.action_set, (dialog, which) -> {
                    PreferenceUtil.getInstance().setSleepTimerFinishMusic(shouldFinishLastSong.isChecked());

                    final int minutes = seekArcProgress;

                    PendingIntent pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);

                    final long nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000;
                    PreferenceUtil.getInstance().setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime);
                    AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime, pi);

                    SafeToast.show(activity, activity.getResources().getString(R.string.sleep_timer_set, minutes));
                    dialog.dismiss();
                })
                .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                    final PendingIntent previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE);
                    if (previous != null) {
                        AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                        am.cancel(previous);
                        previous.cancel();
                        SafeToast.show(activity, activity.getResources().getString(R.string.sleep_timer_canceled));
                    }

                    MusicService musicService = MusicPlayerRemote.musicService;
                    if (musicService != null && musicService.pendingQuit) {
                        musicService.pendingQuit = false;
                        SafeToast.show(activity, activity.getResources().getString(R.string.sleep_timer_canceled));
                    }
                    dialog.dismiss();
                })
                .setView(binding.getRoot())
                .create();
        if (makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null) {
            timerUpdater.start();
        }

        boolean finishMusic = PreferenceUtil.getInstance().getSleepTimerFinishMusic();
        shouldFinishLastSong.setChecked(finishMusic);

        // TODO
        //seekArc.setProgressColor(ThemeSingleton.get().positiveColor.getDefaultColor());
        //seekArc.setThumbColor(ThemeSingleton.get().positiveColor.getDefaultColor());

        seekArc.post(() -> {
            int width = seekArc.getWidth();
            int height = seekArc.getHeight();
            int small = Math.min(width, height);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(seekArc.getLayoutParams());
            layoutParams.height = small;
            seekArc.setLayoutParams(layoutParams);
        });

        seekArcProgress = PreferenceUtil.getInstance().getLastSleepTimerValue();
        updateTimeDisplayTime();
        seekArc.setProgress(seekArcProgress);

        timerDisplay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (changingText.get()) {
                    return;
                }
                changingText.set(true);
                String val = s.toString();
                if (val.isEmpty()) {
                    val = "1";
                }
                seekArc.setProgress(Integer.parseInt(val));
                seekArcProgress = Integer.parseInt(val);
                PreferenceUtil.getInstance().setLastSleepTimerValue(seekArcProgress);
                changingText.set(false);
            }
        });

        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekArc seekArc, int i, boolean b) {
                if (i < 1) {
                    seekArc.setProgress(1);
                    return;
                }
                seekArcProgress = i;
                if (!changingText.get()) {
                    updateTimeDisplayTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                PreferenceUtil.getInstance().setLastSleepTimerValue(seekArcProgress);
            }
        });

        return materialDialog;
    }

    void updateTimeDisplayTime() {
        timerDisplay.setText(String.valueOf(seekArcProgress));
    }

    private PendingIntent makeTimerPendingIntent(int flag) {
        return PendingIntentCompat.getService(getActivity(), 0, makeTimerIntent(), flag);
    }

    private Intent makeTimerIntent() {
        Intent intent = new Intent(getActivity(), MusicService.class);
        if (shouldFinishLastSong.isChecked()) {
            return intent.setAction(MusicService.ACTION_PENDING_QUIT);
        }
        return intent.setAction(MusicService.ACTION_QUIT);
    }

    void updateCancelButton() {
//        TODO
//        MusicService musicService = MusicPlayerRemote.musicService;
//        if (musicService != null && musicService.pendingQuit) {
//            materialDialog.setActionButton(DialogAction.NEUTRAL, materialDialog.getContext().getString(R.string.cancel_current_timer));
//        } else {
//            materialDialog.setActionButton(DialogAction.NEUTRAL, null);
//        }
    }

    private class TimerUpdater extends CountDownTimer {
        TimerUpdater() {
            super(PreferenceUtil.getInstance().getNextSleepTimerElapsedRealTime() - SystemClock.elapsedRealtime(), 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
//            TODO
//            materialDialog.setActionButton(DialogAction.NEUTRAL, materialDialog.getContext().getString(R.string.cancel_current_timer) + " (" + MusicUtil.getReadableDurationString(millisUntilFinished) + ")");
        }

        @Override
        public void onFinish() {
            updateCancelButton();
        }
    }
}
