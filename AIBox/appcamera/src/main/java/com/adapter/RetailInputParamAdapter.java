package com.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.retail.RetailInputParam;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.idata.aibox.R;
import com.lib.sdk.bean.StringUtils;
import com.mgr.serial.comn.util.GsonUtil;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

public class RetailInputParamAdapter extends BaseAdapter {
    private List<RetailInputParam> list;
    private Context context;

    public RetailInputParamAdapter(List<RetailInputParam> list, Context context) {
        super();
        this.list = list;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    LayoutInflater layoutInflater;

    final class ViewHolder {
        ImageView imv_camera;
        TextView tv_camera;
        TextView tv_weights;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RetailInputParam getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View contextView, ViewGroup viewGroup) {
        RetailInputParam param = getItem(position);
        ViewHolder viewHolder;
        if (contextView == null) {
            viewHolder = new ViewHolder();
            contextView = layoutInflater.inflate(R.layout.layout_item_param, null);
            viewHolder.tv_camera = (TextView) contextView.findViewById(R.id.tv_camera);
            viewHolder.tv_weights = (TextView) contextView.findViewById(R.id.tv_weights);
            viewHolder.imv_camera = (ImageView) contextView.findViewById(R.id.imv_camera);
            contextView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) contextView.getTag();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(param.getBitmap()!=null){
            param.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] bytes = baos.toByteArray();
            Glide.with(this.context).load(bytes).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .signature(new StringSignature(new Date().getTime() + "")).into(viewHolder.imv_camera);
        }else if(!StringUtils.isStringNULL(param.getFilePath())){
            Glide.with(this.context).load(param.getFilePath()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .signature(new StringSignature(new Date().getTime() + "")).into(viewHolder.imv_camera);
        }
        viewHolder.tv_camera.setText("摄像头编号：" + param.getMcastId());
        viewHolder.tv_weights.setText("重量集：" + GsonUtil.toJson(param.getGetWeightList()));
        return contextView;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
