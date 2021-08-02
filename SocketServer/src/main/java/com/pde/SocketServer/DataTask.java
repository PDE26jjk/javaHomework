package com.pde.SocketServer;

import com.pde.SocketServer.Dao.Bean.User;

/**
 * 数据传输任务，只有上传下载两种，是命令socket和数据socket的桥梁，放在命令的map中，也有线程专门回收
 * 由FilesTools类创建
 */
public class DataTask {

    public enum Type{
        SAVE,
        SENT
    }

    public enum State {
        WAITING,
        TRANSFERRING,
        ERROR,
        CANCELED,
        DONE
    }

    public Path file;
    public Type type;
    public Long createTime;
    public User user;
    public State status;

    public DataTask(Path file, Type type, User user) {
        this.file = file;
        this.type = type;
        this.user = user;
        this.status = State.WAITING;
        createTime = System.currentTimeMillis();
    }

}
