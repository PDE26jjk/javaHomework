package com.pde.SocketServer;

import com.pde.Serializable.S_Path;
import com.pde.SocketServer.Dao.Bean.User;
import com.pde.SocketServer.GUI.IndexWindow;

import java.io.*;
import java.util.LinkedList;

/**
 * 操作文件的辅助类，与Path类配合使用，提供单例对象，方便互作。提供解析映射文件和序列化的方法。
 * 提供数据命令的创建，期间会检查权限和文件可用性。提供各种对文件的直接操作，包括重命名、删除、新建文件夹。
 */
public class FilesTools {
    private static final FilesTools instance = new FilesTools();

    private FilesTools() {
    }

    public static FilesTools getInstance() {
        return instance;
    }

    // 创建数据传输命令
    public DataTask createCommand(String filePath, DataTask.Type type, User user) throws IOException {
        Path file = Path.findPath(this.filePath + filePath);
        switch (type) {
            case SAVE: { // 已存在文件不能收
                if (file != null) {
                    return null;
                } else {
                    File newFile = new File(this.filePath + filePath);
                    newFile.createNewFile();
                    file = Path.findPath(this.filePath + filePath);
                    return new DataTask(file, type, user);
                }
            }
            case SENT: // 没有的文件不能发，别人正在上传的也不能发
                if (file == null || file.getState() == Path.FileState.WRITING) {

                    return null;
                } else {
                    InputStream fin = null;
                    try {
                        fin = new FileInputStream(file);
                    } catch (java.io.FileNotFoundException e) {
                        return null;
                    } finally {
                        CommandThread.safeClose(fin);
                    }
                    file.setState(Path.FileState.READING);
                    file.addReadingCount();
                    return new DataTask(file, type, user);
                }
        }
        return null;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        String title = "服务端 - 当前映射到 " + filePath + " 端口：" + SocketServer.getPort();
        System.out.println("当前映射到" + filePath);
        IndexWindow.setTitle(title);
        reloadFiles();
    }

    public String getFilePath() {
        return filePath;
    }

    // 映射路径(文件夹)
    private String filePath;
    private Path root;

    public void reloadFiles() {
        Path file = Path.findPath(filePath);
        if (file == null) {
            try {
                throw new FileNotFoundException("没找到路径");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        root = file;
        addFilesToList(filePath);
    }

    private void addFilesToList(String filePath) {
        Path file = Path.findPath(filePath);
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    addFilesToList(childFile.getAbsolutePath());
                }
            }
        }
    }

    // 将文件路径序列化
    public S_Path serializeFiles(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
//            throw new FileNotFoundException("没找到路径");
        }
        S_Path path = null;
        if (file.isDirectory()) {
            path = new S_Path(file.getName(), S_Path.Path_Type.DIR, 0L);
            path.children = new LinkedList<>();
            File[] children = file.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    path.children.add(serializeFiles(childFile.getAbsolutePath()));
                }
            }
        } else if (file.isFile()) {
            path = new S_Path(file.getName(), S_Path.Path_Type.FILE, file.length());
        }
        return path;
    }

    public boolean deleteFile(String filePath) {
        Path file = Path.findPath(this.filePath + filePath);
        if (!file.exists() || file.equals(root)) return false;
        if (isFileAvailable(file)) {
            return deleteFileOrDir(file);
        }
        return false;
    }

    /**
     * 直接操作文件的全删除
     *
     * @param file 文件
     * @return 删除结果
     */
    private boolean deleteFileOrDir(File file) {
        if (file.isDirectory() && null != file.listFiles()) {
            for (File listFile : file.listFiles()) {
                if (!deleteFileOrDir(listFile)) return false;
            }
        }
        return file.delete();
    }

    public boolean renameFile(String oldName, String newName) {
        Path file = Path.findPath(this.filePath + oldName);
        String relativePath = oldName.substring(0, oldName.lastIndexOf(Path.separatorChar));
        if (!file.exists() || file.equals(root)) return false;
        if (isFileAvailable(file)) {
            String pathname = this.filePath + relativePath + Path.separatorChar + newName;
            return file.renameTo(new File(pathname));
        }
        return false;
    }

    public boolean createPath(String dirPath) {
        File file = new File(this.filePath + dirPath);
        if (file.equals(root) || file.exists()) return false;
        Path.findPath(file);
        return file.mkdir();
    }

    // 递归判断目录的子文件和目录是否AVAILABLE
    private boolean isFileAvailable(File file) {
        Path.FileState fileStatus = Path.findPath(file).getState();
        if (fileStatus != Path.FileState.AVAILABLE) {
            return false;
        }
        if (file.isDirectory() && file.listFiles() != null) {
            for (File listFile : file.listFiles()) {
                if (!isFileAvailable(listFile)) return false;
            }
        }
        return true;
    }

}
