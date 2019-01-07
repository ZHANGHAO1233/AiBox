package com.mgr.serial.comn.bean;

/**
 * @author zhangh
 * @version 1.0.1
 */

public class SerialResult {
    private boolean succ;
    private int times;
    private double result;


    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public boolean isSucc() {
        return succ;
    }

    public void setSucc(boolean succ) {
        this.succ = succ;
    }

    public void updateSucc(double result) {
        this.succ = true;
        this.result = result;
    }

    public void updateFail() {
        this.succ = false;
        this.times += times;
    }
}
