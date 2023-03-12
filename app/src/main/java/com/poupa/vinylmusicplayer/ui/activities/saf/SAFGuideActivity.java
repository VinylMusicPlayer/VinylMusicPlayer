package com.poupa.vinylmusicplayer.ui.activities.saf;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

public class SAFGuideActivity extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);

        String title = String.format(getString(R.string.saf_guide_slide1_title), getString(R.string.app_name));

        if (Build.VERSION.SDK_INT < VERSION_CODES.R) {
            setButtonCtaVisible(false);
            setButtonNextVisible(false);
            setButtonBackVisible(false);

            addSlide(new SimpleSlide.Builder()
                    .title(title)
                    .description(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 ?
                            R.string.saf_guide_slide1_description_before_o :
                            R.string.saf_guide_slide1_description)
                    .image(R.drawable.saf_guide_1)
                    .background(R.color.md_indigo_300)
                    .backgroundDark(R.color.md_indigo_400)
                    .layout(R.layout.fragment_simple_slide_large_image)
                    .build());
            addSlide(new SimpleSlide.Builder()
                    .title(R.string.saf_guide_slide2_title)
                    .description(R.string.saf_guide_slide2_description)
                    .image(R.drawable.saf_guide_2)
                    .background(R.color.md_indigo_500)
                    .backgroundDark(R.color.md_indigo_600)
                    .layout(R.layout.fragment_simple_slide_large_image)
                    .build());
            addSlide(new SimpleSlide.Builder()
                    .title(R.string.saf_guide_slide3_title)
                    .description(R.string.saf_guide_slide3_description)
                    .image(R.drawable.saf_guide_3)
                    .background(R.color.md_indigo_700)
                    .backgroundDark(R.color.md_indigo_800)
                    .layout(R.layout.fragment_simple_slide_large_image)
                    .build());
        } else {
            setButtonCtaVisible(true);
            setButtonNextVisible(true);
            setButtonNextFunction(BUTTON_NEXT_FUNCTION_NEXT_FINISH);

            addSlide(new SimpleSlide.Builder()
                    .buttonCtaLabel("Always asked for permission")
                    .buttonCtaClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            PreferenceUtil.getInstance().setAlwaysAskWritePermission(true);
                            setResult(RESULT_OK);
                            finish();
                        }
                    })
                    .title(R.string.saf_guide_slide_title_android13)
                    .description((R.string.saf_guide_slide_description_android13))
                    .image(R.drawable.ic_baseline_lock_open)
                    .background(R.color.md_indigo_700)
                    .backgroundDark(R.color.md_indigo_800)
                    .layout(R.layout.fragment_simple_slide_large_image)
                    .build());
        }
    }
}
