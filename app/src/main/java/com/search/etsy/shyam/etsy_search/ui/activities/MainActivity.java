package com.search.etsy.shyam.etsy_search.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import com.search.etsy.shyam.etsy_search.R;
import it.sephiroth.android.library.tooltip.Tooltip;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String PARAM_NEW_SEARCH = "PARAM_NEW_SEARCH";
    public static final String ACTION_NEW_SEARCH = "ACTION_NEW_SEARCH";
    public static final int INITIAL_PAGE_NUMBER = 1;
    public ProgressBar mProgressSpinner;
    public static final String KEY_IS_FIRST_TIME_USER ="KEY_IS_FIRST_TIME_USER";
    public static final String ETSY_PREFERENCES = "ETSY_PREFERENCES" ;
    public static boolean isFirstTimeUser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        isFirstTimeUser = getSharedPreferences(ETSY_PREFERENCES, MODE_PRIVATE).getBoolean(KEY_IS_FIRST_TIME_USER,true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem mSearchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setOnQueryTextListener(this);
        // Associate searchable configuration with the SearchView
        SearchManager mSearchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(
                mSearchManager.getSearchableInfo(getComponentName()));
        if(isFirstTimeUser) {
            final ViewTreeObserver mViewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
            mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //this will obtain the location of the search widget
                    View menuButton = findViewById(R.id.search);
                    if (menuButton != null) {
                        int[] mLocation = new int[2];
                        menuButton.getLocationInWindow(mLocation);
                        showHelpPopUp(mLocation[0] + (menuButton.getMeasuredWidth() / 2),
                                mLocation[1] + (menuButton.getMeasuredHeight() / 2),
                                MainActivity.this.getString(R.string.hint_search));
                        mViewTreeObserver.removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //Passing search query obtained from search view to SearchListFragment through Broadcast
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Intent mIntent = new Intent(ACTION_NEW_SEARCH);
            mIntent.putExtra(PARAM_NEW_SEARCH, searchQuery);
            if(isFirstTimeUser) {
                SharedPreferences.Editor mEditor = getSharedPreferences(ETSY_PREFERENCES, MODE_PRIVATE).edit();
                mEditor.putBoolean(KEY_IS_FIRST_TIME_USER, false);
                mEditor.commit();
                isFirstTimeUser=false;
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
        }
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // User changed the text
        return false;
    }

    public boolean isNetworkAvailable() {
        //Checks if device is connected to network
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showHelpPopUp(int x, int y, String message){
        Point mDest = new Point(x,y);
        int mDuration = 2500; //2ms duration
        Tooltip.make(this,
                new Tooltip.Builder()
                        .anchor(mDest, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), mDuration)
                        .text(message)
                        .withArrow(false)
                        .withOverlay(true).build()).show();
    }
}
