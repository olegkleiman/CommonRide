package com.labs.okey.commonride.pickers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by c1306948 on 25/01/2015.
 */
public class TimePickerFragment extends DialogFragment{

    private TimePickerDialog.OnTimeSetListener listener;

    public TimePickerFragment() {
        super();
    }

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar selectedTime = Calendar.getInstance();
        int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedTime.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), listener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

}
