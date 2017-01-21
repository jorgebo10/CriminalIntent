package com.bignerdranch.android.criminalintent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.f2prateek.dart.Dart;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.bignerdranch.android.criminalintent.CrimePagerActivity.EXTRA_CRIME_ID;
import static com.bignerdranch.android.criminalintent.DatePickerFragment.EXTRA_DATE;

public class CrimeFragment extends Fragment {
    private static final String DIALOG_CREATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    @BindView(R.id.text)
    EditText mTextFrame;
    @BindView(R.id.crime_title)
    EditText mTitleField;
    @BindView(R.id.crime_solved)
    CheckBox mSolvedCheckBox;
    @BindView(R.id.crime_date)
    Button mDateButton;
    @BindView(R.id.crime_report)
    Button mReportCrimeButton;
    @BindView(R.id.crime_suspect)
    Button mSuspectButton;
    private Crime mCrime;
    private Subscription mSubscription;


    public static CrimeFragment newInstance(final UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = Dart.get(getArguments(), EXTRA_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

        mSubscription = getTextFromNetwork("jorgebo10")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySubscriber());
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        if ((mSubscription != null) && (this.getActivity().isFinishing())) {
            mSubscription.unsubscribe();
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_crime, container, false);
        ButterKnife.bind(this, v);

        final UUID crimeId = Dart.get(getArguments(), EXTRA_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

        mTitleField.setText(mCrime.getmTitle());
        mSolvedCheckBox.setChecked(mCrime.ismSolved());
        if (mCrime.getmSuspect() != null) {
            mSuspectButton.setText(mCrime.getmSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        updateCrimeDate();

        return v;
    }

    private void updateCrimeDate() {
        mDateButton.setText(mCrime.getmDate().toString());
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.ismSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MM dd";
        String dateString = android.text.format.DateFormat.format(dateFormat, mCrime.getmDate()).toString();

        String suspect = mCrime.getmSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        return getString(R.string.crime_status, mCrime.getmTitle(), dateString, solvedString, suspect);
    }

    @OnTextChanged(R.id.crime_title)
    public void setCrimeTitle(final CharSequence charSequence) {
        mCrime.setmTitle(charSequence.toString());
    }

    @OnCheckedChanged(R.id.crime_solved)
    public void setCrimeSolved(final boolean b) {
        mCrime.setmSolved(b);
    }

    @OnClick(R.id.crime_date)
    public void showDateCrime() {
        DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getmDate());
        FragmentManager fm = getFragmentManager();
        dialog.setTargetFragment(this, REQUEST_DATE);
        dialog.show(fm, DIALOG_CREATE);
    }

    @OnClick(R.id.crime_report)
    public void reportCrime() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
        i.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.crime_report_subject));
        i = Intent.createChooser(i, getString(R.string.send_report));
        startActivity(i);
    }

    @OnClick(R.id.crime_suspect)
    public void addSuspect() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i, REQUEST_CONTACT);
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = Dart.get(intent.getExtras(), EXTRA_DATE);
            mCrime.setmDate(date);
            updateCrimeDate();
        } else if (requestCode == REQUEST_CONTACT) {
            Uri contatUri = intent.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            try (Cursor c = getActivity().getContentResolver().query(contatUri, queryFields, null, null, null)) {
                if (c != null && c.getCount() == 0) {
                    return;
                }

                if (c != null) {
                    c.moveToFirst();
                    String suspect = c.getString(0);
                    mCrime.setmSuspect(suspect);
                    mSuspectButton.setText(suspect);
                }
            }
        }
    }

    Observable<String> getTextFromNetwork(final String url) {
        return Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            String text = downloadText(url);
                            subscriber.onNext(text);
                            subscriber.onCompleted();
                        } catch (Throwable t) {
                            subscriber.onError(t);
                        }
                    }
                }
        );
    }

    private String downloadText(String user) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GithubService service = retrofit.create(GithubService.class);
        Call<List<Contributor>> call = service.contributors("fs_opensource", "android-boilerplate");

            List<Contributor> contributors = call.execute().body();
            return String.valueOf(contributors);
    }

    class MySubscriber extends Subscriber<String> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_LONG);
            Log.e(CrimeFragment.class.getName(), "Error retrieving", e);
        }

        @Override
        public void onNext(String s) {
            Log.e(CrimeFragment.class.getName(), s);

            mTextFrame.setText(s);
        }
    }

}

