package com.labs.okey.commonride.pickers;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by c1306948 on 25/01/2015.
 */
public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;

    public DatePickerFragment() {
        super();
    }

    public DatePickerFragment(DatePickerDialog.OnDateSetListener onTimeSetListener) {
        super();
        listener = onTimeSetListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar selectedDate= Calendar.getInstance();
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), listener, year, month, day);
    }

}
