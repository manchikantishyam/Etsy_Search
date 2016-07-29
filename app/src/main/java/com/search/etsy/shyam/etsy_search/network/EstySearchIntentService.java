package com.search.etsy.shyam.etsy_search.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.search.etsy.shyam.etsy_search.model.ApiError;
import com.search.etsy.shyam.etsy_search.network.action.BaseApiAction;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by Shyam on 7/17/16.
 */
public class EstySearchIntentService extends IntentService {
    private static final String TAG = EstySearchIntentService.class.getSimpleName();


    private static final String ACTION = "com.search.etsy.shyam.etsy_search.network.API_ACTION";
    private static final String PARAM_API_ACTION_INSTANCE = "PARAM_API_ACTION_INSTANCE";
    public static final String PARAM_RESULT_CODE = "PARAM_RESULT_CODE";
    public static final String PARAM_RESULT_ENTITY = "PARAM_RESULT_ENTITY";
    public EstySearchIntentService() {
        super("EstySearchIntentService");
    }

    public static Intent getApiActionIntent(Context context, BaseApiAction action) {
        Intent intent = new Intent(context, EstySearchIntentService.class);
        intent.setAction(ACTION);
        intent.putExtra(PARAM_API_ACTION_INSTANCE, (Parcelable) action);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION)) {
                BaseApiAction mApiAction = intent.getParcelableExtra(PARAM_API_ACTION_INSTANCE);
                Intent mResultIntent = new Intent(mApiAction.getActionName());
                mResultIntent.putExtras(intent.getExtras());
                try {
                    mApiAction.doNetworkOperation();
                } catch (IOException e) {
                    Log.e(TAG, null, e);
                    mResultIntent.putExtra(PARAM_RESULT_CODE, ApiError.DEF_ERROR_CODE);
                    mResultIntent.putExtra(PARAM_RESULT_ENTITY, new ApiError(e.getMessage()));
                }
                mApiAction.doProcessingResult(getApplicationContext());
                mResultIntent.putExtra(PARAM_RESULT_CODE, mApiAction.getResultCode());
                if (mApiAction.getResultCode() == HttpURLConnection.HTTP_OK) {
                    mResultIntent.putExtra(PARAM_RESULT_CODE, mApiAction.getResultCode());
                } else {
                    mResultIntent.putExtra(PARAM_RESULT_ENTITY, mApiAction.getError());
                    mResultIntent.putExtra(PARAM_RESULT_CODE, mApiAction.getResultCode());
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mResultIntent);
            }
        }
    }
}
