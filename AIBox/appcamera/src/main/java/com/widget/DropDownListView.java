package com.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adapter.SpinnerAdapter;
import com.box.utils.ILog;
import com.idata.aibox.R;

@SuppressLint("NewApi")
/**
 * 下拉列表框控件
 *
 * @author zhanghao
 *
 */
public class DropDownListView extends LinearLayout {
    private TextView editText;
    private ListPopupWindow popupWindow = null;
    private Context context;
    private boolean showDown = true;

    public DropDownListView(Context context) {
        this(context, null);
    }

    public DropDownListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView();
    }

    public void initView() {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        layoutInflater.inflate(R.layout.dropdownlist_view, this, true);
        this.editText = (TextView) findViewById(R.id.text);
        this.popupWindow = new ListPopupWindow(context);
    }

    public void initListener() {
        this.setOnClickListener(v -> {
            if (!popupWindow.isShowing()) {
                if (showDown) {
                    showPopWindow();
                } else {
                    showPopWindowUp();
                }
            } else {
                closePopWindow();
            }
        });
        this.popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            DropDownListView.this.popupWindow.setSelectedPosition(position);
            if (title) {
                updateDropView(position);
            }
            if (DropDownListView.this.itemSelectedListener != null) {
                DropDownListView.this.itemSelectedListener.onItemSelected(getAdapter(),
                        DropDownListView.this, view, position, id);
            }
            if (autoDismiss) {
                closePopWindow();
            }
        });
    }

    private void updateDropView(int position) {
        ILog.d("更新视图标题");
        if (position == -1) {
            editText.setText(context.getString(R.string.select));
        } else {
            editText.setText(getAdapter().getName(position));
            setBackgroundResource(R.drawable.btn_dayinfenlei_normal_bg2);
        }
        requestLayout();
        invalidate();
    }


    public void setAdapter(SpinnerAdapter adapter) {
        this.popupWindow.setAdapter(adapter);
        initListener();
    }

    public SpinnerAdapter getAdapter() {
        return this.popupWindow.getAdapter();
    }


    /**
     * 打开下拉列表弹窗
     */
    private void showPopWindow() {
        if (this.getAdapter() == null) {
            return;
        }
        this.popupWindow.showPopWindow(this);
    }

    /**
     * 设置显示在v上方(以v的左边距为开始位置)
     */
    private void showPopWindowUp() {
        if (this.getAdapter() == null) {
            return;
        }
        this.popupWindow.showPopWindowUp(this);
    }

    /**
     * 关闭下拉列表弹窗
     */
    private void closePopWindow() {
        ILog.d("关闭弹窗");
        this.popupWindow.closePopWindow();
    }

    public void setSelection(int position) {
        this.popupWindow.setSelectedPosition(position);
        updateDropView(position);
    }

    public void setText(String text) {
        ILog.d("设置标题文本");
        editText.setText(text);
    }

    private OnItemSelectedListener itemSelectedListener;

    public OnItemSelectedListener getItemSelectedListener() {
        return itemSelectedListener;
    }

    public void setItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    /**
     * 监听选中
     *
     * @author demo2
     */
    public interface OnItemSelectedListener {
        void onItemSelected(SpinnerAdapter adapter, View parent, View view, int
                position, long id);
    }

    private OnHeadViewClickListener headViewClickListener;

    public OnHeadViewClickListener getHeadViewClickListener() {
        return headViewClickListener;
    }

    public void setHeadViewClickListener(OnHeadViewClickListener headViewClickListener) {
        this.headViewClickListener = headViewClickListener;
    }

    public interface OnHeadViewClickListener {
        void onHeadViewClick(View headView);
    }

    private OnViewDismissListener onViewDismissListener;

    public OnViewDismissListener getOnViewDismissListener() {
        return onViewDismissListener;
    }

    public void setOnViewDismissListener(OnViewDismissListener onViewDismissListener) {
        this.onViewDismissListener = onViewDismissListener;
    }

    public interface OnViewDismissListener {
        void onViewDismiss();
    }


    public void setSelectedItemPosition(int selectedItemPosition) {
        ILog.d("设置选中位置");
        this.popupWindow.setSelectedPosition(selectedItemPosition);
        updateDropView(selectedItemPosition);
    }

    public int getSelectedItemPosition() {
        ILog.d("获得选中位置");
        return this.popupWindow.getSelectedPosition();
    }

    private boolean title = true;

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(boolean title) {
        ILog.d("设置标题");
        this.title = title;
    }

    private boolean autoDismiss = true;

    /**
     * 點擊選擇項消失
     *
     * @param autoDismiss
     */
    public void setAutoDismiss(boolean autoDismiss) {
        ILog.d("點擊選擇項消失");
        this.autoDismiss = autoDismiss;
    }


    public void setShowDown(boolean showDown) {
        this.showDown = showDown;
    }
}
