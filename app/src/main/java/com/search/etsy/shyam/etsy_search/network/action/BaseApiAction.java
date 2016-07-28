package com.search.etsy.shyam.etsy_search.network.action;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.search.etsy.shyam.etsy_search.R;
import com.search.etsy.shyam.etsy_search.model.ApiError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shyam on 7/17/16.
 */
public abstract class BaseApiAction<E extends Parcelable> implements Parcelable {
    private static final String TAG = BaseApiAction.class.getSimpleName();

    private static final String PRINT_PATTERN = "%s(%d/%d):%s";

    private static final String QUERY_PARAM_API_KEY = "api_key";

    private static final String QUERY_PARAM__INCLUDES = "includes";

    private static final int READ_TIMEOUT = 100000;

    private static final int CONNECTION_TIMEOUT = 150000;

    private static final String GET_METHOD = "GET";

    private String mUrl;
    private Map<String, String> mQueryParams;

    private String mOperationResult;
    private int mResultCode;

    public BaseApiAction() {
        mQueryParams = new HashMap<String, String>();
    }

    public BaseApiAction(Context context) {
        this();
        this.mUrl = context.getString(R.string.esty_url_static_part);
        addQueryParam(QUERY_PARAM_API_KEY, context.getString(R.string.etsy_api_key));
        addQueryParam(QUERY_PARAM__INCLUDES, context.getString(R.string.etsy_url_main_image));
    }

    public BaseApiAction(Parcel in) {
        this();
        mUrl = in.readString();
        in.readMap(mQueryParams, BaseApiAction.class.getClassLoader());
    }

    public void addQueryParam(String key,String value){
        mQueryParams.put(key, value);

    }

    public String getOperationResult() {
        return mOperationResult;
    }

    public int getResultCode() {
        return mResultCode;
    }

    protected void setResultCode(int mResultCode) {
        this.mResultCode = mResultCode;
    }

    protected void setOperationResult(String mOperationResult) {
        this.mOperationResult = mOperationResult;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeMap(mQueryParams);
    }

    public void doNetworkOperation() throws IOException {
        InputStream is = null;
        HttpURLConnection urlConnection = getURLConnection(getUrl());
        mResultCode = urlConnection.getResponseCode();
        if (mResultCode == HttpURLConnection.HTTP_OK) {
            is = urlConnection.getInputStream();
        } else {
            is = urlConnection.getErrorStream();
        }
        mOperationResult = getStringFromStream(is);
        print(TAG, mOperationResult);
        Log.v("Loading Counts: ","New operation result fetched ");
    }


    private String getUrl() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(mUrl);
        if (!mQueryParams.isEmpty()){
            sb.append('?');
            Set<String> keySet = mQueryParams.keySet();
            for(String key:keySet){
                sb.append(URLEncoder.encode(key, Charset.defaultCharset().name()));
                sb.append('=');
                sb.append(URLEncoder.encode(mQueryParams.get(key), Charset.defaultCharset().name()));
                sb.append('&');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    private HttpURLConnection getURLConnection(String urlString) throws IOException {
        Log.d(TAG, "getURLConnection: " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.setReadTimeout(READ_TIMEOUT);
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setRequestMethod(GET_METHOD);
        urlConnection.setDoInput(true);
        return urlConnection;
    }




    private static void print(String tag, String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return;
        }
        int len = 2000;
        int partsCount = inputString.length() / len + 1;
        for (int i = 0; i < partsCount; i++) {
            int step = i * len;
            Log.d(TAG, String.format(
                    PRINT_PATTERN,
                    tag,
                    i + 1,
                    partsCount,
                    inputString.substring(step, step
                            + ((step + len) <= inputString.length() ? len
                            : inputString.length() - step))));
        }
    }

    private String getStringFromStream(InputStream inputStream) {
        String readString;
        StringBuilder builder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while ((readString = in.readLine()) != null) {
                builder.append(readString);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return builder.toString();
    }

    public ApiError getError(){
        return new ApiError(getOperationResult());
    }

    public abstract void doProcessingResult(Context context);

    public abstract E getResultEntity();

    public abstract String getActionName();
}
