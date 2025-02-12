package com.mozhimen.scank.face.arc41.test.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;


/**
 * A {@link DialogPreference} that shows a {@link EditText} in the dialog.
 *
 * <p>This preference saves a string value.
 */
public class ThresholdLivePreference extends EditTextPreference {

    public ThresholdLivePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThresholdLivePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThresholdLivePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThresholdLivePreference(Context context) {
        super(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getFloat(index,0);
    }


}
