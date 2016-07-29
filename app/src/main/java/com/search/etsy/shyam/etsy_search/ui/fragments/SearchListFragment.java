package com.search.etsy.shyam.etsy_search.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.search.etsy.shyam.etsy_search.R;
import com.search.etsy.shyam.etsy_search.db.EtsySearchDataContentProvider;
import com.search.etsy.shyam.etsy_search.model.ApiError;
import com.search.etsy.shyam.etsy_search.model.SearchData;
import com.search.etsy.shyam.etsy_search.network.EstySearchIntentService;
import com.search.etsy.shyam.etsy_search.network.action.SearchEstyListingsApiAction;
import com.search.etsy.shyam.etsy_search.ui.activities.MainActivity;
import com.search.etsy.shyam.etsy_search.ui.adapters.SearchListingAdapter;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shyam on 7/23/16.
 */
public class SearchListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView mSearchRecylerView;
    SearchListingAdapter mSearchListingAdapter;
    private static final int SEARCH_LISTINGS_LOADER_ID = 1;
    private boolean isWaitOnData;
    private String mSearchKeyWord;
    private int mSearchPage = 1;
    private boolean isNewSearch = true;
    private List<SearchData> mSearchDataList = new ArrayList<>();
    private final int COLUMN_NO_TITLE = 1;
    private final int COLUMN_NO_IMAGE_URL = 2;
    private final int COLUMN_NO_PRICE = 3;
    private final int COLUMN_NO_CURRENCY = 4;
    LinearLayoutManager mLayoutManager;
    int mPastVisiblesItems, mVisibleItemCount, mTotalItemCount;


    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if (mAction.equals(SearchEstyListingsApiAction.ACTION)) {
                //Action is received from the API (data from service is loaded in database)
                if (isWaitOnData) {
                    int mApiResultCode = intent.getExtras().getInt(EstySearchIntentService.PARAM_RESULT_CODE);
                    if (mApiResultCode == HttpURLConnection.HTTP_OK)
                        updateSearchResults();
                    else {
                        ((MainActivity) getActivity()).mProgressSpinner.setVisibility(View.GONE);
                        if (mApiResultCode == ApiError.NO_RESULTS_ERROR_CODE) {
                            if (isNewSearch)
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_no_results), Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_no_more_items), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
                        }
                    }
                    isWaitOnData = false;
                }
            } else if (mAction.equals(MainActivity.ACTION_NEW_SEARCH)) {
                //Action recieved from the MainActivity for New Search with search param
                Bundle mBundle = intent.getExtras();
                if (mBundle != null)
                    mSearchKeyWord = mBundle.getString(MainActivity.PARAM_NEW_SEARCH);
                mSearchPage = MainActivity.INITIAL_PAGE_NUMBER;//Since new search requesting for 1st page result
                isNewSearch = true;
                getActivity().getContentResolver().delete(EtsySearchDataContentProvider.CONTENT_URI, null, null);
                requestResultsFromAPI();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.search_fragment_layout, container, false);
        mSearchRecylerView = (RecyclerView) mRootView.findViewById(R.id.search_recycler_view);
        mSearchRecylerView.setHasFixedSize(false);
        mSearchRecylerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSearchListingAdapter = new SearchListingAdapter(getActivity(), mSearchDataList);
        mSearchRecylerView.setAdapter(mSearchListingAdapter);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setRetainInstance(true);//This will retain the state even when device is rotated
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSearchRecylerView.setLayoutManager(mLayoutManager);

        mSearchRecylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    mVisibleItemCount = mLayoutManager.getChildCount();
                    mTotalItemCount = mLayoutManager.getItemCount();
                    mPastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!isWaitOnData) {
                        if ((mVisibleItemCount + mPastVisiblesItems) >= mTotalItemCount) {
                            //End of scroll detected
                            isWaitOnData = true;
                            mSearchPage++;
                            isNewSearch = false;
                            requestResultsFromAPI();//requesting for next page results
                        }
                    }
                }
            }
        });

    }

    private void updateSearchResults() {
        //Updating the Search results by quering the database using content provider
        getLoaderManager().restartLoader(SEARCH_LISTINGS_LOADER_ID, null, SearchListFragment.this);
    }

    private void requestResultsFromAPI() {
        //Initiating the intent service to download the new content with the help of API
        if (((MainActivity) getActivity()).isNetworkAvailable()) {
            if (isNewSearch) {
                mSearchDataList = new ArrayList<SearchData>();
                updateListingAdapter(mSearchDataList);
                this.getView().setVisibility(View.GONE);//to make progress spinner come to middle of screen for new search
            }
            SearchEstyListingsApiAction action = new SearchEstyListingsApiAction(getContext());
            action.setSearchKeyWords(mSearchKeyWord);
            action.setSearchPage(String.valueOf(mSearchPage));
            Intent intent = EstySearchIntentService.getApiActionIntent(getContext(), action);
            getContext().startService(intent);
            isWaitOnData = true;
            ((MainActivity) getActivity()).mProgressSpinner.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEW_SEARCH);
        filter.addAction(SearchEstyListingsApiAction.ACTION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mLocalBroadcastReceiver,
                filter);

    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLocalBroadcastReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;
        switch (id) {
            case SEARCH_LISTINGS_LOADER_ID: {
                loader = new CursorLoader(getContext(),
                        EtsySearchDataContentProvider.CONTENT_URI,
                        new String[]{
                                EtsySearchDataContentProvider.SearchDataTable._ID,
                                EtsySearchDataContentProvider.SearchDataTable.COLUMN_TITLE,
                                EtsySearchDataContentProvider.SearchDataTable.COLUMN_IMAGE_URL,
                                EtsySearchDataContentProvider.SearchDataTable.COLUMN_PRICE,
                                EtsySearchDataContentProvider.SearchDataTable.COLUMN_CURRENCY
                        }, EtsySearchDataContentProvider.SEARCH_DATA_REQUEST, new String[]{String.valueOf(mSearchPage)}, null);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Using the data from the cursor to update the Listing Adapter
        if (data != null && data.getCount() != 0) {
            ((MainActivity) getActivity()).mProgressSpinner.setVisibility(View.GONE);
            this.getView().setVisibility(View.VISIBLE);
            while (data.moveToNext()) {
                SearchData mSearchData = new SearchData();
                mSearchData.setTitle(data.getString(COLUMN_NO_TITLE));
                mSearchData.setImageURL(data.getString(COLUMN_NO_IMAGE_URL));
                mSearchData.setPrice(data.getDouble(COLUMN_NO_PRICE));
                mSearchData.setCurrencyType(data.getString(COLUMN_NO_CURRENCY));
                mSearchDataList.add(mSearchData);
            }
            data.close();
            updateListingAdapter(mSearchDataList);
            isWaitOnData = false;
        } else {
            isWaitOnData = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        updateListingAdapter(new ArrayList<SearchData>());
    }

    public void updateListingAdapter(List<SearchData> lSearchData) {
        mSearchDataList = lSearchData;
        mSearchListingAdapter.setData(mSearchDataList);
        mSearchListingAdapter.notifyDataSetChanged();
    }

}
