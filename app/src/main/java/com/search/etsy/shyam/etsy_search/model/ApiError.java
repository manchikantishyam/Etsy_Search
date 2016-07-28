package com.search.etsy.shyam.etsy_search.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Shyam on 7/17/16.
 */
public class ApiError implements Parcelable{

    public static final int DEF_ERROR_CODE = -1;

    public static final int NO_RESULTS_ERROR_CODE = 0;

    private String mErrorMessage;

    public ApiError(){

    }

    public ApiError(Parcel in) {
        mErrorMessage = in.readString();
    }

    public ApiError(String message) {
        mErrorMessage = message;
    }

    public void setErrorMessage(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

    public static final Parcelable.Creator<ApiError> CREATOR = new Parcelable.Creator<ApiError>() {
        @Override
        public ApiError createFromParcel(Parcel in) {
            return new ApiError(in);
        }

        @Override
        public ApiError[] newArray(int size) {
            return new ApiError[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mErrorMessage);
    }
}
