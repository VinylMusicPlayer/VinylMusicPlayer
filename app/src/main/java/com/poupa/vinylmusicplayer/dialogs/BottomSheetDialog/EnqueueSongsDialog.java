package com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog;


import java.util.List;

import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class EnqueueSongsDialog extends BottomSheetDialogWithButtons {
    public static EnqueueSongsDialog newInstance() { return new EnqueueSongsDialog(); }

    int defaultChoice;

    public EnqueueSongsDialog setDefaultChoice(int defaultIndex) {
        this.defaultChoice = defaultIndex;
        return this;
    }

    @Override
    public EnqueueSongsDialog setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public EnqueueSongsDialog setButtonList(List<Item> buttonList) {
        super.setButtonList(buttonList);
        return this;
    }

    void afterClickOn(int i) {
        defaultChoice = i;
        PreferenceUtil.getInstance().setEnqueueSongsDefaultChoice(this.defaultChoice);
        dismiss();
    }
}
