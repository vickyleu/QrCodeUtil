package com.vicky.qrcode.core;

public abstract class Auto_generate_condition {

    private String host;

    public Auto_generate_condition(String host) {
        this.host = host;
    }

    public abstract String setQRCodeContent(String string, int position);

    public abstract String setQRCodeHint(String string, int position);

    public String getQRCodeHint(String messsage) {
        if (messsage.contains(host)) {
            return messsage.replace(host, "");
        } else {
            return messsage;
        }
    }

    protected String format(String string, int position) {
        return String.valueOf(Integer.parseInt(string) + position);
    }


}
