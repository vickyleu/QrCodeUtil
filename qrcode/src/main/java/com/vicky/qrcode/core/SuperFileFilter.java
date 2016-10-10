package com.vicky.qrcode.core;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/29.
 */
public class SuperFileFilter extends javax.swing.filechooser.FileFilter {
    ArrayList<String> extension = new ArrayList<String>();
    String Description = "";

    public String getDescription() {
        return Description;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String name = file.getName();
        name = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        return extension.contains(name);
    }

    public void addExtension(String string) {
        extension.add(string);
    }

    public void setDescription(String string) {
        Description = string;
    }
}
