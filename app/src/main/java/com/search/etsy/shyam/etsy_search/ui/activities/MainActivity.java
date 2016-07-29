package com.search.etsy.shyam.etsy_search.ui.activities;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.search.etsy.shyam.etsy_search.R;
import com.search.etsy.shyam.etsy_search.db.EtsySearchDataContentProvider;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String PARAM_NEW_SEARCH = "PARAM_NEW_SEARCH";
    public static final String ACTION_NEW_SEARCH = "ACTION_NEW_SEARCH";
    public static final int INITIAL_PAGE_NUMBER = 1;
    public ProgressBar mProgressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
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

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //Passing search query obtained from search view to SearchListFragment through Broadcast
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            if(searchQuery.length()>0) {
                Intent mIntent = new Intent(ACTION_NEW_SEARCH);
                mIntent.putExtra(PARAM_NEW_SEARCH, searchQuery);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
                ContentValues values = new ContentValues();
                values.put(EtsySearchDataContentProvider.RecentSearchDataTable.COLUMN_SEARCH_KEY, searchQuery);
                getContentResolver().insert(EtsySearchDataContentProvider.CONTENT_RECENT_URI, values);
            }

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
}
