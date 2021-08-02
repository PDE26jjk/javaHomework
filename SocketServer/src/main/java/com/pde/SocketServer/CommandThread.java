package com.pde.SocketServer;


import com.pde.SocketServer.Dao.Bean.User;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理命令socket的线程类，继承自Thread。对象与每次用户连接一一对应。将处理用户的各种命令，如上传、下载、删除等。
 */
public class CommandThread extends Thread {

    private final Socket socket;

    public boolean isClose() {
        return socket.isClosed();
    }

    private final static Map<String, DataTask> dataTransferTasks = new ConcurrentHashMap<>();

    public static void cleanupTask() {
        long nowTime = System.currentTimeMillis();
        Set<String> codes = new HashSet<>();
        for (Map.Entry<String, DataTask> commandEntry : dataTransferTasks.entrySet()) {
            DataTask dataTask = commandEntry.getValue();
            if (dataTask.status == DataTask.State.TRANSFERRING) {// 除了正在执行的任务
            } else if (dataTask.status == DataTask.State.WAITING &&
                    Math.abs(nowTime - dataTask.createTime) < 1000 * 10) {// 和等待未超过10秒的任务
            } else {
                codes.add(commandEntry.getKey());// 其他的删掉
            }
        }
        dataTransferTasks.keySet().removeAll(codes);
    }
//    private final static Map<String, CommandThread> CommandThreads = new ConcurrentHashMap<>();

    public static DataTask getCommandByTaskCode(String taskCode) {
        return dataTransferTasks.get(taskCode);
    }

    // 传入一个socket来创建线程
    public CommandThread(Socket socket) {
        this.socket = socket;
    }

    private User user;

    private static final FilesTools filesTools = FilesTools.getInstance();

    @Override
    public void run() {
//        System.out.println(Thread.currentThread().getName() + "已连接");
        DataInputStream fromClient = null;
        DataOutputStream toClient = null;
        try {
            fromClient = new DataInputStream(socket.getInputStream());
            toClient = new DataOutputStream(socket.getOutputStream());

            while (socket.isConnected() && !socket.isClosed() && !socket.isOutputShutdown() && !socket.isInputShutdown()) {
                // 如果命令socket连接未断开就不断地读命令，读到EOF就sleep一下
                String firstOrder = "";// 第一个指令
                try {
                    firstOrder = fromClient.readUTF();
                } catch (java.io.EOFException e) {
                    try {
                        sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    break;
                } catch (java.net.SocketException e) {
                    break;
                }
                switch (firstOrder) {
                    case "ARE_YOU_OK": // 心跳，保持连接用
                        break;
                    case "LOG_IN": {
                        String userName = fromClient.readUTF();
                        String pwd = fromClient.readUTF();
                        if (!UserTools.checkUserAndPassword(userName, pwd)) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + userName + "尝试登录失败,账户或密码错误");
                            throw new IOException();
                        }
                        System.out.println("用户" + userName + "登录");
                        user = UserTools.getUser(userName);

                        toClient.writeUTF("SUCCESS");

                        break;
                    }
                    case "GET_FILES_PATH": {
                        if (!UserTools.refreshUser(user).isA_browse()) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试浏览目录失败");
                            break;
                        }
                        toClient.writeUTF("SUCCESS");
                        // TODO: 2021-07-23 发送文件路径
                        ObjectOutputStream ojo = new ObjectOutputStream(toClient);
                        try {
                            filesTools.reloadFiles();
                            ojo.writeObject(filesTools.serializeFiles(filesTools.getFilePath()));
                        } catch (java.net.SocketException e) {
                            System.out.println("用户" + user.getName() + "路径接收错误");
                        }

                        break;
                    }

                    case "UPLOAD": {
                        String filePath = fromClient.readUTF();
                        Long size = fromClient.readLong();
                        // TODO: 2021-07-23 客户端上传处理
                        if (!UserTools.refreshUser(user).isA_upload()) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试上传失败");
                            break;
                        }
                        DataTask dataTask = filesTools.createCommand(filePath, DataTask.Type.SAVE, user);
                        if (null == dataTask) {
                            toClient.writeUTF("ERROR");
                            break;
                        }
                        toClient.writeUTF("SUCCESS");
                        String dataTransferCode = getRandomCode();
                        toClient.writeUTF(dataTransferCode);
                        dataTransferTasks.put(dataTransferCode, dataTask);
                        break;
                    }

                    case "DOWNLOAD": {
                        // TODO: 2021-07-23 客户端下载处理
                        if (!UserTools.refreshUser(user).isA_download()) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试下载失败");
                            break;
                        }
                        String filePath = fromClient.readUTF();
                        DataTask dataTask = filesTools.createCommand(filePath, DataTask.Type.SENT, user);
                        if (null == dataTask) {
                            toClient.writeUTF("ERROR");
                            break;
                        }
                        toClient.writeUTF("SUCCESS");
                        toClient.writeLong(dataTask.file.length());
                        String dataTransferCode = getRandomCode();
                        toClient.writeUTF(dataTransferCode);
                        dataTransferTasks.put(dataTransferCode, dataTask);
                        break;
                    }

