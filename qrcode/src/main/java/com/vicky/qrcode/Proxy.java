package com.vicky.qrcode;

import com.vicky.qrcode.core.Auto_generate_condition;

public class Proxy {

    private static String HOST;

    public static Auto_generate_condition nameCondition() {
        return new Condition(HOST);
    }

    public static void setHOST(String HOSTs) {
        HOST = HOSTs;
    }
}
