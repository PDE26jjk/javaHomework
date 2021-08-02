package com.pde.SocketServer.Dao.Bean;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty pwd = new SimpleStringProperty();
    private final SimpleBooleanProperty a_browse = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty a_upload = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty a_download = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty a_refactor = new SimpleBooleanProperty(false);

    public User(String name, String pwd) {
        setName(name);
        setPwd(pwd);
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getPwd() {
        return pwd.get();
    }

    public SimpleStringProperty pwdProperty() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd.set(pwd);
    }

    public boolean isA_browse() {
        return a_browse.get();
    }

    public SimpleBooleanProperty a_browseProperty() {
        return a_browse;
    }

    public void setA_browse(boolean a_browse) {
        this.a_browse.set(a_browse);
    }

    public boolean isA_upload() {
        return a_upload.get();
    }

    public SimpleBooleanProperty a_uploadProperty() {
        return a_upload;
    }

    public void setA_upload(boolean a_upload) {
        this.a_upload.set(a_upload);
    }

    public boolean isA_download() {
        return a_download.get();
    }

    public SimpleBooleanProperty a_downloadProperty() {
        return a_download;
    }

    public void setA_download(boolean a_download) {
        this.a_download.set(a_download);
    }

    public boolean isA_refactor() {
        return a_refactor.get();
    }

    public SimpleBooleanProperty a_refactorProperty() {
        return a_refactor;
    }

    public void setA_refactor(boolean a_refactor) {
        this.a_refactor.set(a_refactor);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name=" + name +
                ", pwd=" + pwd +
                ", a_browse=" + a_browse +
                ", a_upload=" + a_upload +
                ", a_download=" + a_download +
                ", a_refactor=" + a_refactor +
                '}';
    }
}