                    case "CANCEL": {
//                        commandList.clear();
                        String taskCode = fromClient.readUTF();
                        DataTask dataTask = getCommandByTaskCode(taskCode);
                        if (dataTask != null) {
                            dataTask.status = DataTask.State.CANCELED;
                        }
                        break;
                    }

                    case "DELETE": {
                        if (!UserTools.refreshUser(user).isA_refactor()) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试删除失败，无权限");
                            break;
                        }
                        String filePath = fromClient.readUTF();
                        if (!filesTools.deleteFile(filePath)) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试删除失败，文件" + filePath + "占用");
                            break;
                        }
                        toClient.writeUTF("SUCCESS");
                        System.out.println("用户" + user.getName() + "删除文件" + filePath);
                        break;
                    }
                    case "RENAME": {
                        if (!UserTools.refreshUser(user).isA_refactor()) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试重命名失败，无权限");
                            break;
                        }
                        String filePath = fromClient.readUTF();
                        String newName = fromClient.readUTF();
                        if (!filesTools.renameFile(filePath, newName)) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试重命名失败，文件" + filePath);
                            break;
                        }
                        toClient.writeUTF("SUCCESS");
                        System.out.println("用户" + user.getName() + "重命名：" + filePath + "->" + newName);
                        break;
                    }
                    case "CREATE_PATH": {
                        if (!UserTools.refreshUser(user).isA_refactor()) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试创建目录失败，无权限");
                            break;
                        }
                        String dirPath = fromClient.readUTF();
                        if (!filesTools.createPath(dirPath)) {
                            toClient.writeUTF("ERROR");
                            System.out.println("用户" + user.getName() + "尝试创建目录失败");
                            break;
                        }
                        toClient.writeUTF("SUCCESS");
                        System.out.println("用户" + user.getName() + "创建目录：" + dirPath);
                        break;
                    }
                }
            }
//            System.out.println("接受3" + inputStream.readUTF());

        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            if (user != null) {
                System.out.println("用户" + user.getName() + "已断开");
            }
            safeClose(socket);
            safeClose(fromClient);
        }
    }

    /**
     * 这是生成10位随机字符串的方法，用于任务的随机码的生成。
     * @return 10位随机字符串
     */
    private String getRandomCode() {
        int Len = 10;
        String[] baseString = {"0", "1", "2", "3",
                "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o",
                "p", "q", "r", "s", "t",
                "u", "v", "w", "x", "y",
                "z", "A", "B", "C", "D",
                "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N",
                "O", "P", "Q", "R", "S",
                "T", "U", "V", "W", "X", "Y", "Z"};
        Random random = new Random();
        int length = baseString.length;
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            randomString.append(baseString[random.nextInt(length)]);
        }
        random = new Random(System.currentTimeMillis());
        StringBuilder resultStr = new StringBuilder();
        for (int i = 0; i < Len; i++) {
            resultStr.append(randomString.charAt(random.nextInt(randomString.length() - 1)));
        }
        return resultStr.toString();
    }
    // 安全关闭可关闭类
    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
