package com.pde.SocketClient;

import com.pde.SocketClient.GUI.IndexWindow;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

/**
 * 负责创建线程来完成上传和下载等数据传输
 */
public class DataTransfer implements Closeable {
    private Socket dataSocket;
    private static final SocketClient socketClient = SocketClient.getInstance();
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private final String dataTransferCode;
    private Thread running;

    public DataTransfer(String dataTransferCode) throws IOException {
        this.dataTransferCode = dataTransferCode;
        init(dataTransferCode);
    }

    private void init(String dataTransferCode) throws IOException {
        dataSocket = new Socket(socketClient.getHost(), socketClient.getDataPort());
        toServer = new DataOutputStream(dataSocket.getOutputStream());
        fromServer = new DataInputStream(dataSocket.getInputStream());
        toServer.writeUTF(dataTransferCode);
        if (!fromServer.readUTF().equals("SUCCESS")) {
            throw new ConnectException("数据端口连接失败");
        }

    }

    public void fileUpload(File sentFile, Long size) {

        count = size / 1024 + 1;

        SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
        SimpleStringProperty label = new SimpleStringProperty("");
        IndexWindow.showProgressBarWindow(this, "正在上传...", progress, label);
        running = new Thread() {
            @Override
            public void run() {
                super.run();

                InputStream fin = null;
                try {
                    System.out.println("文件开始上传");
                    fin = new FileInputStream(sentFile);
                    int length = 0;
                    byte[] bytes = new byte[1024];
                    int i = 0;
                    while ((length = fin.read(bytes, 0, bytes.length)) > 0) {
                        i++;
                        progress.set(i / (double) count);
                        label.set(sentFile.getName() + ":  " + i + "kb / " + count + "kb");
                        toServer.write(bytes, 0, length);
                        toServer.flush();
//                        System.out.println(i + " " + Thread.currentThread().getName());
                    }
                    dataSocket.close();
                    progress.set(1.0);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    SocketClient.safeClose(fin);
                }
                SocketClient.safeClose(fin);
                System.out.println("文件已上传");
                socketClient.getPath();
            }
        };
        running.start();
    }

    private long count = 0;
    //
    public void fileDownload(File saveFile, Long size) throws IOException {
        count = size / 1024 + 1;

        SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
        SimpleStringProperty label = new SimpleStringProperty("");
        IndexWindow.showProgressBarWindow(this, "正在下载...", progress, label);
        running = new Thread() {
            @Override
            public void run() {
                super.run();

                FileOutputStream fout = null;
                try {
                    System.out.println("文件开始下载");
                    fout = new FileOutputStream(saveFile);
                    int length = 0;
                    byte[] bytes = new byte[1024];

                    int i = 0;
                    while ((length = fromServer.read(bytes, 0, bytes.length)) > 0) {
                        progress.set(++i / (double) count);
                        label.set(saveFile.getName() + ":  " + i + "kb / " + count + "kb");
                        fout.write(bytes, 0, length);
                        fout.flush();
//                        System.out.println(i + " " + Thread.currentThread().getName());
                    }
                    progress.set(1.0);
                    fout.flush();
                } catch (IOException e) {
                    saveFile.delete();
                    e.printStackTrace();
                } finally {
                    SocketClient.safeClose(fout);
                }
                System.out.println("文件已下载");

            }
        };
        running.start();
    }

    public void onCancel() {
        socketClient.sentCancel(dataTransferCode);
        running.interrupt();
        running.stop();

    }

    @Override
    public void close() {
        try {
            if (!dataSocket.isClosed()) {
                if (!dataSocket.isInputShutdown()) {
                    dataSocket.shutdownInput();
                }
                if (!dataSocket.isOutputShutdown()) {
                    dataSocket.shutdownOutput();
                }
                dataSocket.close();
            }
        } catch (IOException ignored) {
        }
    }
}

