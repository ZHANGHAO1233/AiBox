package com.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LogAdapter extends BaseAdapter {
    private List<String> list;
    private Context context;

    public LogAdapter(List<String> list, Context context) {
        super();
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View contextView, ViewGroup viewGroup) {
        String data = getItem(position);
        TextView tv;
        if (contextView == null) {
            tv = new TextView(this.context);
            contextView = tv;
        } else {
            tv = (TextView) contextView;
        }
        tv.setText(data);
        return contextView;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
