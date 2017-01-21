package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;


class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;

    Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    String getmSuspect() {
        return mSuspect;
    }

    void setmSuspect(String mSuspect) {
        this.mSuspect = mSuspect;
    }

    UUID getmId() {
        return mId;
    }

    String getmTitle() {
        return mTitle;
    }

    void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    Date getmDate() {
        return mDate;
    }

    void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    boolean ismSolved() {
        return mSolved;
    }

    void setmSolved(boolean mSolved) {
        this.mSolved = mSolved;
    }
}
