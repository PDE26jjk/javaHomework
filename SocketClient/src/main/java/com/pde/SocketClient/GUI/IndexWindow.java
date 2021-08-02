package com.pde.SocketClient.GUI;

import com.pde.Serializable.S_Path;
import com.pde.SocketClient.DataTransfer;
import com.pde.SocketClient.GUI.bean.FileItem;
import com.pde.SocketClient.SocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

/**
 * 主窗口界面，继承自javafx.application.Application，
 * 负责加载GUI，显示弹窗、使用SocketClient对象完成各种命令操作
 */
public class IndexWindow extends Application {

    private static ObservableList<FileItem> fileItemList;
    private static TableView<FileItem> fileTV;
    private static Label pathLabel;
    private static SocketClient command;
    private TableColumn<FileItem, String> tc_name;
    private Button b_newFolder;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static Stage primaryStage;

    public static void main(String[] args) throws IOException {

        command = SocketClient.getInstance();
//        command.init();
        launch(args);
    }

    @Override
    public void init() throws Exception {

    }

    public static void setTitle(String title) {
        Platform.runLater(() -> primaryStage.setTitle(title));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        IndexWindow.primaryStage = primaryStage;

        Platform.runLater(LoginWindow::login);// 打开登录窗口
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("客户端");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        root.setLayoutY(30);

        if (pathLabel == null) {
            pathLabel = new Label("/");
        }
        root.getChildren().add(pathLabel);
        AnchorPane.setTopAnchor(pathLabel, -30.0);
        AnchorPane.setLeftAnchor(pathLabel, 2.0);

        Button b1 = new Button("打开/下载");
        AnchorPane.setTopAnchor(b1, 0.0);
        AnchorPane.setLeftAnchor(b1, 10.0);
        b1.setOnAction(event -> {
            OpenOrDownload(fileTV.getSelectionModel().getSelectedItem());
        });
        root.getChildren().add(b1);

        Button b2 = new Button("上传");
        AnchorPane.setTopAnchor(b2, 0.0);
        AnchorPane.setLeftAnchor(b2, 100.0);
        b2.setOnAction(event -> {
            upload();
        });
        root.getChildren().add(b2);
        Button b3 = new Button("刷新");
        AnchorPane.setTopAnchor(b3, 0.0);
        AnchorPane.setRightAnchor(b3, 100.0);
        b3.setOnAction(event -> {
            SocketClient.getInstance().getPath();
        });
        root.getChildren().add(b3);


        Button b4 = new Button("返回上级");
        b4.setLayoutY(0);
        b4.setLayoutX(100);
        b4.setOnAction(event -> {
            // TODO: 2021-07-28 返回上级
            if (fileItemStack.empty()) {
                System.out.println("目录错误");
                return;
            }
            if (fileItemStack.peek() != absolutePathRoot && fileItemStack.size() >= 2) {
                fileItemStack.pop();
                setFileItems();
            }
        });
        AnchorPane.setRightAnchor(b4, 5.0);
        root.getChildren().add(b4);

        b_newFolder = new Button("新建文件夹");
        AnchorPane.setLeftAnchor(b_newFolder, 150.0);
        b_newFolder.setOnAction(event -> {
            newFolder();
        });
        root.getChildren().add(b_newFolder);

        Button b6 = new Button("退出登录");
        AnchorPane.setRightAnchor(b6, 160.0);
        b6.setOnAction(event -> {
            command.logout();
        });

        root.getChildren().add(b6);

        if (fileTV == null) {
            fileTV = new TableView<>();
        }
        fileTV.setPlaceholder(new Label("空文件夹"));
        fileTV.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fileTV.setEditable(true);

        AnchorPane.setTopAnchor(fileTV, 31.0);
        AnchorPane.setLeftAnchor(fileTV, 0.0);
        AnchorPane.setRightAnchor(fileTV, 0.0);
        AnchorPane.setBottomAnchor(fileTV, 0.0);
        // todo 文件名列
        tc_name = new TableColumn<>("文件名");
        tc_name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        tc_name.setCellFactory(TextFieldTableCell.forTableColumn());
        tc_name.setEditable(false);

        // todo 类型列
        TableColumn<FileItem, FileItem.Type> tc_type = new TableColumn<>("类型");
        tc_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        tc_type.setCellFactory(column -> new TableCell<FileItem, FileItem.Type>() {
            @Override
            protected void updateItem(FileItem.Type item, boolean empty) {
                super.updateItem(item, empty);
                this.setText(null);
                this.setGraphic(null);
                if (!empty) {
                    if (item == FileItem.Type.DIR) {
                        this.setText("文件夹");
                    } else {
                        this.setText("");
                    }
                }
            }
        });

        // todo 大小列
        // 设置文件大小的显示格式：,三个数字 KB
        TableColumn<FileItem, Long> tc_size = new TableColumn<>("大小");
        tc_size.setCellValueFactory(new PropertyValueFactory<>("size"));
        tc_size.setCellFactory(column -> new TableCell<FileItem, Long>() {

            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                this.setText(null);
                this.setGraphic(null);
                if (!empty) {
                    if (item <= 0L) {
                        this.setText("");
                    } else {
                        this.setText(String.format("%,d KB", item / 1024 + 1));
                    }

                }
            }
        });

