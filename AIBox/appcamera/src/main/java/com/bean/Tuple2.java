package com.bean;

public class Tuple2<T1, T2> {
    T1 value1;
    T2 value2;

    public Tuple2(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public static <R1, R2> Tuple2<R1, R2> createTuple2(R1 value1, R2 value2) {
        return new Tuple2(value1, value2);
    }

    public T1 getValue1() {
        return this.value1;
    }

    public void setValue1(T1 value1) {
        this.value1 = value1;
    }

    public T2 getValue2() {
        return this.value2;
    }

    public void setValue2(T2 value2) {
        this.value2 = value2;
    }
}
