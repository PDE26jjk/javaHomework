package com.pde.SocketClient;


import com.pde.Serializable.S_Path;
import com.pde.SocketClient.GUI.IndexWindow;
import com.pde.SocketClient.GUI.LoginWindow;

import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  客户端最主要的类，负责登录、向服务端发送命令，创建DateTransfer对象完成文件数据传输
 */
public class SocketClient {

    private static SocketClient instance;

    private SocketClient() {
    }

    public static SocketClient getInstance() {
        if(instance == null){
            instance = new SocketClient();
        }
        return instance;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    private boolean loginState;

    public boolean isLogin() {
        return loginState;
    }

    private ExecutorService threadPool;
    private Socket socket;
    private DataOutputStream toServer;
    private DataInputStream fromServer;

    private final int commandPort = 9000;
    private final int dataPort = commandPort - 1;
    private final String host = "127.0.0.1";

    public int getCommandPort() {
        return commandPort;
    }

    public int getDataPort() {
        return dataPort;
    }

    public String getHost() {
        return host;
    }

    public boolean login(String userName, String pwd, String host, int commandPort) {
        threadPool = Executors.newSingleThreadExecutor();
        try {
            socket = new Socket(host, commandPort);

            toServer = new DataOutputStream(socket.getOutputStream());
            fromServer = new DataInputStream(socket.getInputStream());
            // TODO: 登录命令
            toServer.writeUTF("LOG_IN");
            toServer.writeUTF(userName);
            toServer.writeUTF(pwd);
            if (!fromServer.readUTF().equals("SUCCESS")) {
                throw new ConnectException("登录失败，账户或密码错误!");
            }
            toServer.flush();
            loginState = true;
            getPath();

        } catch (IOException e) {
            System.out.println("连接服务端失败，或已断开");
            return false;
        }
        Runnable runnable = () -> {
            try {
                while (socket.isConnected() && !socket.isClosed() && !socket.isOutputShutdown() && !socket.isInputShutdown()) {
                    toServer.writeUTF("ARE_YOU_OK");
                    Thread.sleep(1000 * 5);
                }
                System.out.println("连接服务端失败，或已断开");
                reLogin();
            } catch (InterruptedException ignored) {
            } catch (IOException e) {

                System.out.println("连接服务端失败，或已断开");
                reLogin();
            }

        };

        threadPool.submit(runnable);
        return true;
    }


    public boolean getPath() {
        // TODO: 获取根路径
        try {
            toServer.writeUTF("GET_FILES_PATH");

            if (!fromServer.readUTF().equals("SUCCESS")) {
                return false;
            }
            ObjectInputStream oji = new ObjectInputStream(fromServer);
            S_Path files = (S_Path) oji.readObject();

            IndexWindow.loadFileItemsFromSPath(files);
        } catch (Exception e) {
            reLogin();
        }
        return true;
    }

    public void onClose() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
        try {
            if (socket != null) {
                if (!socket.isInputShutdown()) {
                    socket.shutdownInput();
                }
                if (!socket.isOutputShutdown()) {
                    socket.shutdownOutput();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        safeClose(socket);
        safeClose(toServer);
    }

    // 上传文件
    public boolean uploadFile(String filePath, File sentFile) {
        try {
            toServer.writeUTF("UPLOAD");
            toServer.writeUTF(filePath);
            long size = sentFile.length();
            toServer.writeLong(size);
            toServer.flush();
            if (!fromServer.readUTF().equals("SUCCESS")) {
                System.out.println("文件已存在或者没有上传权限!!");
                return false;
            }

            String dataTransferCode = fromServer.readUTF();
            DataTransfer dataTransfer = new DataTransfer(dataTransferCode);
            dataTransfer.fileUpload(sentFile, size);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            reLogin();
            return false;
        }
    }

    // 发送取消命令
    public void sentCancel(String taskCode) {
        try {
            toServer.writeUTF("CANCEL");
            toServer.writeUTF(taskCode);
        } catch (IOException e) {
            e.printStackTrace();
            reLogin();
        }
    }

    // 登出，显示登录界面
    public void logout() {
        loginState = false;
        onClose();
        LoginWindow.login();
    }

    // 出了错要重新登录
    public void reLogin() {
        loginState = false;
        onClose();
        LoginWindow.reLogin();
    }

    // 下载文件
    public boolean downloadFile(String filePath, File saveFile) {
        long size = 0L;
        try {
            toServer.writeUTF("DOWNLOAD");
            toServer.writeUTF(filePath);
            toServer.flush();
            if (!fromServer.readUTF().equals("SUCCESS")) {
                return false;
            }
            size = fromServer.readLong();
            String dataTransferCode = fromServer.readUTF();
            DataTransfer dataTransfer = new DataTransfer(dataTransferCode);
            dataTransfer.fileDownload(saveFile, size);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            reLogin();
            return false;
        }
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean renameFile(String filePath, String newName) {
        try {
            toServer.writeUTF("RENAME");
            toServer.writeUTF(filePath);
            toServer.writeUTF(newName);
            toServer.flush();
            return fromServer.readUTF().equals("SUCCESS");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            getPath();
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            toServer.writeUTF("DELETE");
            toServer.writeUTF(filePath);
            toServer.flush();
            return fromServer.readUTF().equals("SUCCESS");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            getPath();
        }
    }

    public boolean createFolder(String newPath) {
        try {
            toServer.writeUTF("CREATE_PATH");
            toServer.writeUTF(newPath);
            toServer.flush();
            return fromServer.readUTF().equals("SUCCESS");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            getPath();
        }
    }


}
