package com.harrydmorgan.shoppinglist.reminder;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;


public class TimePicker extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private final Calendar c;

    TimePicker(Calendar c) {
        this.c = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onTimeSet(android.widget.TimePicker timePicker, int hour, int min) {
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, min);
        Calendar now = Calendar.getInstance();
        if (now.getTimeInMillis() > c.getTimeInMillis()) {
            Toast.makeText(getContext(), "Invalid time", Toast.LENGTH_SHORT).show();
            new TimePicker(c).show(getParentFragmentManager(), "timePicker");
            return;
        }

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Schedule notification
        AlarmManager manager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
    }
}
