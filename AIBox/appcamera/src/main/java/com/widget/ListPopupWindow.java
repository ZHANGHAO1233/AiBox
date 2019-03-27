package com.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.adapter.SpinnerAdapter;
import com.idata.aibox.R;

/**
 * Created by zhanghao on 2017-08-09.
 */
public class ListPopupWindow extends PopupWindow {
    private SpinnerAdapter mAdapter;
    private ListView mListView;
    private View mContentView;
    private LinearLayout mHeadLayout;
    private Context mContext;
    private int mSelectedPosition = -1;

    public ListPopupWindow(Context context) {
        super(context);
        // 加载popupWindow的布局文件
        mContentView = View.inflate(context, R.layout.dropdownlist_popupwindow, null);
        this.setContentView(mContentView);
        mListView = (ListView) mContentView.findViewById(R.id.listView);
        mHeadLayout = (LinearLayout) mContentView.findViewById(R.id.headView);
        this.mContext = context;
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setBackgroundDrawable(null);
    }

    /**
     * 打开下拉列表弹窗
     */
    public void showPopWindow(View view) {
        if (this.mAdapter == null) {
            return;
        }
        int width = view.getMeasuredWidth();
        this.setWidth(width);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.showAsDropDown(view);
        this.getContentView().setOnTouchListener((v, event) -> {
            ListPopupWindow.this.setFocusable(false);
            closePopWindow();
            return true;
        });
    }

    /**
     * 设置显示在v上方(以v的左边距为开始位置)
     */
    public void showPopWindowUp(View view) {
        if (this.mAdapter == null) {
            return;
        }
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int height = 150;
        int width = view.getMeasuredWidth();
        //在控件上方显示
        this.setWidth(width);
        this.setHeight(height);
        this.getContentView().setOnTouchListener((v, event) -> {
            ListPopupWindow.this.setFocusable(false);
            closePopWindow();
            return true;
        });
        this.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] - height);
    }

    /**
     * 关闭下拉列表弹窗
     */
    public void closePopWindow() {
        this.dismiss();
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(SpinnerAdapter adapter) {
        this.mAdapter = adapter;
        this.mListView.setAdapter(adapter);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listner) {
        mListView.setOnItemClickListener(listner);
    }
}
