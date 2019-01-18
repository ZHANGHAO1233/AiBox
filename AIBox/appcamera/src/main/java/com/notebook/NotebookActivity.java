package com.notebook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.retail.RetailInputParam;
import com.bean.CloseParam;
import com.bean.OpenParam;
import com.bean.Order;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.idata.aibox.R;
import com.mgr.OrderFileManager;

import java.util.ArrayList;
import java.util.List;

import static com.box.utils.ILog.TIME_TAG;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_INITED;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_MESS;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_ORDER;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_PARMA;

public class NotebookActivity extends android.app.Activity implements View.OnClickListener {
    private TextView tv_order_no;
    private ListView lv_open_params;
    private ListView lv_close_params;
    private ListView lv_time_log;
    private BdManager nh;
    public RequestParamsHandler handler = new RequestParamsHandler();
    private List<String> logs;
    private LogAdapter logAdapter;
    private Order order;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        nh = BdManager.getBd();
        nh.init(handler);
        ILog.setTagHandler(TIME_TAG, handler);
        OsModule.get().setHandler(handler);
        initView();
    }

    private void initView() {
        this.tv_order_no = (TextView) findViewById(R.id.tv_order_no);
        this.lv_open_params = (ListView) findViewById(R.id.lv_open_params);
        this.lv_close_params = (ListView) findViewById(R.id.lv_close_params);
        this.lv_time_log = (ListView) findViewById(R.id.lv_time_log);
        findViewById(R.id.download1).setOnClickListener(this);
        findViewById(R.id.download2).setOnClickListener(this);
        findViewById(R.id.order_succ).setOnClickListener(this);
        findViewById(R.id.order_fail).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BdManager.getBd().closeAllSerials();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download1:
                if (handler != null) {
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_WHAT_INITED;
                    handler.sendMessage(message);
                }
                nh.testOpen();
                break;
            case R.id.download2:
                nh.testClose();
                break;
            case R.id.order_succ:
                if (order != null) {
                    try {
                        OrderFileManager.getInstance().writeOrder(true, order);
                        Toast.makeText(this, "订单" + order.getOrder() + "写入成功", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "订单" + order.getOrder() + "写入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    order = null;
                }
                break;
            case R.id.order_fail:
                if (order != null) {
                    try {
                        OrderFileManager.getInstance().writeOrder(false, order);
                        Toast.makeText(this, "订单" + order.getOrder() + "写入成功", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "订单" + order.getOrder() + "写入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    order = null;
                }
                break;
        }
    }

    class RequestParamsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_WHAT_MESS:
                    String mess = (String) msg.obj;
                    updateLog(mess);
                    break;
                case HANDLER_MESSAGE_WHAT_PARMA:
                    if (msg.obj != null) {
                        if (msg.obj instanceof OpenParam) {
                            updateOpenParam((OpenParam) msg.obj);
                        } else if (msg.obj instanceof CloseParam) {
                            updateCloseParam((CloseParam) msg.obj);
                        }
                    }
                    break;
                case HANDLER_MESSAGE_WHAT_INITED:
                    initLogView();
                    break;
                case HANDLER_MESSAGE_WHAT_ORDER:
                    NotebookActivity.this.order = (Order) msg.obj;
                    break;
            }
        }
    }

    private void initLogView() {
        if (this.logs != null) {
            this.logs.clear();
        }
        if (this.logAdapter != null) {
            this.logAdapter.notifyDataSetChanged();
        }
    }

    private void updateLog(String mess) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(mess);
        if (this.logAdapter == null) {
            this.logAdapter = new LogAdapter(this.logs, this);
            this.lv_time_log.setAdapter(this.logAdapter);
        } else {
            this.logAdapter.notifyDataSetChanged();
        }
    }

    private void updateCloseParam(CloseParam param) {
        this.lv_close_params.setAdapter(new RetailInputParamAdapter(param.getRetailInputParams(), this));
        this.updateLog(" ");
    }

    @SuppressLint("SetTextI18n")
    private void updateOpenParam(OpenParam param) {
        this.lv_close_params.setAdapter(new RetailInputParamAdapter(new ArrayList<RetailInputParam>(), this));
        this.tv_order_no.setText("订单号 " + param.getOrder_no());
        this.lv_open_params.setAdapter(new RetailInputParamAdapter(param.getRetailInputParams(), this));
        this.updateLog(" ");
    }
}
