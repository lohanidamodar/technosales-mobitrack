package com.technosales.mobitrack;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TogglePreference extends Preference {
    private CharSequence mText;

    public TogglePreference(Context context) {
        super(context);
    }

    public TogglePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TogglePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new LinearLayout(getContext());
            ((LinearLayout) convertView)
                    .setOrientation(LinearLayout.HORIZONTAL);

            TextView txtInfo = new TextView(getContext());

            txtInfo.setText("Title");
            ((LinearLayout) convertView).addView(txtInfo,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            ToggleButton btn = new ToggleButton(getContext());
            ((LinearLayout) convertView).addView(btn);
        }

        return convertView;
    }

    public void setText(CharSequence text) {
        this.mText = text;
        notifyChanged();
    }
}