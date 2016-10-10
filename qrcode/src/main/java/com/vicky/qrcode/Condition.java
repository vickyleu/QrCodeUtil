package com.vicky.qrcode;

import com.vicky.qrcode.core.Auto_generate_condition;

class Condition extends Auto_generate_condition {
    private String HOST;

    Condition(String HOST) {
        super(HOST);
        this.HOST = HOST;
    }

    @Override
    public String setQRCodeContent(String string, int position) {
        return HOST + format(string, position);
    }


    @Override
    public String getQRCodeHint(String messsage) {
        return super.getQRCodeHint(messsage);
    }

    public String setQRCodeHint(String string, int position) {
        return format(string, position);
    }
}
