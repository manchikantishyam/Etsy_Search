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
import com.search.etsy.shyam.etsy_search.model.ListOfSearchData;
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
    private static final int SEARCH_LISTINGS_LOADER_ID =1;
    private boolean isWaitOnData;
    private String mSearchKeyWord;
    private int mSearchPage =1;
    private boolean isNewSearch = true;
    private List<SearchData> mSearchDataList = new ArrayList<>();
    private final int COLUMN_NO_TITLE =1;
    private final int COLUMN_NO_IMAGE_URL =2;
    private final int COLUMN_NO_PRICE =3;
    private final int COLUMN_NO_CURRENCY =4;
    private final String SAVED_INSTANCE_STATE_DATA ="SearchListFragmentData";
    LinearLayoutManager mLayoutManager;
    int pastVisiblesItems, visibleItemCount, totalItemCount;


    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SearchEstyListingsApiAction.ACTION)) {
                if(isWaitOnData) {
                    int mApiResultCode =intent.getExtras().getInt(EstySearchIntentService.PARAM_RESULT_CODE);
                    if(mApiResultCode == HttpURLConnection.HTTP_OK)
                        updateSearchResults();
                    else{
                        ((MainActivity)getActivity()).mProgressSpinner.setVisibility(View.GONE);
                        if(mApiResultCode == ApiError.NO_RESULTS_ERROR_CODE){
                            if(isNewSearch)
                                Toast.makeText(getActivity(), "No Results Found", Toast.LENGTH_SHORT).show();
                            else{
                                Toast.makeText(getActivity(), "End of Scroll Reached", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    isWaitOnData = false;
                }
            }else if(action.equals(MainActivity.NEW_SEARCH)){
                Bundle mBundle = intent.getExtras();
                if(mBundle!=null)
                     mSearchKeyWord= mBundle.getString(MainActivity.PARAM_NEW_SEARCH);
                mSearchPage=1;
                isNewSearch = true;
                getActivity().getContentResolver().delete(EtsySearchDataContentProvider.CONTENT_URI, null, null);
                requestResultsFromAPI();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.search_fragment_layout,container,false);
        mSearchRecylerView = (RecyclerView) mRootView.findViewById(R.id.search_recycler_view);
        mSearchRecylerView.setHasFixedSize(false);
        mSearchRecylerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSearchListingAdapter = new SearchListingAdapter(getActivity(),mSearchDataList);
        mSearchRecylerView.setAdapter(mSearchListingAdapter);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            mSearchDataList =((ListOfSearchData)savedInstanceState.
                    getParcelable(SAVED_INSTANCE_STATE_DATA)).getListOfSearchData();
        }
        this.setRetainInstance(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSearchRecylerView.setLayoutManager(mLayoutManager);

        mSearchRecylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!isWaitOnData) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isWaitOnData = true;
                            mSearchPage++;
                            isNewSearch = false;
                            requestResultsFromAPI();
                            Log.v("...", "Last Item Wow !");
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });

//        Bundle bundle = new Bundle();
//        bundle.putString(MainActivity.PARAM_SEARCH_KEYWORD, mSearchKeyWord);
//        bundle.putInt(MainActivity.PARAM_SEARCH_PAGE, mSearchPage);
//        getLoaderManager().restartLoader(SEARCH_LISTINGS_LOADER_ID, bundle, this);
    }

    private void updateSearchResults() {
        getLoaderManager().restartLoader(SEARCH_LISTINGS_LOADER_ID, null, SearchListFragment.this);
    }

    private void requestResultsFromAPI(){
        if(isNewSearch) {
            mSearchDataList = new ArrayList<SearchData>();
            updateListingAdapter(mSearchDataList);
        }
        SearchEstyListingsApiAction action = new SearchEstyListingsApiAction(getContext());
        action.setSearchKeyWords(mSearchKeyWord);
        action.setSearchPage(String.valueOf(mSearchPage));
        Intent intent = EstySearchIntentService.getApiActionIntent(getContext(), action);
        getContext().startService(intent);
        isWaitOnData=true;
        ((MainActivity)getActivity()).mProgressSpinner.setVisibility(View.VISIBLE);
        if(isNewSearch)
            this.getView().setVisibility(View.GONE);
    }
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(MainActivity.NEW_SEARCH);
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
                        },null, null, null);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data!=null && data.getCount() != mSearchDataList.size()) {
            Log.v("Loading Counts","DB Count: "+data.getCount()+" listCount: "+mSearchDataList.size());
            mSearchDataList = new ArrayList<SearchData>();
            ((MainActivity)getActivity()).mProgressSpinner.setVisibility(View.GONE);
            this.getView().setVisibility(View.VISIBLE);
            while (data.moveToNext()) {
                SearchData mSearchData = new SearchData();
                mSearchData.setTitle(data.getString(COLUMN_NO_TITLE));
                mSearchData.setImageURL(data.getString(COLUMN_NO_IMAGE_URL));
                mSearchData.setPrice(data.getDouble(COLUMN_NO_PRICE));
                mSearchData.setCurrencyType(data.getString(COLUMN_NO_CURRENCY));
                mSearchDataList.add(mSearchData);
            }
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

    public void updateListingAdapter(List<SearchData> lSearchData){
        mSearchDataList =lSearchData;
        mSearchListingAdapter.setData(mSearchDataList);
        mSearchListingAdapter.notifyDataSetChanged();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        ListOfSearchData mListOfSearchData = new ListOfSearchData();
        mListOfSearchData.setListOfSearchData(mSearchDataList);
        outState.putParcelable(SAVED_INSTANCE_STATE_DATA,mListOfSearchData);
        super.onSaveInstanceState(outState);
    }
}
