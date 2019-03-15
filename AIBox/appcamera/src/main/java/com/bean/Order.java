package com.bean;

import org.json.JSONArray;

/**
 * @author zhangh
 * @version 1.0.1
 */
public class Order {
    private String order;
    private Integer wxId;
    private JSONArray products;
    private String mess;

    public Order(String order, Integer wxUserId) {
        this.order = order;
        this.wxId = wxUserId;
    }

    public String getOrder() {
        return order == null ? "" : order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public JSONArray getProducts() {
        return products;
    }

    public void setProducts(JSONArray products) {
        this.products = products;
    }

    public String getMess() {
        return mess == null ? "" : mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public Integer getWxId() {
        return wxId;
    }

    public void setWxId(Integer wxId) {
        this.wxId = wxId;
    }
}
