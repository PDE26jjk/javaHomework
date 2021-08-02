package com.pde.SocketClient.GUI.bean;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件项，将显示到文件视图上
 */
public class FileItem{
    public enum Type{
        FILE,
        DIR
    }

    public Type getType() {
        return type;
    }

    private Type type;
    private final SimpleStringProperty fileName = new SimpleStringProperty();
    private final SimpleLongProperty size = new SimpleLongProperty();
    public List<FileItem> getContent() {
        return content;
    }
    private List<FileItem> content;

    public SimpleStringProperty fileNameProperty() {
        return fileName;
    }

    public long getSize() {
        return size.get();
    }

    public SimpleLongProperty sizeProperty() {
        return size;
    }

    @Override
    public String toString() {
        return "[" +
                "type=" + type +
                ", fileName=" + fileName +
                ", size=" + size +
                ", content=" + content +
                ']';
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public String getFileName() {
        return fileName.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public FileItem(String fileName, Long size,Type type) {
        this.fileName.set(fileName);
        this.size.set(size);
        this.type = type;
        if(type == Type.DIR){
            content = new ArrayList<>();
        }else{
            content = null;
        }
    }
}
