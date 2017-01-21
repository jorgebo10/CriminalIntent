package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.f2prateek.dart.Dart;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DatePickerFragment extends DialogFragment {
    static final String EXTRA_DATE = "extra_date";

    @BindString(R.string.date_picker_title)
    String datePickerTitle;
    @BindString(android.R.string.ok)
    String positiveButtonLabel;
    @BindView(R.id.dialog_date_date_picker)
    DatePicker datePicker;

    public static DatePickerFragment newInstance(final Date date) {
        final Bundle args = new Bundle();
        args.putSerializable(EXTRA_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final ViewGroup parent = null;
        final View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_date, parent);

        ButterKnife.bind(this, v);

        final Date date = Dart.get(getArguments(), EXTRA_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(datePickerTitle)
                .setPositiveButton(positiveButtonLabel, (dialogInterface, i) -> {
                    final Date date1 = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()).getTime();
                    if (getTargetFragment() == null) {
                        return;
                    }

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_DATE, date1);

                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                })
                .create();
    }
}
