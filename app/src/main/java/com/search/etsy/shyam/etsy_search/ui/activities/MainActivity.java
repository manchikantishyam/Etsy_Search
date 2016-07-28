package com.search.etsy.shyam.etsy_search.ui.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.search.etsy.shyam.etsy_search.R;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String PARAM_SEARCH_KEYWORD = "PARAM_SEARCH_KEYWORD";
    public static final String PARAM_SEARCH_PAGE = "PARAM_SEARCH_PAGE";
    public static final String PARAM_NEW_SEARCH = "PARAM_NEW_SEARCH";
    public static final String NEW_SEARCH = "NEW_SEARCH";
    public static final String SEARCH_UPDATE = "SEARCH_UPDATE";
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
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Intent mIntent = new Intent(NEW_SEARCH);
            mIntent.putExtra(PARAM_NEW_SEARCH, searchQuery);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
            Toast.makeText(this, "Searching by: " + searchQuery, Toast.LENGTH_SHORT).show();
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
}
