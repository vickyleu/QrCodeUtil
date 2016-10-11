/*
 * $Id$
 *
 * Copyright 2013 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vicky.qrcode.gui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.vicky.qrcode.Proxy;
import com.vicky.qrcode.core.SuperFileFilter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * QR code generator.
 *
 * @author javadev
 * @version $Revision$ $Date$
 */

public class QrcodeGenerator extends javax.swing.JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 339128581185317395L;

    @SuppressWarnings("unused")
    private static final String ADDRESS_TEMPLATE = "BEGIN:VCARD\n" + "VERSION:3.0\n" + "N:{LN};{FN};\n"
            + "FN:{FN} {LN}\n" + "TITLE:{TITLE}/{COMPANYNAME}\n" + "TEL;TYPE=WORK;VOICE:{PHONE}\n"
            + "EMAIL;TYPE=WORK:{EMAIL}\n" + "ADR;TYPE=INTL,POSTAL,WORK:;;{STREET};{CITY};{STATE};{ZIP};{COUNTRY}\n"
            + "URL;TYPE=WORK:{WEBSITE}\n" + "END:VCARD";


    private static int compressLevel = 46;


    private JFileChooser chooser1 = new JFileChooser();
    private JFileChooser chooser2 = new JFileChooser();

    private String dirName = null;
    private File logoDirName = new File("res/logo.png");
    private String TAG = null;

    private JTextField inputFieldCount;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private ExecutorService thread;
    private BufferedImage imageAll;


    private class QRCodePanel extends javax.swing.JPanel {

        /**
         *
         */
        private static final long serialVersionUID = -6763979515063896435L;

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            if (imageAll != null) {
                grphcs.drawImage(imageAll, 0, 0, null);
            }
        }
    }


    /**
     * Creates new form
     */
    public QrcodeGenerator() {
        initComponents();
        XMLDecoder d;
        String x = null;
        String y = null;
        String height = null;
        String width = null;
        String index = null;
        try {
            d = new XMLDecoder(new BufferedInputStream(new FileInputStream("qrcode.xml")));
            compressLevel = Integer.parseInt((String) d.readObject());
            dirName = (String) d.readObject();
            TAG = (String) d.readObject();
            inputField.setText(new String(((String) d.readObject()).getBytes("ISO-8859-1"), "utf-8"));
            inputFieldHost.setText(new String(((String) d.readObject()).getBytes("ISO-8859-1"), "utf-8"));
            inputFieldCount.setText(new String(((String) d.readObject()).getBytes("ISO-8859-1"), "utf-8"));
            String img = (String) d.readObject();
            imgPath = ((img.length() != 0) ? img : "");

            System.out.println("能读取到图片吗?:" + imgPath);

            if (imgPath.length() != 0) {
                jButton1.setText("");
                jButton1.setText(imgPath);
            }
            x = (String) d.readObject();
            y = (String) d.readObject();
            height = (String) d.readObject();
            width = (String) d.readObject();
            index = (String) d.readObject();
            d.close();
        } catch (Exception ex) {
            ex.getMessage();
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent winEvt) {
                XMLEncoder e;
                try {
                    e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("qrcode.xml")));
                    e.writeObject(String.valueOf(compressLevel));
                    e.writeObject(dirName);
                    e.writeObject(TAG);
                    e.writeObject(inputField.getText());
                    e.writeObject(inputFieldHost.getText());
                    e.writeObject(inputFieldCount.getText());
                    String s = (imgPath != null && imgPath.length() != 0) ? imgPath : "";
                    System.out.println("存储的图片?:" + s);
                    e.writeObject(s);
                    e.writeObject("" + getLocation().x);
                    e.writeObject("" + getLocation().y);
                    e.writeObject("" + getSize().height);
                    e.writeObject("" + getSize().width);
                    e.writeObject("" + labelTitle.getSelectedIndex());
                    e.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(QrcodeGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
        });

        SuperFileFilter filter = new SuperFileFilter();
        filter.addExtension("png");
        filter.addExtension("gif");
        filter.setDescription("png和gif图片才能选择");

        chooser2.addChoosableFileFilter(filter);
        chooser2.setDialogTitle("请选择二维码logo");

        if (imgPath == null) {
            chooser2.setCurrentDirectory(new File("."));
        } else {
            File chooser2File = new File(imgPath);
            if (!chooser2File.exists()) {
                chooser2.setCurrentDirectory(new File("."));
            } else {
                chooser2.setCurrentDirectory(new File(chooser2File.getParent()));
            }
        }


        chooser2.setAcceptAllFileFilterUsed(false);

        chooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        chooser1.setDialogTitle("请选择二维码输出目录");


        if (dirName == null) {
            chooser1.setCurrentDirectory(new File("/output"));
        } else {
            File chooser1File = new File(dirName);
            if (!chooser1File.exists()) {
                chooser1.setCurrentDirectory(new File("/output"));
            } else {
                chooser1.setCurrentDirectory(chooser1File);
            }
        }


        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if (x == null) {
            x = "" + ((screenSize.width - getWidth()) / 2);
        }
        if (y == null) {
            y = "" + ((screenSize.height - getHeight()) / 2);
        }
        if (height == null) {
            height = "" + getPreferredSize().height;
        }
        if (width == null) {
            width = "" + getPreferredSize().width;
        }
        if (index == null) {
            index = "" + labelTitle.getSelectedIndex();
        }
        setLocation(Integer.valueOf(x), Integer.valueOf(y));
        setSize(new Dimension(Integer.valueOf(width), Integer.valueOf(height)));
        labelTitle.setSelectedIndex(Integer.valueOf(index));
        inputLevel.setText(String.valueOf(compressLevel));
        if (inputField.getText().length() != 0
                && inputFieldCount.getText().length() != 0
                && !inputFieldCount.getText().equals("0")) {
            //// TODO: 2016/9/30
        } else {
            jButton2.setEnabled(false);
            jButton3.setEnabled(false);
        }

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new QRCodePanel();
        labelTitle = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        inputField = new javax.swing.JTextField();
        inputLevel = new javax.swing.JTextField();
        inputFieldHost = new javax.swing.JTextField();
        inputFieldCount = new javax.swing.JTextField();


        label1 = new JLabel("基数:");
        label2 = new JLabel("生成次数:");
        label3 = new JLabel("前缀数据:");
        label4 = new JLabel("压缩等级:(1~100可选范围整数)");


        jButton2 = new javax.swing.JButton();

        jButton3 = new javax.swing.JButton();


        
        jButton1 = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("超级工厂二维码生成器");
        // setSize(1800, 600);

        setMinimumSize(new java.awt.Dimension(800, 700));

        setMaximumSize(new java.awt.Dimension(800, 700));
        setResizable(false);
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setPreferredSize(new java.awt.Dimension(350, 350));
        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(0, 208, Short.MAX_VALUE));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0,
                208, Short.MAX_VALUE));

        labelTitle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane6StateChanged(evt);
            }
        });

        inputField.setFont(new java.awt.Font("宋体", 0, 12)); // NOI18N
        inputFieldCount.setFont(new java.awt.Font("宋体", 0, 12));
        inputField.setText("");

        inputLevel.setFont(new java.awt.Font("宋体", 0, 12)); // NOI18N
        inputLevel.setText("");


        inputLevel.setText("46");

        inputFieldHost.setFont(new java.awt.Font("宋体", 0, 12)); // NOI18N
        inputFieldHost.setText("");


        inputFieldCount.setText("1");
        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (inputField.getText().length() == 0) {
                    inputField.setText("超级工厂二维码生成器");
                }
            }
        });

        inputLevel.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                super.keyReleased(evt);

                String ic = inputLevel.getText();
                if (ic.length() != 0 && ic.length() < 3) {
                    if (ic.substring(0, 1).equals("0")) {
                        ic = ic.substring(1, ic.length());
                        inputLevel.setText("");
                        inputLevel.setText(ic);
                    }
                }
                if (ic.length() > 3) {
                    inputLevel.setText("");
                    inputLevel.setText(ic.substring(0, 3));
                }

                if (ic.length()==3){
                    if (Integer.parseInt(inputLevel.getText()) > 100) {
                        inputLevel.setText("");
                        inputLevel.setText(ic.substring(0, 2));
                    }
                }


                if (inputLevel.getText().length() != 0 &&! inputLevel.getText().equals("0")) {
                    compressLevel = Integer.parseInt(inputLevel.getText());
                } else {
                    compressLevel = 46;

                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if (keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) {

                } else {
                    e.consume();
                }
            }
        });


        inputField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                super.keyReleased(evt);
                if (inputField.getText().length() != 0
                        && inputFieldCount.getText().length() != 0
                        && !inputFieldCount.getText().equals("0")) {
                    jButton2.setEnabled(true);
                    jButton3.setEnabled(true);
                } else {
                    jButton2.setEnabled(false);
                    jButton3.setEnabled(false);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if (keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) {

                } else {
                    e.consume();
                }
            }
        });
        // 键盘事件,屏蔽
        inputFieldCount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                super.keyReleased(evt);
                if (inputField.getText().length() != 0
                        && inputFieldCount.getText().length() != 0
                        && !inputFieldCount.getText().equals("0")) {
                    jButton2.setEnabled(true);
                    jButton3.setEnabled(true);
                } else {
                    jButton2.setEnabled(false);
                    jButton3.setEnabled(false);
                }

                String ic = inputFieldCount.getText();
                if (ic.length() != 0 && ic.length() < 3) {
                    if (ic.substring(0, 1).equals("0")) {
                        ic = ic.substring(1, ic.length());
                        inputFieldCount.setText("");
                        inputFieldCount.setText(ic);
                    }
                }
                if (ic.length() > 4) {
                    inputFieldCount.setText("");
                    inputFieldCount.setText(ic.substring(0, 4));
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if (keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) {

                } else {
                    e.consume();
                }
            }
        });

        org.jdesktop.layout.GroupLayout inputArea = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(inputArea);
        inputArea.setHorizontalGroup(

                inputArea.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(inputArea.createSequentialGroup().
                                addContainerGap().add(label1)).add(inputField, 200, 200, 200)
                        .add(inputArea.createSequentialGroup().
                                addContainerGap().add(label2)).add(inputFieldCount, 50, 50, 50)
                        .add(inputArea.createSequentialGroup().
                                addContainerGap()
                                .add(label3)).add(inputFieldHost, 150, 150, 150)
                        .add(inputArea.createSequentialGroup().
                                addContainerGap()
                                .add(label4)).add(inputLevel, 50, 50, 50)
        );


        inputArea.setVerticalGroup(inputArea.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(inputArea.createSequentialGroup().addContainerGap()
                        .add(label1).add(inputField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(label2).add(inputFieldCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(label3).add(inputFieldHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(label4).add(inputLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                )

        );

        labelTitle.addTab("Designed by Vicky", jPanel4);

        jButton2.setText("批量生成");
        jButton2.addActionListener(new java.awt.event.ActionListener()

        {
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                if (jButton2.getText().equals("批量生成")) {
                    final int count = Integer.valueOf(inputFieldCount.getText());
                    TAG = inputFieldHost.getText();
                    Proxy.setHOST(TAG);
                    auto_generate(inputField.getText(), count);
                } else {
                    if (thread != null) {
                        List<Runnable> list = thread.shutdownNow();
                        for (int i = 0; i < list.size(); i++) {
                            ((Runnablex) list.get(i)).stop();
                        }

                        thread.shutdown();
                    }
                    jButton2.setText("");
                    jButton2.setText("批量生成");
                }
            }
        });

        if (TAG != null && TAG.length() != 0)

        {
            Proxy.setHOST(TAG);
        } else

        {
            Proxy.setHOST("");
        }


        jButton3.setText("生成单张");
        jButton3.addActionListener(new java.awt.event.ActionListener()

        {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TAG = inputFieldHost.getText();
                Proxy.setHOST(TAG);
                once_generate(inputField.getText());
            }
        });

        jButton1.setText("选择logo");
        jButton1.addActionListener(new java.awt.event.ActionListener()

        {
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                // logo图片
                // TODO
                if (chooser2.showOpenDialog(QrcodeGenerator.this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                String str = chooser2.getSelectedFile().getPath();
                jButton1.setText("");
                jButton1.setText(str);
                System.out.println("选择的png图片" + str);
                imgPath = str;
                // jButton1ActionPerformed(evt);
            }
        });
        //
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .

                        add(jPanel1Layout.createSequentialGroup().

                                addContainerGap()
                                .

                                        add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .

                                                        add(jPanel1Layout.createSequentialGroup()
                                                                .

                                                                        add(jButton1, 82, 82, 82)
                                                                .

                                                                        addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .

                                                                        add(jButton3, 82, 82, 82)
                                                                .

                                                                        add(jButton2,
                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124,
                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .

                                                        add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .

                                        addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .

                                        add(labelTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                                .

                                        addContainerGap()));


        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .

                        add(jPanel1Layout.createSequentialGroup().

                                addContainerGap()
                                .

                                        add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).

                                                add(labelTitle)
                                                .

                                                        add(jPanel1Layout.createSequentialGroup()
                                                                .

                                                                        add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                                .

                                                                        add(11, 11, 11)
                                                                .

                                                                        add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                                                .

                                                                                        add(jButton2).

                                                                                        add(jButton3).

                                                                                        add(jButton1))
                                                                .

                                                                        add(0, 308, Short.MAX_VALUE)))
                                .

                                        addContainerGap()));
        //
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());

        getContentPane().

                setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).

                add(jPanel1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).

                add(jPanel1,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE));

        pack();

    }


    /**
     * 在二维码中间加入图片
     * logo图片
     *
     * @param imgPath
     * @return
     */

    private void createPhotoAtCenter(Graphics2D graphics, String imgPath, BufferedImage image) throws Exception {

        if (imgPath == null || imgPath.length() == 0) {
            System.err.println("不使用logo");
            return;
        }

        if (!imgPath.toLowerCase().endsWith(".png") && !imgPath.toLowerCase().endsWith(".gif")) {
            System.err.println("错误的图片类型");
            return;
        }
        File file = new File(imgPath);
        if (!file.exists()) {
            System.err.println("" + imgPath + "   该文件不存在！");
            return;
        }


        BufferedImage logo = ImageIO.read(file);

        int logoHeight = (int) (image.getWidth() * 0.18);
        logo = getScaledImage(logo, logoHeight, logoHeight);

        int first = ((image.getHeight() / 2 - image.getHeight() / 13) - logo.getHeight() / 2);
        int last = (image.getHeight() / 2 - image.getHeight() / 13) + logo.getHeight() / 2;
//        int last = (image.getHeight() / 2 - image.getHeight() / 13) + logo.getHeight() / 2 + lastY;

//        System.out.println("坐标轴:"+last);
        graphics.drawImage(logo,
                image.getWidth() / 2 - logo.getWidth() / 2,
                first,
                image.getWidth() / 2 + logo.getWidth() / 2,
                last,
                0, 0,
                logoHeight,
                logoHeight,
                null);


    }


    /**
     * Scale image to required size
     */
    private BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(
                scaleTransform, AffineTransformOp.TYPE_BILINEAR);

        return bilinearScaleOp.filter(
                image,
                new BufferedImage(width, height, image.getType()));
    }


    /**
     * 自动生成
     */
    private void auto_generate(final String string, final int length) {

        final List<BufferedImage> bufferedImageList = new ArrayList<BufferedImage>();
        if (dirName != null && dirName.length() != 0) {
            if (chooser1.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            dirName = chooser1.getSelectedFile().getPath();
        } else {
            if (chooser1.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            dirName = chooser1.getSelectedFile().getPath();
        }


        final File parent = new File(dirName);
        if (parent.isDirectory()) {
            for (File file2 : parent.listFiles()) {
                file2.delete();
            }
        }

        jButton3.setEnabled(false);
        jButton1.setEnabled(false);
        jButton2.setText("");
        jButton2.setText("停止批量处理");
        if (thread != null) {
            thread.shutdown();
            thread = null;
        }
        thread = Executors.newCachedThreadPool();
        thread.submit(new Runnablex() {

            @Override
            public void stop() {
                super.stop();
            }

            public void run() {
                for (int i = 0; i < length && flag; i++) {

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                        jButton3.setEnabled(true);
                        jButton1.setEnabled(true);
                        jButton2.setText("批量生成");
                        e.printStackTrace();
                    }
                    inputField.setText("");
                    inputField.setText(Proxy.nameCondition().setQRCodeHint(string, i));
                    try {
                        bufferedImageList.add(generateQrCode(Proxy.nameCondition().setQRCodeContent(string, i)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        jButton3.setEnabled(true);
                        jButton1.setEnabled(true);
                        jButton2.setText("批量生成");
                    }
                }


//                List<String> list = new ArrayList<String>();
//                for (File file2 : parent.listFiles()) {
//                    list.add(file2.getAbsolutePath());
//                }
                mergePic(bufferedImageList, parent.getAbsolutePath() + "/merge.jpg", flag);
//                mergePic(bufferedImageList, parent.getAbsolutePath() + "/merge.png", flag);

                for (File file2 : parent.listFiles()) {
                    if (!file2.getName().equals("merge.jpg")) {
                        file2.delete();
                    }
                }
                jButton3.setEnabled(true);
                jButton1.setEnabled(true);
                jButton2.setText("批量生成");
            }
        });
    }


    /**
     * 只生成一次
     *
     * @param string
     */
    private void once_generate(final String string) {

        if (!logoDirName.exists()) {
            return;
        }

        if (dirName != null && dirName.length() != 0) {
            if (chooser1.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            dirName = chooser1.getSelectedFile().getPath();
        } else {
            if (chooser1.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            dirName = chooser1.getSelectedFile().getPath();
        }

        final File parent = new File(dirName);
        if (parent.isDirectory()) {
            for (File file2 : parent.listFiles()) {
                if (file2 != null) file2.delete();
            }
        }


        jButton3.setEnabled(false);
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);

        new Thread(new Runnable() {

            public void run() {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                    jButton3.setEnabled(true);
                    jButton1.setEnabled(true);
                    jButton2.setEnabled(true);
                    e.printStackTrace();
                }
                inputField.setText("");
                inputField.setText(Proxy.nameCondition().setQRCodeHint(string, 0));
                try {
                    generateQrCode(Proxy.nameCondition().setQRCodeContent(string, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                    jButton3.setEnabled(true);
                    jButton1.setEnabled(true);
                    jButton2.setEnabled(true);
                }
                jButton3.setEnabled(true);
                jButton1.setEnabled(true);
                jButton2.setEnabled(true);
            }
        }).start();

    }


    private void jTabbedPane6StateChanged(javax.swing.event.ChangeEvent evt) {
        javax.swing.JTabbedPane jt = (javax.swing.JTabbedPane) evt.getSource();
        switch (jt.getSelectedIndex()) {
            case 0:
                try {
                    generateQrCode(inputField.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // URL页面

                break;
        }
    }

    private static void setLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException {
        javax.swing.UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();
        String firstFoundClass = null;
        for (javax.swing.UIManager.LookAndFeelInfo info : infos) {
            String foundClass = info.getClassName();
            if ("Nimbus".equals(info.getName())) {
                firstFoundClass = foundClass;
                break;
            }
            if (null == firstFoundClass) {
                firstFoundClass = foundClass;
            }
        }

        if (null == firstFoundClass) {
            throw new IllegalArgumentException("No suitable Swing looks and feels");
        } else {
            UIManager.setLookAndFeel(firstFoundClass);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        setLookAndFeel();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new QrcodeGenerator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;

    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;

    private javax.swing.JTabbedPane labelTitle;

    private javax.swing.JTextField inputField;
    private javax.swing.JTextField inputLevel;
    private javax.swing.JTextField inputFieldHost;

    private String imgPath = null;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BufferedImage generateQrCode(String messsage) throws Exception {
        BufferedImage image = null;
//        Logger.getLogger(QrcodeGenerator.class.getName()).log(Level.INFO, messsage);
        if (messsage == null || messsage.length() == 0) {
            image = null;
            return null;
        }
        try {

            // 生成二维码图片
            Hashtable hintMap = new Hashtable();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);// 容错率H最高
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // 生成矩阵
            BitMatrix byteMatrix = qrCodeWriter.encode(messsage, BarcodeFormat.QR_CODE,
                    jPanel3.getPreferredSize().width, jPanel3.getPreferredSize().height, hintMap);

            int CrunchifyWidth = byteMatrix.getWidth();

            /**
             *
             */
            try {
                image = resetImg(CrunchifyWidth,
                        new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB), byteMatrix);
                if (image == null) {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
            }
            /**
             *
             */

            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            //
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {


//						unusedMethod(graphics,image, i, j);

                        graphics.fillRect(i, j - CrunchifyWidth / 13, 1, 1);
                    }
                }
            }

            graphics.drawRect(CrunchifyWidth, CrunchifyWidth, CrunchifyWidth, CrunchifyWidth / 10);

            messsage = "No." + Proxy.nameCondition().getQRCodeHint(messsage);

            BufferedImage logo = ImageIO.read(logoDirName);
            int logoWidth = (int) (CrunchifyWidth * 0.75);
            int logoHeight = (int) (CrunchifyWidth * 0.12);
            logo = getScaledImage(logo, logoWidth, logoHeight);
            PaintArea area = new PaintArea((CrunchifyWidth / 2) - (logoWidth / 2), CrunchifyWidth - ((CrunchifyWidth / 15) + logoHeight));

            System.out.println("==" + area.getStartX() + "==" + area.getStartY());

            graphics.drawImage(logo,
                    area.getStartX(),
                    area.getStartY(),
                    area.getStopX(logoWidth),
                    area.getStopY(logoHeight),
                    0, 0,
                    logoWidth, logoHeight, null);


            drawString(graphics, messsage, CrunchifyWidth, (CrunchifyWidth - CrunchifyWidth / 7) + logoHeight, TextAlign.Right);

            // 插入logo
            createPhotoAtCenter(graphics, imgPath, image);

            imageAll = image;
            // 面板绘制
            jPanel3.repaint();

            if (dirName == null || dirName.toString().length() == 0) {
                return logo;
            }
            String fileName = dirName + "/" + messsage + ".png";
            File file = new File(fileName);
            if (!file.exists()) {
                file.mkdirs();
            }

            try {
                ImageIO.write(image, "png", file);
            } catch (IOException ex) {
                Logger.getLogger(QrcodeGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (WriterException ex) {
            Logger.getLogger(QrcodeGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return image;
    }


    /**
     * 将宽度相同的图片，竖向追加在一起 ##注意：宽度必须相同
     *
     * @param piclist 文件路径列表
     * @param outPath 输出路径
     * @param flag
     */
    public static void mergePic(List<BufferedImage> piclist, String outPath, boolean flag) {// 纵向处理图片
        if (!flag) return;
        if (piclist == null || piclist.size() <= 0) {
            System.out.println("图片数组为空!");
            return;
        }
        try {
            int width = 0, // 总宽度
                    picNum = piclist.size();// 图片的数量
//            File fileImg = null; // 保存读取出的图片
            int totalCount = 5;


            BufferedImage buffer = null; // 保存图片流
            List<int[]> imgRGB = new ArrayList<int[]>(); // 保存所有的图片的RGB
            for (int i = 0; i < picNum; i++) {
//                fileImg = new File(piclist.get(i));
                buffer = piclist.get(i);//ImageIO.read(fileImg);
                buffer = compress(buffer, (int) (buffer.getWidth() * (compressLevel * 0.01)), (int) (buffer.getHeight() * (compressLevel * 0.01)));//压缩图片宽高
                if (i == 0) width = buffer.getWidth();// 图片宽度
                int[] abc = buffer.getRGB(0, 0, width, width, new int[width * width], 0, width);
                imgRGB.add(abc);
            }
            int num = 0;
            if (picNum % totalCount == 0) {
                num = picNum / totalCount;
            } else {
                num = (picNum + (totalCount - (picNum % totalCount))) / totalCount;
            }
            // 生成新图片
            BufferedImage imageResult = new BufferedImage(width * totalCount, width * num, BufferedImage.TYPE_INT_RGB);


            System.out.println("整图宽高:" + imageResult.getWidth() + "===" + imageResult.getHeight());
            try {
                whiteBackGround(imageResult);//白色背景
            } catch (Exception e) {
                e.printStackTrace();
            }

            int count = 0;
            itemView(width, picNum, totalCount, imgRGB, imageResult, count, flag);
//            File outFile = new File(outPath);


            FileOutputStream out = new FileOutputStream(outPath);    // 将图片写入 newPath
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(imageResult);
            jep.setQuality(1f, true);    //压缩质量, 1 是最高值
            encoder.encode(imageResult, jep);
            out.close();


//            ImageIO.write(imageResult, "png", outFile);// 写图片
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩图片
     *
     * @param sourceImage
     * @param newImageWidth
     * @param newImageHeight
     * @return
     */
    private static BufferedImage compress(BufferedImage sourceImage, int newImageWidth, int newImageHeight) {
        // 得到源图的宽度
        int sourceImageWidth = sourceImage.getWidth(null);
        // 得到源图的高度
        int sourceImageHeight = sourceImage.getHeight(null);
        // 定义新的图片的宽度以及高度
        int newWidth = 0;
        int newHeight = 0;
        // 计算源图与所需要生成的新图的长宽比例
        double widthPercent = (sourceImageWidth * 1.00)
                / (newImageWidth * 1.00);
        double heightPercent = (sourceImageHeight * 1.00)
                / (newImageHeight * 1.00);

//        //判断是否需要对源图进行调整
//        if(isReviseSourceImage == true) {
//            sourceImage = revise(sourceImage, newImageBackcolor);
//        }

        // 计算新图长
        if (sourceImageWidth > newImageWidth) {
            newWidth = (int) Math.round(sourceImageWidth / widthPercent);
        } else {
            newWidth = sourceImageWidth;
        }
        // 计算新图宽
        if (sourceImageHeight > newImageHeight) {
            newHeight = (int) Math.round(sourceImageHeight / heightPercent);
        } else {
            newHeight = sourceImageHeight;
        }

        BufferedImage newImageBufferedImage = new BufferedImage(newWidth, newHeight,
                BufferedImage.TYPE_INT_RGB);
        // 绘制缩小后的图
        newImageBufferedImage.getGraphics().drawImage(
                sourceImage.getScaledInstance(newWidth, newHeight,
                        Image.SCALE_SMOOTH), 0, 0, null);
        return newImageBufferedImage;
    }

    private static void itemView(int width, int picNum, int totalCount, List<int[]> imgRGB, BufferedImage imageResult, int count, boolean flag) {
        PaintArea area = new PaintArea(0, 0);
        for (int i = 0; i < picNum && flag; i++) {
            if (i != 0) {
                area.setStartX(area.getStartX() + width);
            }
            if (count == totalCount) {
                count = 0;
                area.setStartY(area.getStopY(width));
                area.setStartX(0);
            }

            try {
                imageResult.setRGB(area.getStartX(), area.getStartY(), width,
                        width, imgRGB.get(i), 0, width);
            } catch (Exception e) {
                System.out.println("第" + (i + 1) + "个itemView生成失败,原因是:" + e.getMessage());
                e.printStackTrace();
            }
            count++;
        }
    }

    private static void whiteBackGround(BufferedImage imageResult) throws Exception {
        int[] totalRgb = null;
        int count = 0;

        Rectangle bound = imageResult.getData().getBounds();

        Dimension dm = bound.getSize();

        int height = (int) dm.getWidth();
        int width = (int) dm.getWidth();

        for (int i = 0; i < width * 2; i++) {
            for (int j = 0; j < height; j++) {
                count++;
            }
        }
        totalRgb = new int[count];
        for (int i = 0; i < count; i++) {
            totalRgb[i] = new Color(0xFFFFFF).getRGB();
        }
        imageResult.setRGB(0, 0, imageResult.getWidth(),
                imageResult.getHeight()
                , totalRgb, 0,
                (int) Math.sqrt(imageResult.getWidth() * imageResult.getHeight())
        );
    }


    private void unusedMethod(Graphics2D graphics, BufferedImage image, int i, int j) {
        // 图片的像素点其实是个矩阵，这里利用两个for循环来对每个像素进行操作
        Object data = image.getRaster().getDataElements(i, j, null);// 获取该点像素，并以object类型表示
        int red = image.getColorModel().getRed(data);
        int blue = image.getColorModel().getBlue(data);
        int green = image.getColorModel().getGreen(data);

        System.out.println(
                "颜色值===" + red + "==" + green + "==" + blue);
        red = (red * 3 + green * 6 + blue * 1) / 10;
        green = red;
        blue = green;
        /*
         * 这里将r、g、b再转化为rgb值，因为bufferedImage没有提供设置单个颜色的方法，只能设置rgb
		 * 。rgb最大为8388608，当大于这个值时，应减去255*255*255即16777216
		 */


        int rgb = (red * 256 + green) * 256 + blue;
        if (rgb > 8388608) {
            rgb = rgb - 16777216;
        }

        Color rgbColor = new Color(rgb);

        System.out.println(
                "颜色值" + rgbColor.getRed() + "==" + rgbColor.getGreen() + "==" + rgbColor.getBlue());
        graphics.setColor(rgbColor);
    }


    private BufferedImage resetImg(int crunchifyWidth, BufferedImage image, BitMatrix matrix) {

        image.createGraphics();
        int IMAGE_HALF_WIDTH = crunchifyWidth / 2;
        int FRAME_WIDTH = 2;

        int[][] srcPixels = new int[crunchifyWidth][crunchifyWidth];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                srcPixels[i][j] = image.getRGB(i, j);
            }
        }

        // 二维矩阵转为一维像素数组
        int halfW = matrix.getWidth() / 2;
        int halfH = matrix.getHeight() / 2;
        int[] pixels = new int[image.getWidth() * image.getWidth()];
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                // 左上角颜色,根据自己需要调整颜色范围和颜色
                if (x > 0 && x < 170 && y > 0 && y < 170) {
                    Color color = new Color(231, 144, 56);
                    int colorInt = color.getRGB();
                    pixels[y * image.getWidth() + x] = matrix.get(x, y) ? colorInt : 16777215;
                }
                // 读取图片
                else if (x > halfW - IMAGE_HALF_WIDTH && x < halfW + IMAGE_HALF_WIDTH && y > halfH - IMAGE_HALF_WIDTH
                        && y < halfH + IMAGE_HALF_WIDTH) {
                    pixels[y * image.getWidth() + x] = srcPixels[x - halfW + IMAGE_HALF_WIDTH][y - halfH
                            + IMAGE_HALF_WIDTH];
                } else if ((x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH && x < halfW - IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH + IMAGE_HALF_WIDTH + FRAME_WIDTH)
                        || (x > halfW + IMAGE_HALF_WIDTH - FRAME_WIDTH && x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && y < halfH + IMAGE_HALF_WIDTH + FRAME_WIDTH)
                        || (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH && x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && y < halfH - IMAGE_HALF_WIDTH + FRAME_WIDTH)
                        || (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH && x < halfW + IMAGE_HALF_WIDTH + FRAME_WIDTH
                        && y > halfH + IMAGE_HALF_WIDTH - FRAME_WIDTH
                        && y < halfH + IMAGE_HALF_WIDTH + FRAME_WIDTH)) {
                    pixels[y * image.getWidth() + x] = 0xfffffff;
                    // 在图片四周形成边框
                } else {
                    // 二维码颜色
                    int num1 = (int) (50 - (50.0 - 13.0) / matrix.getHeight() * (y + 1));
                    int num2 = (int) (165 - (165.0 - 72.0) / matrix.getHeight() * (y + 1));
                    int num3 = (int) (162 - (162.0 - 107.0) / matrix.getHeight() * (y + 1));
                    Color color = new Color(num1, num2, num3);
                    int colorInt = color.getRGB();
                    // 此处可以修改二维码的颜色，可以分别制定二维码和背景的颜色；
                    pixels[y * image.getWidth() + x] = matrix.get(x, y) ? colorInt : 16777215;
                    // 0x000000:0xffffff
                }
            }
        }
        BufferedImage image2 = new BufferedImage(image.getWidth(), image.getWidth(), BufferedImage.TYPE_INT_RGB);
        image2.getRaster().setDataElements(0, 0, image.getWidth(), image.getWidth(), pixels);
        return image2;
    }


    @SuppressWarnings("Since15")
    private void drawString(Graphics2D g, String str, int xPos, int yPos, TextAlign ta) {

        g.setColor(Color.BLACK);
        Font font = new Font("微软雅黑", Font.PLAIN, 17);
        if (font != null)
            g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        int strWidth = g.getFontMetrics().stringWidth(str);

        int all = xPos;
        int baseNum = 7;
        switch (ta) {
            case Center:
                g.drawString(str, xPos / 2 - strWidth / 2, yPos);
                break;
            case Left:
                g.drawString(str, all / baseNum, yPos);
                break;
            case Right:
                g.drawString(str, (all - all / baseNum) - strWidth, yPos);
                break;
        }
    }

    enum TextAlign {
        Left, Right, Center
    }


    private static class PaintArea {
        int startX;
        int startY;


        void setStartX(int startX) {
            this.startX = startX;
        }

        void setStartY(int startY) {
            this.startY = startY;
        }

        PaintArea(int startX, int startY) {
            this.startX = startX;
            this.startY = startY;
        }

        int getStartX() {
            return startX;
        }

        int getStartY() {
            return startY;
        }

        int getStopX(int sum) {
            return startX + sum;
        }

        int getStopY(int sum) {
            return startY + sum;
        }
    }
}
