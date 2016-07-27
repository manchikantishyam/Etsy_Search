package com.search.etsy.shyam.etsy_search.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Shyam on 7/23/16.
 */
public class SearchData implements Parcelable {
    private int mId;
    private String mTitle;
    private String mImageURL;
    private Double mPrice;
    private String mCurrencyType;

    public SearchData() {

    }


    protected SearchData(Parcel in) {
        mId = in.readInt();
        mTitle = in.readString();
        mImageURL = in.readString();
        mPrice = in.readDouble();
        mCurrencyType = in.readString();
    }

    public static final Creator<SearchData> CREATOR = new Creator<SearchData>() {
        @Override
        public SearchData createFromParcel(Parcel in) {
            return new SearchData(in);
        }

        @Override
        public SearchData[] newArray(int size) {
            return new SearchData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeString(mImageURL);
        dest.writeDouble(mPrice);
        dest.writeString(mCurrencyType);
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getImageURL() {
        return mImageURL;
    }

    public void setImageURL(String mImageURL) {
        this.mImageURL = mImageURL;
    }

    public Double getPrice() {
        return mPrice;
    }

    public void setPrice(Double mPrice) {
        this.mPrice = mPrice;
    }

    public String getCurrencyType() {
        return mCurrencyType;
    }

    public void setCurrencyType(String mCurrencyType) {
        this.mCurrencyType = mCurrencyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchData that = (SearchData) o;

        if (mId != that.mId) return false;
        if (Double.compare(that.mPrice, mPrice) != 0) return false;
        if (mTitle != null ? !mTitle.equals(that.mTitle) : that.mTitle != null) return false;
        if (mImageURL != null ? !mImageURL.equals(that.mImageURL) : that.mImageURL != null)
            return false;
        return !(mCurrencyType != null ? !mCurrencyType.equals(that.mCurrencyType) : that.mCurrencyType != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = mId;
        result = 31 * result + (mTitle != null ? mTitle.hashCode() : 0);
        result = 31 * result + (mImageURL != null ? mImageURL.hashCode() : 0);
        result = 31 * result + (mCurrencyType != null ? mCurrencyType.hashCode() : 0);
        temp = Double.doubleToLongBits(mPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SearchData{" +
                "mId=" + mId +
                ", mTitle='" + mTitle + '\'' +
                ", mImageURL='" + mImageURL + '\'' +
                ", mPrice=" + mPrice +
                ", mCurrencyType='" + mCurrencyType + '\'' +
                '}';
    }
}