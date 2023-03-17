package com.poupa.vinylmusicplayer.dialogs.BottomSheetDialog;

import android.content.Context;
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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;

import java.util.List;


public class BottomSheetDialogWithButtons extends BottomSheetDialog {
    public static BottomSheetDialogWithButtons newInstance() { return new BottomSheetDialogWithButtons(); }

    String title;
    List<ButtonInfo> buttonList;
    int defaultIndex = -1;

    public BottomSheetDialogWithButtons setTitle(String title) {
        this.title = title;
        return this;
    }
    public BottomSheetDialogWithButtons setButtonList(List<ButtonInfo> buttonList) {
        this.buttonList = buttonList;
        return this;
    }

    public BottomSheetDialogWithButtons setDefaultButtonIndex(int defaultIndex) {
        this.defaultIndex = defaultIndex;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_button_list, container, false);
        Context context = getContext();

        if (buttonList == null || context == null) { dismiss(); return view; }

        TextView titleView = view.findViewById(R.id.title);
        titleView.setText(title);

        for (int i = 0; i < buttonList.size(); i++) {
            final Button button;

            button = new Button(context, null);

            button.setTag(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = (Integer)v.getTag();
                    buttonList.get(i).action.run();
                    afterClickOn(i);
                }
            });

            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
            button.setBackgroundResource(outValue.resourceId);
            int px_vertical = context.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_vertical_divided_margin);
            int px_horizontal = context.getResources().getDimensionPixelSize(R.dimen.default_item_margin);
            button.setPadding(px_horizontal, px_vertical, px_horizontal, px_vertical);

            LinearLayout linearlayout = (LinearLayout) view.findViewById(R.id.buttonList);
            linearlayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.setMargins(0, 0, 0, 0);

            int colorPrimary = ThemeStore.textColorPrimary(getActivity());
            int accentColor = ThemeStore.accentColor(getActivity());
            button.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            button.setTypeface(null, Typeface.NORMAL);
            button.setTransformationMethod(null);
            if (i == defaultIndex) {
                button.setTextColor(accentColor);
            } else {
                button.setTextColor(colorPrimary);
            }
            button.setText(context.getString(buttonList.get(i).titleId));
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

            if (buttonList.get(i).iconId != null) {
                Drawable icon = ContextCompat.getDrawable(context, buttonList.get(i).iconId);
                // TODO Decorate the default item (ex: bold text)
                //if (i == defaultIndex) {
                //    DrawableCompat.setTint(icon, accentColor);
                //    button.getText(context.getString(buttonList.get(i).titleId));
                //} else {
                //    DrawableCompat.setTint(icon, colorPrimary);
                //}
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
        public int id;
        @StringRes
        public int titleId;
        @DrawableRes
        public Integer iconId;
        public Runnable action;

        public ButtonInfo(int id, int titleId, Runnable action) {
            this.id = id;
            this.titleId = titleId;
            this.action = action;
            this.iconId = null;
        }

        public ButtonInfo(int id, int titleId, int iconId, Runnable action) {
            this.id = id;
            this.titleId = titleId;
            this.action = action;
            this.iconId = iconId;
        }

        public ButtonInfo setAction(Runnable action) {
            this.action = action;
            return this;
        }
    }

}