        tc_size.setStyle("-fx-alignment: CENTER-RIGHT;");
        fileTV.getColumns().addAll(tc_name, tc_type, tc_size);
        fileTV.setRowFactory(param -> new TableRow<FileItem>() {
            {
                // 双击时打开文件夹或下载文件
                this.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        OpenOrDownload(this.getItem());
                    }
                });

                // TODO: 文件目录的右键菜单
                ContextMenu cm = new ContextMenu();

                MenuItem item1 = new MenuItem("打开/下载");
                item1.setOnAction(event -> {
                    OpenOrDownload(this.getItem());
                });

                MenuItem item2 = new MenuItem("上传");
                item2.setOnAction(event -> {
                    upload();
                });

                MenuItem item3 = new MenuItem("重命名");
                item3.setOnAction(event -> {
                    rename(this.getItem());
                });

                MenuItem item4 = new MenuItem("删除");
                item4.setOnAction(event -> {
                    delete(this.getItem());
                });

                MenuItem item5 = new MenuItem("新建文件夹");
                item5.setOnAction(event -> {
                    newFolder();
                });

                cm.getItems().addAll(item1, item2, item3, item4, item5);
                this.setContextMenu(cm);
            }
        });

        root.getChildren().add(fileTV);
        primaryStage.show();
    }

    private void rename(FileItem file) {
        // TODO: 重命名文件
        if (file == null) return;

        tc_name.setEditable(true);
        fileTV.edit(fileTV.getSelectionModel().getSelectedIndex(), tc_name);

        tc_name.setOnEditCommit(e -> {
            if (!command.renameFile(getOpenedPath() + file.getFileName(), e.getNewValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("重命名失败!没有权限或者已有同名文件!");
                alert.show();
            }
            tc_name.setEditable(false);
        });

        tc_name.setOnEditCancel(e -> {
            tc_name.setEditable(false);
        });
    }

    private void newFolder() {
        // TODO: 新建文件夹
        String dirName = findUnusedFolderName("新建文件夹");
        FileItem newDir = new FileItem(dirName, 0L, FileItem.Type.DIR);
        fileTV.getItems().add(newDir);
        tc_name.setEditable(true);

        // 滚动到尾行、选择并获取焦点，进入编辑
        int row = fileTV.getItems().size() - 1;
        fileTV.scrollTo(row);
        fileTV.layout();
        fileTV.getSelectionModel().select(row);
        fileTV.getSelectionModel().focus(row);
        fileTV.edit(row, tc_name);
        // 禁用按钮防止重复操作
        b_newFolder.setDisable(true);

        tc_name.setOnEditCommit(event -> {
            if (!command.createFolder(getOpenedPath() + event.getNewValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("新建文件夹失败!没有权限或者已有同名文件夹!");
                alert.show();
            }
            tc_name.setEditable(false);
            b_newFolder.setDisable(false);
        });
        tc_name.setOnEditCancel(event -> {
            tc_name.setEditable(false);
            fileTV.getItems().remove(newDir);
            b_newFolder.setDisable(false);
        });
    }
    // 获取一个未被使用的新文件夹名，i 为编号
    private String findUnusedFolderName(String dirName, int i) {
        String nowName = dirName + " (" + i + ")";
        for (FileItem item : fileTV.getItems()) {
            if (item.getFileName().equals(nowName)) {
                return findUnusedFolderName(dirName, ++i);
            }
        }
        return nowName;
    }
    // 同上
    private String findUnusedFolderName(String dirName) {
        for (FileItem item : fileTV.getItems()) {
            if (item.getFileName().equals(dirName)) {
                return findUnusedFolderName(dirName, 2);
            }
        }
        return dirName;
    }

    private void delete(FileItem file) {
        // TODO: 删除文件
        if (file == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("确认删除" + file.getFileName() + " 么?");
        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        ok.setOnAction(event -> {
            if(!command.deleteFile(getOpenedPath() + file.getFileName())){
                alert.close();
                Alert alert2 = new Alert(Alert.AlertType.ERROR);
                alert2.setContentText("删除失败!没有权限或文件已删除!");
                alert2.show();
            }
        });
        Button cancel = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        alert.show();
        cancel.requestFocus();

    }

    private void OpenOrDownload(FileItem file) {
        if (file == null) return;
        // TODO: 打开文件夹或下载文件
        if (file.getType() == FileItem.Type.DIR) {
            fileItemStack.push(file);
            setFileItems();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(file.getFileName());
            fileChooser.setInitialDirectory(new File("C:\\"));
            // 示范用过滤器
            FileChooser.ExtensionFilter uselessFilter = new FileChooser.ExtensionFilter("任意类型", "*.*");
            fileChooser.getExtensionFilters().add(uselessFilter);

            File savePath = fileChooser.showSaveDialog(new Stage());
            if (savePath != null) {
                if(!command.downloadFile(getOpenedPath() + file.getFileName(), savePath)){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("下载失败!没有权限或文件已删除!");
                    alert.show();
                }
            }
        }
    }

    private void upload() {
        // TODO: 上传文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\"));
        // 示范用过滤器
        FileChooser.ExtensionFilter uselessFilter = new FileChooser.ExtensionFilter("任意类型", "*.*");
        fileChooser.getExtensionFilters().add(uselessFilter);

        File uploadFile = fileChooser.showOpenDialog(new Stage());
        if (uploadFile != null) {
            if(!command.uploadFile(getOpenedPath() + uploadFile.getName(), uploadFile)){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("上传失败!已有同名文件或没有权限!");
                alert.show();
            }
        }
    }

    // TODO: 2021-07-27 进度条窗口
    public static ProgressBar showProgressBarWindow(DataTransfer dataTransfer, String title, DoublePropertyBase doubleBinding, StringProperty labelShow) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setWidth(300);
        stage.setHeight(130);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(new Scene(new AnchorPane()));
        AnchorPane root = (AnchorPane) stage.getScene().getRoot();

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(doubleBinding);
        progressBar.setLayoutX(5);
        progressBar.setLayoutY(5);
        progressBar.setPrefWidth(280);

        // 完成自动关闭
        doubleBinding.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.9999) {
                Platform.runLater(stage::close);
            }
        });

        root.getChildren().add(progressBar);

        Label label = new Label();
