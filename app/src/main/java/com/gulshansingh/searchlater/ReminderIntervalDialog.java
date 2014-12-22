package com.gulshansingh.searchlater;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

public class ReminderIntervalDialog extends DialogPreference {
    private NumberPicker mTimeAmount;
    private Spinner mTimeUnit;

    public ReminderIntervalDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(true);
        setDialogLayoutResource(R.layout.reminder_interval_dialog);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SharedPreferences sharedPreferences = getSharedPreferences();
        mTimeAmount = (NumberPicker) view.findViewById(R.id.time_amount);
        mTimeUnit = (Spinner) view.findViewById(R.id.time_unit);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeUnit.setAdapter(adapter);
        String timeUnit = sharedPreferences.getString("time_unit", "Days");
        mTimeUnit.setSelection(adapter.getPosition(timeUnit));
        mTimeUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setTimeAmountRange(mTimeAmount.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int timeAmount = sharedPreferences.getInt("time_amount", 1);
        setTimeAmountRange(timeAmount);
    }

    private void setTimeAmountRange(int timeAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String timeUnit = mTimeUnit.getSelectedItem().toString();

        int maxValue = 0;
        if (timeUnit.equals("Minutes")) {
            maxValue = 60;
        } else if (timeUnit.equals("Hours")) {
            maxValue = 24;
        } else if (timeUnit.equals("Days")) {
            maxValue = 30;
        } else {
            throw new RuntimeException("Invalid time unit");
        }

        mTimeAmount.setMinValue(1);
        mTimeAmount.setMaxValue(maxValue);
        mTimeAmount.setValue(Math.min(timeAmount, maxValue));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SharedPreferences.Editor editor = getEditor();
            editor.putString("time_unit", mTimeUnit.getSelectedItem().toString());
            editor.putInt("time_amount", mTimeAmount.getValue());
            editor.commit();

            // Trick the SettingsActivity to update the summary
            int curVal = getPersistedInt(0);
            if (curVal == 0) {
                persistInt(1);
            } else {
                persistInt(0);
            }
        }
    }
}
