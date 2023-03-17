package com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;


public abstract class BottomSheetDialog extends BottomSheetDialogFragment {
    public static BottomSheetDialog newInstance() { return null; }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        com.google.android.material.bottomsheet.BottomSheetDialog
                dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireActivity(), ThemeStyleUtil.getInstance().getBottomSheetStyle());

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                com.google.android.material.bottomsheet.BottomSheetDialog
                        d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
                behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behaviour.setDraggable(false);
                behaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });

        return dialog;
    }

}