//        label.textProperty().bind(labelShow);
//        label.setText("222222");
        labelShow.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> label.setText(newValue));
        });
//
//        });
        label.setLayoutX(5);
        label.setLayoutY(30);
        root.getChildren().add(label);

        Button b1 = new Button("取消");
        b1.setLayoutY(60);
        b1.setLayoutX(125);
        b1.setOnAction(event -> {
            dataTransfer.onCancel();
            stage.close();
        });

        root.getChildren().add(b1);
        stage.show();
        return progressBar;
    }

    // TODO: 2021-07-27 setFileItems
    private static void setFileItems() {
        if (fileItemList == null) {
            fileItemList = FXCollections.observableArrayList();
        } else {
            fileItemList.clear();
        }
        if (fileTV == null) {
            fileTV = new TableView<>();
        }
        if (pathLabel == null) {
            pathLabel = new Label("/");
        }
        Platform.runLater(() -> {
            fileItemList.addAll(fileItemStack.peek().getContent());
            fileTV.setItems(fileItemList);
            pathLabel.setText(getOpenedPath());
        });

    }

    public static String getOpenedPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(File.separatorChar);
        for (int i = 1; i < fileItemStack.size(); i++) {
            sb.append(fileItemStack.get(i).getFileName());
            sb.append(File.separatorChar);
        }
        return sb.toString();
    }

    private static FileItem absolutePathRoot;
    private static Stack<FileItem> fileItemStack = new Stack<>();

    /**
     * 将服务端序列化传过来的路径加载到窗口上显示
     *
     * @param sPath 服务器发来的文件夹路径列表
     */
    public static void loadFileItemsFromSPath(S_Path sPath) {
        absolutePathRoot = sPathToFileItems(sPath);
        Stack<FileItem> newFileItemStack = new Stack<>();
        newFileItemStack.push(absolutePathRoot);
        if (fileItemStack.size() >= 2) {
            FileItem item = absolutePathRoot;
            for (int i = 1; i < fileItemStack.size(); i++) {
                boolean got = false;
                for (FileItem fileItem : item.getContent()) {
                    if (fileItemStack.get(i).getFileName().equals(fileItem.getFileName())) {
                        item = fileItem;
                        newFileItemStack.push(item);
                        got = true;
                        break;
                    }
                }
                if (!got) break;
            }
        }
        fileItemStack = newFileItemStack;

        setFileItems();
    }

    private static FileItem sPathToFileItems(S_Path sPath) {
        FileItem fileitem = null;
        switch (sPath.type) {
            case FILE:
                fileitem = new FileItem(sPath.name, sPath.size, FileItem.Type.FILE);
                break;
            case DIR:
                fileitem = new FileItem(sPath.name, 0L, FileItem.Type.DIR);
                List<FileItem> content = fileitem.getContent();
                for (S_Path child : sPath.children) {
                    content.add(sPathToFileItems(child));
                }
                break;
        }
        return fileitem;
    }

    @Override
    public void stop() throws Exception {
        command.onClose();
    }
}
