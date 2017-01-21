package com.bignerdranch.android.criminalintent;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;

import static com.bignerdranch.android.criminalintent.CrimeLab.DATE;
import static com.bignerdranch.android.criminalintent.CrimeLab.SOLVED;
import static com.bignerdranch.android.criminalintent.CrimeLab.SUSPECT;
import static com.bignerdranch.android.criminalintent.CrimeLab.TITLE;
import static com.bignerdranch.android.criminalintent.CrimeLab.UUID;

 class CrimeCursorWrapper extends CursorWrapper {

     CrimeCursorWrapper(final Cursor cursor) {
        super(cursor);
    }

     Crime getCrime() {
        String uuidString = getString(getColumnIndex(UUID));
        String title = getString(getColumnIndex(TITLE));
        long date = getLong(getColumnIndex(DATE));
        int isSolved = getInt(getColumnIndex(SOLVED));
        String suspect = getString(getColumnIndex(SUSPECT));

        final Crime crime = new Crime(java.util.UUID.fromString(uuidString));
        crime.setmTitle(title);
        crime.setmDate(new Date(date));
        crime.setmSolved(isSolved != 0);
        crime.setmSuspect(suspect);
        return crime;
    }
}
