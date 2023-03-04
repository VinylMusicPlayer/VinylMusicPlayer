package com.poupa.vinylmusicplayer.preferences;


import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog.BottomSheetDialogWithButtons;
import com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog.BottomSheetDialogWithButtons.ButtonInfo;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class SongConfirmationPreference extends ATEDialogPreference {
   public static BottomSheetDialogWithButtons.ButtonInfo ASK = new ButtonInfo(PreferenceUtil.ENQUEUE_SONGS_CHOICE_ASK, R.string.action_always_ask_for_confirmation, R.drawable.ic_close_white_24dp, null);
   public static BottomSheetDialogWithButtons.ButtonInfo REPLACE = new ButtonInfo(PreferenceUtil.ENQUEUE_SONGS_CHOICE_REPLACE, R.string.action_play, R.drawable.ic_play_arrow_white_24dp, null);
   public static BottomSheetDialogWithButtons.ButtonInfo NEXT = new ButtonInfo(PreferenceUtil.ENQUEUE_SONGS_CHOICE_NEXT, R.string.action_play_next, R.drawable.ic_redo_white_24dp, null);
   public static BottomSheetDialogWithButtons.ButtonInfo ADD = new ButtonInfo(PreferenceUtil.ENQUEUE_SONGS_CHOICE_ADD, R.string.action_add_to_playing_queue, R.drawable.ic_library_add_white_24dp, null);

   public static final List<ButtonInfo> possibleActions = Arrays.asList(ASK, REPLACE, NEXT, ADD);

   public SongConfirmationPreference(Context context) {
      super(context);
   }

   public SongConfirmationPreference(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public SongConfirmationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
   }

   public SongConfirmationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
   }
}
