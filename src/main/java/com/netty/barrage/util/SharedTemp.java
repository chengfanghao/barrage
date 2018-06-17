package com.netty.barrage.util;

public class SharedTemp {
    static private int num = 0;

    public static synchronized int getNum() {
        return num;
    }

    public static synchronized void setNum() {
        SharedTemp.num++;
    }
}
