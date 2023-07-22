package com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.Util;

import java.util.concurrent.CopyOnWriteArrayList;


public abstract class BottomSheetDialog extends BottomSheetDialogFragment {
    public static BottomSheetDialog newInstance() { return null; }

    /**
     * Not null after {@link #onCreateDialog(Bundle)} was called. <br>
     * You can add a listener to {@link #onBottomSheetCreated}.
     */
    public FrameLayout bottomSheet = null;

    public CopyOnWriteArrayList<Runnable> onBottomSheetCreated = new CopyOnWriteArrayList<>();

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        com.google.android.material.bottomsheet.BottomSheetDialog
                dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireActivity(), ThemeStyleUtil.getInstance().getBottomSheetStyle());

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                com.google.android.material.bottomsheet.BottomSheetDialog
                        d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;
                bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
                behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behaviour.setDraggable(false);

                for (Runnable code : onBottomSheetCreated) {
                    code.run();
                }
            }
        });

        return dialog;
    }

    public void expand(){
        Runnable code = () -> {
            BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
            if(behaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) return;
            behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        };
        if(bottomSheet != null) code.run();
        else onBottomSheetCreated.add(code);
    }

    public void collapse(){
        Runnable code = () -> {
            BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
            if(behaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) return;
            behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        };
        if(bottomSheet != null) code.run();
        else onBottomSheetCreated.add(code);
    }

}
