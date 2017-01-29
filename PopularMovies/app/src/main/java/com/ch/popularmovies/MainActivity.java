package com.ch.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;

import com.ch.popularmovies.entities.Movie;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements MovieCallback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO REMOVE __ DEBUG ONLY
        // setUpDebug();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (findViewById(R.id.detail_container) == null) {
            mTwoPane = false;
        } else {
            // Master-Detail layout => Two pane
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailMovieFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        setUpViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_widget);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    // TODO :: REMOVE REMOVE REMOVE ___ DEBUG ONLY !!!
    private void setUpDebug() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    private void setUpViewPager(ViewPager viewPager) {
        MoviesPagerAdapter mPagerAdapter = new MoviesPagerAdapter(getSupportFragmentManager());
        // This sequence is important!
        mPagerAdapter.addFragment(new MoviesFragment(), "Popular");
        mPagerAdapter.addFragment(new MoviesFragment(), "Top Rated");
        mPagerAdapter.addFragment(new FavoriteMoviesFragment(), "Favorites");

        viewPager.setAdapter(mPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mTwoPane) {
                    DetailMovieFragment dmf = (DetailMovieFragment) getSupportFragmentManager().findFragmentById(R.id.detail_container);
                    dmf.setPlaceHolderVisibility(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onMovieSelected(Movie movie) {
        if (mTwoPane) {
            // Large screens mode
            DetailMovieFragment df = DetailMovieFragment.getInstance(movie);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            // phone mode
            Intent intent = new Intent(this, DetailMovieActivity.class);
            intent.putExtra("movie", movie);
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        }
    }

    class MoviesPagerAdapter extends FragmentPagerAdapter {
        private static final int MOST_POPULAR_POS = 0;
        private static final int HIGHEST_RATED_POS = 1;

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
            }

            mFragments.get(position).setArguments(args);
            return mFragments.get(position);
        }

        public Fragment getFragment(int position){
            return this.mFragments.get(position);
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
