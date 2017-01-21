package com.bignerdranch.android.criminalintent;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class CrimeLab {
    static final String UUID = "uuid";
    static final String TITLE = "title";
    static final String DATE = "date";
    static final String SOLVED = "solved";
    static final String SUSPECT = "suspect";
    private static final String CRIMES = "crimes";
    private static CrimeLab sCrimeLab;

    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context) {
        final Context mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

        final Flyway flyway = new Flyway();
        ContextHolder.setContext(mContext);
        flyway.setDataSource("jdbc:sqlite:" + mDatabase.getPath(), "", "");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
    }

    static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private static ContentValues getContentValues(final Crime crime) {
        ContentValues values = new ContentValues();
        values.put(UUID, crime.getmId().toString());
        values.put(TITLE, crime.getmTitle());
        values.put(DATE, crime.getmDate().getTime());
        values.put(SOLVED, crime.ismSolved() ? 1 : 0);
        values.put(SUSPECT, crime.getmSuspect());
        return values;
    }

    @SuppressLint("NewApi")
    Crime getCrime(UUID id) {

        try (CrimeCursorWrapper cursorWrapper = new CrimeCursorWrapper(queryCrimes(UUID + " = ?", new String[]{id.toString()}))) {
            if (cursorWrapper.getCount() == 0) {
                return null;
            }

            cursorWrapper.moveToFirst();
            return cursorWrapper.getCrime();
        }
    }

    void addCrime(final Crime c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(CRIMES, null, values);
    }

    void updateCrime(Crime c) {
        String uuidString = c.getmId().toString();
        ContentValues values = getContentValues(c);

        mDatabase.update(CRIMES, values, UUID + " = ?", new String[]{uuidString});
    }

    private Cursor queryCrimes(String whereClause, String[] whereArgs) {
        return mDatabase.query(
                CRIMES,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
    }

    @SuppressLint("NewApi")
    List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        try (CrimeCursorWrapper cursorWrapper = new CrimeCursorWrapper(queryCrimes(null, null))) {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                crimes.add(cursorWrapper.getCrime());
                cursorWrapper.moveToNext();
            }
        }

        return crimes;
    }
}
