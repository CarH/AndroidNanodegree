package com.ch.popularmovies;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean mTwoPane;

    private MoviesPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // TODO :: REMOVE IT LATER !
        mTwoPane = false;

        if (!mTwoPane) {
            ViewPager viewPager = (ViewPager) findViewById(R.id.container);
            setUpViewPager(viewPager);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_widget);
            tabLayout.setupWithViewPager(viewPager);

        } else {
            // TODO :: Tab logic here

            // Master-Detail layout => Two pane
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MoviesFragment())
                        .commit();
            }
        }
    }

    private void setUpViewPager(ViewPager viewPager) {
        mPagerAdapter = new MoviesPagerAdapter(getSupportFragmentManager());
        // This sequence is important!
        mPagerAdapter.addFragment(new MoviesFragment(), "Popular");
        mPagerAdapter.addFragment(new MoviesFragment(), "Top Rated");
        mPagerAdapter.addFragment(new MoviesFragment(), "Favorites");

        viewPager.setAdapter(mPagerAdapter);
    }

    class MoviesPagerAdapter extends FragmentPagerAdapter {
        private static final int MOST_POPULAR_POS = 0;
        private static final int HIGHEST_RATED_POS = 1;
        private static final int FAVORITE_POS = 2;

        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MoviesPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        public void addFragment(Fragment f, String title) {
            mFragments.add(f);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            if (position == MOST_POPULAR_POS) {
                args.putString(MoviesFragment.ORDER_BY_KEY, getString(R.string.pref_order_by_most_popular));
            } else if (position == HIGHEST_RATED_POS) {
                args.putString(MoviesFragment.ORDER_BY_KEY, getString(R.string.pref_order_by_highest_rated));
            } else if (position == FAVORITE_POS) {
                // TODO :: IMPLEMENT THE FAVORITE LOGIC HERE
            }
            mFragments.get(position).setArguments(args);
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
