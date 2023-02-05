package com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog;


import java.util.List;

import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class EnqueueSongsBottomSheetDialog extends ButtonListBottomSheetDialog {
    public static EnqueueSongsBottomSheetDialog newInstance() { return new EnqueueSongsBottomSheetDialog(); }

    int defaultChoice;

    public EnqueueSongsBottomSheetDialog setDefaultChoice(int defaultIndex) {
        this.defaultChoice = defaultIndex;
        return this;
    }

    @Override
    public EnqueueSongsBottomSheetDialog setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public EnqueueSongsBottomSheetDialog setButtonList(List<Item> buttonList) {
        super.setButtonList(buttonList);
        return this;
    }

    void afterClickOn(int i) {
        defaultChoice = i;
        PreferenceUtil.getInstance().setEnqueueSongsDefaultChoice(this.defaultChoice);
        dismiss();
    }
}
