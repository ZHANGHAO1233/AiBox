package com.bean;

/**
 * @author zhangh
 * @version 1.0.1
 */
public class Order {
    private String order;
    private String products;
    private String mess;

    public Order(String order, String products, String mess) {
        this.order = order;
        this.products = products;
        this.mess = mess;
    }

    public String getOrder() {
        return order == null ? "" : order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getProducts() {
        return products == null ? "" : products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getMess() {
        return mess == null ? "" : mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }
}
