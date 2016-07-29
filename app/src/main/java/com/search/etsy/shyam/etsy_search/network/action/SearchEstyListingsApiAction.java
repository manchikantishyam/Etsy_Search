package com.search.etsy.shyam.etsy_search.network.action;

import android.content.ContentValues;
import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.search.etsy.shyam.etsy_search.db.EtsySearchDataContentProvider;
import com.search.etsy.shyam.etsy_search.model.ApiError;
import com.search.etsy.shyam.etsy_search.model.SearchData;
import com.search.etsy.shyam.etsy_search.ui.activities.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by Shyam on 7/17/16.
 */
public class SearchEstyListingsApiAction extends BaseApiAction {
    private static final String TAG = SearchEstyListingsApiAction.class.getSimpleName();

    public static final String ACTION = SearchEstyListingsApiAction.class.getPackage() + ".ACTION_SEARCH";


    private static final String QUERY_PARAM_SEARCH_KEY = "keywords";

    private static final String QUERY_PARAM_SEARCH_PAGE = "page";

    private static final String TAG_RESULTS = "results";
    private static final String TAG_TITLE = "title";
    private static final String TAG_MAIN_IMAGE = "MainImage";
    private static final String TAG_IMAGE_URL_170x135 = "url_170x135";
    private static final String TAG_PRICE = "price";
    private static final String TAG_CURRENCY_CODE = "currency_code";


    private String mSearchKeyWords;
    private String mSearchPage;

    public SearchEstyListingsApiAction() {

    }

    public SearchEstyListingsApiAction(Context context) {
        super(context);

    }

    public SearchEstyListingsApiAction(Parcel in) {
        super(in);
        mSearchKeyWords = in.readString();
        mSearchPage = in.readString();
    }

    @Override
    public void doNetworkOperation() throws IOException {
        addQueryParam(QUERY_PARAM_SEARCH_KEY, mSearchKeyWords);
        addQueryParam(QUERY_PARAM_SEARCH_PAGE, mSearchPage);
        super.doNetworkOperation();
    }

    @Override
    public void doProcessingResult(Context context) {
        {
            if (getResultCode() == HttpURLConnection.HTTP_OK) {
                try {
                    //Parsing the result & saving it on the database using content provider
                    JSONObject mObject = new JSONObject(getOperationResult());
                    JSONArray mSearchResultsArray = mObject.getJSONArray(TAG_RESULTS);
                    if(mSearchResultsArray!=null && mSearchResultsArray.length()>0) {
                        ContentValues[] contentValuesArray = new ContentValues[mSearchResultsArray.length()];
                        for (int i = 0; i < mSearchResultsArray.length(); i++) {
                            SearchData data = getSerachDatafromJson(mSearchResultsArray, i);
                            ContentValues value = new ContentValues();
                            value.put(EtsySearchDataContentProvider.SearchDataTable.COLUMN_TITLE, data.getTitle());
                            value.put(EtsySearchDataContentProvider.SearchDataTable.COLUMN_IMAGE_URL, data.getImageURL());
                            value.put(EtsySearchDataContentProvider.SearchDataTable.COLUMN_PRICE, data.getPrice());
                            value.put(EtsySearchDataContentProvider.SearchDataTable.COLUMN_CURRENCY, data.getCurrencyType());
                            value.put(EtsySearchDataContentProvider.SearchDataTable.COLUMN_PAGE, Integer.valueOf(mSearchPage));
                            contentValuesArray[i] = value;
                        }
                        if (mSearchPage.equals(String.valueOf(MainActivity.INITIAL_PAGE_NUMBER))) //deleting the old results from database in case of new search
                            context.getContentResolver().delete(EtsySearchDataContentProvider.CONTENT_URI, null, null);
                        int insertCount = context.getContentResolver().bulkInsert(EtsySearchDataContentProvider.CONTENT_URI, contentValuesArray);
                        Log.d(TAG, "inserted: " + insertCount);
                    }else{
                        setResultCode(ApiError.NO_RESULTS_ERROR_CODE);
                        setOperationResult("No Results");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, null, e);
                    setOperationResult(e.getMessage());
                    setResultCode(ApiError.DEF_ERROR_CODE);
                }
            }
        }

    }

    @Override
    public String getActionName() {
        return ACTION;
    }

    @NonNull
    private SearchData getSerachDatafromJson(JSONArray resultsArray, int i) throws JSONException {
        JSONObject jsonResult = resultsArray.getJSONObject(i);
        SearchData data = new SearchData();
        data.setTitle(jsonResult.getString(TAG_TITLE));
        data.setPrice(jsonResult.getDouble(TAG_PRICE));
        data.setCurrencyType(jsonResult.getString(TAG_CURRENCY_CODE));
        JSONObject mainImage = jsonResult.getJSONObject(TAG_MAIN_IMAGE);
        data.setImageURL(mainImage.getString(TAG_IMAGE_URL_170x135));
        return data;
    }

    public static final Creator<SearchEstyListingsApiAction> CREATOR = new Creator<SearchEstyListingsApiAction>() {
        @Override
        public SearchEstyListingsApiAction createFromParcel(Parcel in) {
            return new SearchEstyListingsApiAction(in);
        }

        @Override
        public SearchEstyListingsApiAction[] newArray(int size) {
            return new SearchEstyListingsApiAction[size];
        }
    };

    public void setSearchKeyWords(String mSearchKeyWords){
        this.mSearchKeyWords = mSearchKeyWords;
    }
    public void setSearchPage(String mSearchPage){
        this.mSearchPage = mSearchPage;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mSearchKeyWords);
        dest.writeString(mSearchPage);
    }
}

