package com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog;


import java.util.List;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;


public class BottomSheetDialogWithButtons extends BottomSheetDialog {
    public static BottomSheetDialogWithButtons newInstance() { return new BottomSheetDialogWithButtons(); }

    String title;
    List<ButtonInfo> buttonList;

    public BottomSheetDialogWithButtons setTitle(String title) {
        this.title = title;
        return this;
    }
    public BottomSheetDialogWithButtons setButtonList(List<ButtonInfo> buttonList) {
        this.buttonList = buttonList;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_button_list, container, false);

        TextView titleView = view.findViewById(R.id.title);
        titleView.setText(title);

        for (int i = 0; i < buttonList.size(); i++) {
            final Button button;

            button = new Button(view.getContext(), null);
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
            button.setBackgroundResource(outValue.resourceId);
            int px_vertical = getContext().getResources().getDimensionPixelSize(R.dimen.default_item_padding);
            int px_horizontal = getContext().getResources().getDimensionPixelSize(R.dimen.default_item_margin);
            button.setPadding(px_horizontal, px_vertical, px_horizontal, px_vertical);

            button.setText(buttonList.get(i).title);
            // TODO: make color the undo one, for know this is not working
            button.setTextColor(ThemeStore.textColorSecondary(getActivity())); //PlayingQueueAdapter.getBackgroundColor((AppCompatActivity)getActivity()));

            button.setTag(i);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = (Integer)v.getTag();
                    buttonList.get(i).action.run();
                    afterClickOn(i);
                }
            });

            LinearLayout linearlayout = (LinearLayout) view.findViewById(R.id.buttonList);
            linearlayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.setMargins(0, 0, 0, 0);

            int colorPrimary = ThemeStore.textColorPrimary(getActivity());
            button.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            button.setTypeface(null, Typeface.NORMAL);
            button.setTransformationMethod(null);
            button.setTextColor(colorPrimary);

            Drawable icon = buttonList.get(i).icon;
            if (icon != null) {
                DrawableCompat.setTint(icon, colorPrimary);
                button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                button.setCompoundDrawablePadding((int)(1.5*px_horizontal));
            }
            linearlayout.addView(button, buttonParams);
        }

        return view;
    }

    void afterClickOn(int i) {
        dismiss();
    }

    public static class ButtonInfo {
        public String title;
        public Runnable action;
        public Drawable icon;

        public ButtonInfo(String title, Runnable action) {
            this.title = title;
            this.action = action;
            this.icon = null;
        }

        public ButtonInfo(String title, Runnable action, Drawable icon) {
            this.title = title;
            this.action = action;
            this.icon = icon;
        }
    }

}