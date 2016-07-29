package com.search.etsy.shyam.etsy_search.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.search.etsy.shyam.etsy_search.R;
import com.search.etsy.shyam.etsy_search.model.SearchData;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Shyam on 7/23/16.
 */
public class SearchListingAdapter extends RecyclerView.Adapter<SearchListingAdapter.MyViewHolder> {
    private List<SearchData> mSearchDataList;
    Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mPrice;
        public ImageView image;

        public MyViewHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.item_title);
            mPrice = (TextView) view.findViewById(R.id.item_price);
            image = (ImageView) view.findViewById(R.id.item_image);
        }
    }

    public SearchListingAdapter (Context mContext, List<SearchData> searchDataList){
        this.mSearchDataList = searchDataList;
        this.mContext = mContext;
    }

    public void setData (List<SearchData> searchDataList){
        this.mSearchDataList = searchDataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item_card, parent, false);
        return new MyViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder mHolder, int position) {
        SearchData mSearchData = mSearchDataList.get(position);
        mHolder.mTitle.setText(mSearchData.getTitle());
        mHolder.mPrice.setText(mSearchData.getCurrencyType() + "  "
                + String.valueOf(mSearchData.getPrice()));
        Picasso.with(mContext)
                .load(mSearchData.getImageURL())
                .into(mHolder.image);
    }

    @Override
    public int getItemCount() {
            return mSearchDataList.size();
    }
}
