package com.search.etsy.shyam.etsy_search.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shyam on 7/23/16.
 */
public class ListOfSearchData implements Parcelable {
    private List<SearchData> mSearchDataList;
    public ListOfSearchData (){
        mSearchDataList = new ArrayList<SearchData>();
    }
    public List<SearchData>getListOfSearchData (){
        return mSearchDataList;
    }
    public void setListOfSearchData(List<SearchData> mSearchDataList){
        this.mSearchDataList = mSearchDataList;
    }

    protected ListOfSearchData(Parcel in) {
        in.readTypedList(mSearchDataList,SearchData.CREATOR);
    }

    public static final Creator<ListOfSearchData> CREATOR = new Creator<ListOfSearchData>() {
        @Override
        public ListOfSearchData createFromParcel(Parcel in) {
            return new ListOfSearchData(in);
        }

        @Override
        public ListOfSearchData[] newArray(int size) {
            return new ListOfSearchData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mSearchDataList);
    }
}
