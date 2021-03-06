package com.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/14
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

    private Context mContext;
    public LayoutInflater mLayoutInflater;
    private ArrayList<T> mArrayList;
    private int[] mItemLayoutId ;


    public CommonAdapter(Context context, ArrayList<T> arrayList,int[] itemLayoutId) {

        mContext = context;
        mArrayList = arrayList;
        mLayoutInflater = LayoutInflater.from(context);
        mItemLayoutId = itemLayoutId;
    }

    public CommonAdapter() {
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemViewType(int position) {
        if (position==0){
            return 0;
        }
        return 1;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = getViewHolder(position, convertView, parent);
        //noinspection unchecked
        convert(viewHolder, (T) getItem(position));
        return viewHolder.getConvertView();
    }

    public abstract void convert (ViewHolder viewHolder,T item);

    public ViewHolder getViewHolder(int position, View convertView, ViewGroup parent){

        int type = getItemViewType(position);
        int itemLayoutId = 0;
        switch (type){
            case 0:
                itemLayoutId = mItemLayoutId[0];
                break;
            case 1:
                itemLayoutId = mItemLayoutId[1];
                break;
        }
        return ViewHolder.getViewHolder(mContext, convertView, parent, itemLayoutId, position);
    }


}
