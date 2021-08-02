package com.pde.SocketServer;

import com.pde.SocketServer.GUI.IndexWindow;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务端主要的类，提供init方法，启动对两个端口的监听，分派到不同的线程执行（使用线程池），
 * 也启动一个线程去定时刷新映射的文件夹的路径信息，一个线程去清理任务列表中的过时任务
 * 也提供shutdown方法去关闭监听的线程和结束未完成的任务
 */
public class SocketServer {

    private static volatile boolean isRunning;

    private static int commandPort;
    private static int dataPort;
    private static final List<ServerSocket> serverSockets = new ArrayList<>();

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    private static ExecutorService threadPool;

    public static boolean isClosed() {
        if (threadPool == null) return true;
        return threadPool.isShutdown();
    }

    public static synchronized void init(String[] args)   {
        isRunning = true;
        FilesTools filesTools = FilesTools.getInstance();
        ServerSocket commandServer = null;
        ServerSocket dataServer = null;
        try{
            commandServer = new ServerSocket(commandPort);
            dataServer = new ServerSocket(dataPort);
            // 放入列表，便于统一关闭
            serverSockets.add(commandServer);
            serverSockets.add(dataServer);
        }
        catch (BindException e){
            System.out.println(getPort() +"端口已被使用！");
            if(IndexWindow.isUseGUI()){
                IndexWindow.ChargePort();
            }else {
                System.exit(0);
            }
            isRunning = false;
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }


        threadPool = Executors.newFixedThreadPool(50); // 线程池，最多50个线程在跑

        // 处理控制台的数据，主要用于关闭服务,按下0就调用shutDown()
        Runnable consoleListener = () -> {
            Scanner scanner = new Scanner(System.in);
            while (isRunning) {
                try {
                    Thread.sleep(500);
                    if (scanner.next().equals("0")) {
                        isRunning = false;
                        shutDown();
                        UserTools.OnClose();
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        if (!IndexWindow.isUseGUI())
            threadPool.execute(consoleListener);
        // 刷新文件夹中的文件
        Runnable refreshFilesThread = () -> {
            try {
                Thread.sleep(5000);
                while (!isClosed()) {
                    filesTools.reloadFiles();
//                filesTools.printFile();
                    Thread.sleep(5000);// TODO: 2021-07-26 刷新文件时间
                }

            } catch (InterruptedException ignored) {
            }
        };
        threadPool.execute(refreshFilesThread);

        // 启动ServerSocket

        // 监听命令端口的线程，主要分派命令线程
        ServerSocket finalCommandServer = commandServer;
        Runnable commandListener = () -> {
            System.out.println("正在监听" + commandPort + "命令端口...");
            try {
                while (isRunning) {
                    Socket socket = finalCommandServer.accept();
                    Thread serverThread = new CommandThread(socket);
                    threadPool.execute(serverThread);
                }
            } catch (java.net.SocketException e) {
                System.out.println("命令socket关闭");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CommandThread.safeClose(finalCommandServer);
            }
        };

        threadPool.execute(commandListener);
        // 监听数据端口的线程，主要分派数据线程
        ServerSocket finalDataServer = dataServer;
        Runnable dataListener = () -> {
            System.out.println("正在监听" + dataPort + "数据端口...");
            try {
                while (isRunning) {
                    Socket socket = finalDataServer.accept();
                    Thread dataThread = new DataThread(socket);
                    threadPool.execute(dataThread);
                }
            } catch (java.net.SocketException e) {
                System.out.println("数据socket关闭");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CommandThread.safeClose(finalDataServer);
            }

        };

        threadPool.execute(dataListener);
        // 定时删除无用数据传输任务
        Runnable cleanupTask = () -> {
            try {
                Thread.sleep(1000);
                while (isRunning) {
                    Thread.sleep(1000);
                    CommandThread.cleanupTask();
                }
            } catch (InterruptedException ignored) {
            }
        };
        threadPool.execute(cleanupTask);
    }
    // 同步方法，用于关闭线程池和ServerSocket
    public static synchronized void shutDown() {
        threadPool.shutdown();// 通知结束线程池中的线程

        System.out.println("关闭服务");
        isRunning = false;
        for (ServerSocket serverSocket : serverSockets) {
            CommandThread.safeClose(serverSocket);
        }
        serverSockets.clear();
        threadPool.shutdownNow();
        threadPool = null;
    }

    public static int getPort() {
        return commandPort;
    }

    public static void setPort(int port) {
        commandPort = port;
        dataPort = port - 1;
        String title = "服务端 - 当前映射到 " + FilesTools.getInstance().getFilePath() + " 端口：" + SocketServer.getPort();
        IndexWindow.setTitle(title);
    }
}


