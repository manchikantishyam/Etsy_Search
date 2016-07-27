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

    public static Intent getApiActionIntent(Context context, BaseApiAction<?> action) {
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
                BaseApiAction apiAction = intent.getParcelableExtra(PARAM_API_ACTION_INSTANCE);
                Intent resultIntent = new Intent(apiAction.getActionName());
                resultIntent.putExtras(intent.getExtras());
                try {
                    apiAction.doNetworkOperation();
                } catch (IOException e) {
                    Log.e(TAG, null, e);
                    resultIntent.putExtra(PARAM_RESULT_CODE, ApiError.DEF_ERROR_CODE);
                    resultIntent.putExtra(PARAM_RESULT_ENTITY, new ApiError(e.getMessage()));
                }
                apiAction.doProcessingResult(getApplicationContext());
                resultIntent.putExtra(PARAM_RESULT_CODE, apiAction.getResultCode());
                if (apiAction.getResultCode() == HttpURLConnection.HTTP_OK) {
                    resultIntent.putExtra(PARAM_RESULT_ENTITY, apiAction.getResultEntity());
                } else {
                    resultIntent.putExtra(PARAM_RESULT_ENTITY, apiAction.getError());
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);
            }
        }
    }
}
