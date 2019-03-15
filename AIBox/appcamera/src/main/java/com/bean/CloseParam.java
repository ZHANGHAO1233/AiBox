package com.bean;

import com.baidu.retail.RetailInputParam;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangh
 * @version 1.0.1
 */

public class CloseParam implements Serializable {
    private List<RetailInputParam> retailInputParams;

    public CloseParam(List<RetailInputParam> retailInputParams) {
        this.retailInputParams = retailInputParams;
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
