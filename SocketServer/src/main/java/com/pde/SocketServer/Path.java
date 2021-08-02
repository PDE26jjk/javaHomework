package com.pde.SocketServer;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * java.io.File的子类，扩展文件状态、文件当前读的次数等方法。
 */
public class Path extends File {

    public static Map<String, Path> getPaths() {
        return paths;
    }

    private static final Map<String, Path> paths = new ConcurrentHashMap<>();

    private FileState state;

    public FileState getState() {
        return state;
    }

    public void setState(FileState state) {
        this.state = state;
    }


    public static Path findPath(String pathname) {
        Path path = new Path(pathname);
        if (!path.exists()) return null;
        String absolutePath = path.getAbsolutePath();
        Path pathGet = paths.get(absolutePath);
        if (pathGet != null) {
            return pathGet;
        } else {
            paths.put(absolutePath, path);
            return path;
        }
    }

    public static Path findPath(File file) {
        if (!file.exists()) return null;
        String absolutePath = file.getAbsolutePath();
        Path pathGet = paths.get(absolutePath);
        if (pathGet != null) {
            return paths.get(absolutePath);
        } else {
            Path path = new Path(absolutePath);
            paths.put(absolutePath, path);
            return path;
        }
    }

    private Path(String pathname) {
        super(pathname);
        this.state = FileState.AVAILABLE;
    }

    private volatile int readingCount;

    public synchronized void addReadingCount() {
        readingCount++;
    }

    public synchronized void subtractReadingCount() {
        --readingCount;
        if (readingCount <= 0) {
            readingCount = 0;
            this.state = FileState.AVAILABLE;
        }
    }

    /**
     * 文件状态
     */
    public enum FileState {
        AVAILABLE,
        READING,
        WRITING
    }

}
