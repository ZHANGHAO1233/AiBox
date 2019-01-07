package com.notebook;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Curry on 2018/10/24.
 */

public class BdParam implements Comparable<BdParam> {
    private Bitmap bitmap;
    private int floor;
    private List<Double> weights;

    public BdParam(Bitmap var1, int var2, List<Double> weights) {
        this.bitmap = var1;
        this.floor = var2;
        this.weights = weights;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public int getFloor() {
        return this.floor;
    }

    public List<Double> getWeights() {
        if (weights == null) {
            weights = new ArrayList<>();
        }
        return weights;
    }

    public void setWeights(List<Double> weights) {
        this.weights = weights;
    }

    @Override
    public int compareTo(BdParam o) {
        return this.floor - o.floor;
    }
}
