package com.mozhimen.scank.face.arc41.test.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.mozhimen.scank.face.arc41.test.R;

/**
 * 提示选项的图标 + 文字
 */
public class NavigateItemView extends LinearLayout {
    /**
     * 图片资源
     */
    private int imgRes;

    TextView hintTextView;
    /**
     * 额外数据，在不同场景下存放不同类型的额外信息
     */
    private Object extraData;


    public NavigateItemView(Context context, int imgRes, String tip, Object extraData) {
        this(context, imgRes, tip, null);
        this.extraData = extraData;
    }

    public NavigateItemView(Context context, int imgRes, String tip, String tipHint, Object extraData) {
        this(context, imgRes, tip, tipHint);
        this.extraData = extraData;
    }

    public NavigateItemView(Context context, int imgRes, String tip) {
        this(context, imgRes, tip, null);
    }

    public NavigateItemView(Context context, int imgRes, String tip, String tipHint) {
        super(context);

        this.imgRes = imgRes;

        // item size
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.navigate_item_height);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        setOrientation(LinearLayout.HORIZONTAL);

        // img label
        AppCompatImageView imageView = new AppCompatImageView(context);
        int imgViewMargin = getResources().getDimensionPixelSize(R.dimen.label_margin);
        int size = itemHeight - imgViewMargin * 2;
        LayoutParams imgViewLayoutParams = new LayoutParams(size, size);
        imgViewLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        imgViewLayoutParams.setMargins(imgViewMargin, imgViewMargin, imgViewMargin, imgViewMargin);
        imageView.setLayoutParams(imgViewLayoutParams);

        imageView.setAdjustViewBounds(true);
        addView(imageView);
        imageView.setImageResource(imgRes);

        if (tipHint == null) {
            // 仅标题
            TextView textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.navigate_item_title_text_size));
            LayoutParams tipTextParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
            int tipsMargin = getResources().getDimensionPixelSize(R.dimen.tips_horizontal_margin);
            tipTextParams.setMargins(tipsMargin, 0, 0, 0);
            textView.setLayoutParams(tipTextParams);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            addView(textView);
            textView.setText(tip);
        } else {
            LayoutParams subLinearLayout = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
            LinearLayout textLabelLayout = new LinearLayout(context);
            textLabelLayout.setLayoutParams(subLinearLayout);
            textLabelLayout.setOrientation(LinearLayout.VERTICAL);

            // 标题
            TextView tipTextView = new TextView(context);
            tipTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.navigate_item_title_text_size));
            LayoutParams tipTextParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight / 2);
            int tipsMargin = getResources().getDimensionPixelSize(R.dimen.tips_horizontal_margin);
            tipTextParams.setMargins(tipsMargin, 0, 0, 0);
            tipTextView.setLayoutParams(tipTextParams);
            tipTextView.setGravity(Gravity.CENTER_VERTICAL);
            tipTextView.setTypeface(Typeface.DEFAULT_BOLD);

            tipTextView.setText(tip);

            // 提示语
            hintTextView = new TextView(context);
            hintTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.navigate_item_title_text_size));
            hintTextView.setLayoutParams(tipTextParams);
            hintTextView.setGravity(Gravity.CENTER_VERTICAL);
            hintTextView.setText(tipHint);

            textLabelLayout.addView(tipTextView);
            textLabelLayout.addView(hintTextView);
            addView(textLabelLayout);


        }

        // 点击效果
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        if (typedArray.getIndexCount() > 0) {
            Drawable drawable = typedArray.getDrawable(0);
            setBackground(drawable);
        }
        typedArray.recycle();

    }

    /**
     * 图像资源Id，可用于 switch-case
     *
     * @return 图像资源Id
     */
    public int getImgRes() {
        return imgRes;
    }


    public Object getExtraData() {
        return extraData;
    }

    public void setExtraData(Object extraData) {
        this.extraData = extraData;
    }

    /**
     * 修改提示信息
     *
     * @param content 提示信息内容
     */
    public void changeTipHint(String content) {
        if (hintTextView != null) {
            hintTextView.setText(content);
            return;
        }
        throw new NullPointerException("hint not exists!");
    }

}
