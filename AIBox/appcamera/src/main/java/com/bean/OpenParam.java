package com.bean;

import com.baidu.retail.RetailInputParam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangh
 * @version 1.0.1
 */

public class OpenParam implements Serializable {
    private String order_no;
    private List<RetailInputParam> retailInputParams;

    public OpenParam(String order_no, List<RetailInputParam> retailInputParams) {
        this.order_no = order_no;
        this.retailInputParams = retailInputParams;
    }

    public String getOrder_no() {
        return order_no == null ? "" : order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public List<RetailInputParam> getRetailInputParams() {
        if (retailInputParams == null) {
            retailInputParams = new ArrayList<>();
        }
        return retailInputParams;
    }

    public void setRetailInputParams(List<RetailInputParam> retailInputParams) {
        this.retailInputParams = retailInputParams;
    }
}
