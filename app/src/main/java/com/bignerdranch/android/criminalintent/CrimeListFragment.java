package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class CrimeListFragment extends Fragment {
    private static final int REQUEST_CRIME = 1;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final String TAG = "CrimeListFragment";

    @BindView(R.id.crime_recycler_view)
    RecyclerView mRecyclerView;

    private CrimeAdapter mCrimeAdapter;
    private boolean mSubtitleVisible;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Observable<String> myObservable = Observable.from(Arrays.asList("Hello from RxJava", "Welcome...", "Goodbye"));
        Subscriber<String> mySubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "Rx Java events completed");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error found processing stream", e);
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "New event -" + s);
            }
        };

        myObservable.subscribe(mySubscriber);

        Observable<Integer> myObservable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(10);
                subscriber.onNext(3);
                subscriber.onNext(9);
                subscriber.onCompleted();
            }
        });

        myObservable1.subscribe(integer -> Log.i(TAG, "New number :" + integer),
                throwable -> Log.e(TAG, "Error: " + throwable.getMessage()),
                () -> Log.i(TAG, "Rx number stream completed"));

        Observable<Integer> myObservable2 = Observable.just(1,2,3);
        myObservable2.subscribe(integer -> Log.i(TAG, "New number :" + integer),
                throwable -> Log.e(TAG, "Error: " + throwable.getMessage()),
                () -> Log.i(TAG, "Rx number stream completed"));

        String content = "This is an example \n " +
                "Looking for lines with the word RxJava\n" +
                "We are finished.";

        Observable
                .just(content)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String content) {
                        return Observable.from(content.split("\n"));
                    }
                })
                .filter(line -> line.contains("RxJava"))
                .count()
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.i(TAG, "Number of Lines " + integer);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(crimes);
            mRecyclerView.setAdapter(mCrimeAdapter);
        } else {
            mCrimeAdapter.setCrimes(crimes);
            mCrimeAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private void updateSubtitle() {
        final CrimeLab crimeLab = CrimeLab.get(getActivity());
        final int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        final MenuItem showSubtitleMenuItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible) {
            showSubtitleMenuItem.setTitle(R.string.hide_subtitle);
        } else {
            showSubtitleMenuItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_crime:
                final Crime crime = new Crime(UUID.randomUUID());
                CrimeLab.get(getActivity()).addCrime(crime);
                final Intent intent = Henson.with(getActivity())
                        .gotoCrimePagerActivity()
                        .crimeId(crime.getmId())
                        .build();
                startActivity(intent);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.list_item_crime_title_text_view)
        TextView mTitleTextView;
        @BindView(R.id.list_item_crime_date_text_view)
        TextView mDateTextView;
        @BindView(R.id.list_item_crime_solved_check_box)
        CheckBox mSolvedCheckBox;
        private Crime mCrime;


        CrimeHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        void bindCrime(final Crime crime) {
            this.mCrime = crime;
            mTitleTextView.setText(crime.getmTitle());
            mDateTextView.setText(crime.getmDate().toString());
            mSolvedCheckBox.setChecked(crime.ismSolved());
        }

        @Override
        public void onClick(final View view) {
            final Intent intent = Henson.with(getActivity())
                    .gotoCrimePagerActivity()
                    .crimeId(mCrime.getmId())
                    .build();
            startActivityForResult(intent, REQUEST_CRIME);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        CrimeAdapter(final List<Crime> crimes) {
            this.mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(final CrimeHolder holder, final int position) {
            final Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }
}
