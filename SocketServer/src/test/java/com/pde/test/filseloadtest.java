package com.pde.test;

import com.pde.Serializable.S_Path;
import com.pde.SocketServer.FilesTools;
import org.junit.Test;

import java.io.FileNotFoundException;

public class filseloadtest {

    @Test
    public void test1() throws FileNotFoundException {
        FilesTools filesTools = FilesTools.getInstance();
        filesTools.setFilePath("C:\\Users\\26jjk\\Desktop\\ShadowsocksR-win-4.9.0");
        filesTools.reloadFiles();
        S_Path sPath = filesTools.serializeFiles(filesTools.getFilePath());
        System.out.println(sPath);
    }
}
