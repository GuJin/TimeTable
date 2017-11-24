package com.sunrain.timetablev4.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseListAdapter<E, VH extends BaseListAdapter.ViewHolder> extends BaseAdapter {

    protected List<E> mList;

    protected BaseListAdapter() {
        mList = new ArrayList<>();
    }

    protected BaseListAdapter(List<E> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public E getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH viewHolder;
        if (convertView == null) {
            convertView = createConvertView(LayoutInflater.from(parent.getContext()), parent);
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            //noinspection unchecked
            viewHolder = (VH) convertView.getTag();
        }

        onGetView(viewHolder, position);

        return convertView;
    }


    private VH createViewHolder(View convertView) {
        return onCreateViewHolder(convertView);
    }

    protected abstract View createConvertView(LayoutInflater inflater, ViewGroup parent);

    protected abstract VH onCreateViewHolder(View convertView);

    protected abstract void onGetView(VH viewHolder, int position);

    public static abstract class ViewHolder {

        public ViewHolder(View itemView) {
            initView(itemView);
        }

        public abstract void initView(View itemView);
    }
}
