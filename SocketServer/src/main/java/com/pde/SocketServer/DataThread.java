package com.pde.SocketServer;

import com.pde.SocketServer.Dao.Bean.User;

import java.io.*;
import java.net.Socket;

/**
 * 处理数据socket的线程类，继承自Thread。对象与每次用户上传或下载一一对应。负责发送或接收文件数据。
 */
public class DataThread extends Thread {

    private final Socket socket;
    private User user;

    public DataThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream fromClient = null;
        DataOutputStream toClient = null;
        try {
            fromClient = new DataInputStream(socket.getInputStream());
            toClient = new DataOutputStream(socket.getOutputStream());
            String taskCode = fromClient.readUTF();
            DataTask dataTask = CommandThread.getCommandByTaskCode(taskCode);

            if(dataTask != null && dataTask.status == DataTask.State.WAITING){
                dataTask.status = DataTask.State.TRANSFERRING;
            }else {
                toClient.writeUTF("ERROR");
                throw new RuntimeException("某用户尝试传输数据失败");
            }
            toClient.writeUTF("SUCCESS");
            user = dataTask.user;
            Path file = dataTask.file;
//            System.out.println(user.getName() + "数据已连接");

            if (!socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                switch (dataTask.type) {
                    // TODO: 2021-07-25 数据发送
                    case SENT: {
                        byte[] bytes = new byte[1024];
                        int length = 0;
                        System.out.println(user.getName() + "正在下载" + file.getAbsolutePath());
                        InputStream fin = new FileInputStream(file);

                        try {
//                            int i = 1;
                            while (dataTask.status == DataTask.State.TRANSFERRING &&
//                                    !socket.isOutputShutdown() &&
                                    (length = fin.read(bytes, 0, bytes.length)) > 0) {
                                toClient.write(bytes, 0, length);
                                toClient.flush();
//                                System.out.println(i++);
//                                sleep(100);
                            }

                            if (dataTask.status == DataTask.State.CANCELED) {
                                System.out.println("文件发送失败,被取消：" + file.getAbsolutePath());

                            } else {
                                dataTask.status = DataTask.State.DONE;
                                System.out.println("文件已发送：" + file.getAbsolutePath());
                            }

                            safeClose(fin);
                        } catch (java.net.SocketException e) {
                            dataTask.status = DataTask.State.ERROR;
                            System.out.println("文件发送失败：" + dataTask.file.getAbsolutePath());
                        } finally {
                            toClient.flush();
                            file.subtractReadingCount();
                        }
                        break;
                    }

                    // TODO: 2021-07-25 数据接收
                    case SAVE: {
                        System.out.println(user.getName() + "正在上传" + file.getAbsolutePath());
                        FileOutputStream fout = new FileOutputStream(file);
                        int length = 0;
                        byte[] bytes = new byte[1024];
                        while (dataTask.status == DataTask.State.TRANSFERRING &&
//                                !socket.isInputShutdown() &&
                                (length = fromClient.read(bytes, 0, bytes.length)) > 0) {
                            fout.write(bytes, 0, length);
                            fout.flush();
                        }

                        if (dataTask.status == DataTask.State.CANCELED) {
                            System.out.println("文件接收失败,被取消" + dataTask.file.getAbsolutePath());
                            if(file.exists()){
                                file.delete();
                            }
                        } else {
                            dataTask.status = DataTask.State.DONE;
                            System.out.println("文件保存到" + dataTask.file.getAbsolutePath());
                            file.setState(Path.FileState.AVAILABLE);
                        }
                        safeClose(fout);
                        break;
                    }
                }
            }
            sleep(0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            safeClose(socket);
        }
        safeClose(socket);
        System.out.println(user.getName() + "数据已断开");
        FilesTools.getInstance().reloadFiles();
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

}

