package com.mozhimen.scank.face.arc42.test.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VerticalAlignTextSpan extends ReplacementSpan {
    private int fontSizeSp = -1;//单位:sp

    public VerticalAlignTextSpan() {
    }

    public VerticalAlignTextSpan(int fontSizeSp) {
        this.fontSizeSp = fontSizeSp;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        Paint newPaint = getCustomTextPaint(paint);
        return (int) newPaint.measureText(text, start, end);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int
            bottom, @NonNull Paint paint) {
        Paint newPaint = getCustomTextPaint(paint);
        Paint.FontMetricsInt fontMetricsInt = newPaint.getFontMetricsInt();
        int offsetY = (y + fontMetricsInt.ascent + y + fontMetricsInt.descent) / 2 - (top + bottom) / 2;
        canvas.drawText(text, start, end, x, y - offsetY, newPaint);
    }

    private TextPaint getCustomTextPaint(Paint srcPaint) {
        TextPaint textPaint = new TextPaint(srcPaint);
        if (fontSizeSp != -1) {//-1没有重设fontSize
            textPaint.setTextSize(fontSizeSp * textPaint.density);//sp转px
        }
        return textPaint;
    }
}