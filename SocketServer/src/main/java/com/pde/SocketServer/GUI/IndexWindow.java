package com.pde.SocketServer.GUI;

import com.pde.SocketServer.Dao.Bean.User;
import com.pde.SocketServer.FilesTools;
import com.pde.SocketServer.SocketServer;
import com.pde.SocketServer.UserTools;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.regex.Pattern;

/**
 * 主窗口界面，继承自javafx.application.Application，
 * 负责加载GUI，显示弹窗、将命令行的输出重定向到信息框等
 */
public class IndexWindow extends Application {

    private static boolean useGUI;
    private TableView<User> userTV;
    private ObservableList<User> userList;
    private TableColumn<User, String> tc_user_name;
    private TableColumn<User, String> tc_password;

    public static void ChargePort() {
        b4.fire();
    }

    private volatile static Button b4;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static boolean isUseGUI() {
        return useGUI;
    }

    private static Stage primaryStage;
    private static TextArea infoTA;

    public static void main(String[] args) {
        useGUI = true;
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if ("NO_GUI".equals(arg)) {
                    useGUI = false;
                    break;
                }
            }
        }

        if (useGUI) {
            // 等GUI加载完了再初始化SocketServer
            new Thread(() -> {
                while (getPrimaryStage() == null || !getPrimaryStage().isShowing()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                }
                // 映射到桌面
                FilesTools.getInstance().setFilePath(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath());
                SocketServer.setPort(9000);
                SocketServer.init(args);
            }).start();
            launch(args);
        } else {
            FilesTools.getInstance().setFilePath(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath());
            SocketServer.setPort(9000);
            SocketServer.init(args);
        }


    }


    /**
     * 这个类用于将控制台的输出重定向到文本框中。和System.setErr() System.setOut() 配合使用
     */
    static class InfoOutput extends PrintStream {
        private final TextArea textArea;

        public InfoOutput(TextArea textArea) {
            super(new ByteArrayOutputStream());
            this.textArea = textArea;
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            print(new String(buf, off, len));
        }

        @Override
        public void print(String s) {
            Platform.runLater(() -> {
                textArea.appendText(s);
            });

        }

    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        IndexWindow.primaryStage = primaryStage;
        String title = "服务端 - 当前映射到 " + FilesTools.getInstance().getFilePath() + " 端口：" + SocketServer.getPort();
        setTitle(title);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        root.setLayoutY(30);

        // 用户视图，用tableView来做
        if (userTV == null) {
            userTV = new TableView<>();
        }
        userTV.setPlaceholder(new Label("无用户"));
        userTV.setEditable(true);

        infoTA = new TextArea();
        infoTA.setEditable(false);
        InfoOutput infoOutput = new InfoOutput(infoTA);

        System.setErr(infoOutput);
        System.setOut(infoOutput);

        // 使用两个可拖动的面板装用户界面和信息框
        StackPane sp1 = new StackPane(userTV);
        StackPane sp2 = new StackPane(infoTA);

        SplitPane pane = new SplitPane();
        pane.setOrientation(Orientation.VERTICAL);
        pane.getItems().addAll(sp1, sp2);

        pane.setDividerPositions(0.7f, 0.3f);
        AnchorPane.setRightAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);

        root.getChildren().add(pane);

        // 第一个列表项是用户ID
        TableColumn<User, Integer> tc_id = new TableColumn<>("id");
        tc_id.setCellValueFactory(new PropertyValueFactory<>("id"));

        // 两个可编辑的列表项显示用户名和密码
        tc_user_name = new TableColumn<>("用户名");
        tc_user_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tc_user_name.setCellFactory(TextFieldTableCell.forTableColumn());

        tc_password = new TableColumn<>("密码");
        tc_password.setCellValueFactory(new PropertyValueFactory<>("pwd"));
        tc_password.setCellFactory(TextFieldTableCell.forTableColumn());

        // 四个可编辑的checkbox显示用户权限
        TableColumn<User, Boolean> tc_browse = new TableColumn<>("浏览权限");
        tc_browse.setCellValueFactory(new PropertyValueFactory<>("a_browse"));
        tc_browse.setCellFactory(CheckBoxTableCell.forTableColumn(tc_browse));

        TableColumn<User, Boolean> tc_upload = new TableColumn<>("上传权限");
        tc_upload.setCellValueFactory(new PropertyValueFactory<>("a_upload"));
        tc_upload.setCellFactory(CheckBoxTableCell.forTableColumn(tc_upload));

        TableColumn<User, Boolean> tc_download = new TableColumn<>("下载权限");
        tc_download.setCellValueFactory(new PropertyValueFactory<>("a_download"));
        tc_download.setCellFactory(CheckBoxTableCell.forTableColumn(tc_download));

        TableColumn<User, Boolean> tc_refactor = new TableColumn<>("修改权限");
        tc_refactor.setCellValueFactory(new PropertyValueFactory<>("a_refactor"));
        tc_refactor.setCellFactory(CheckBoxTableCell.forTableColumn(tc_refactor));

        // 各个列表项的宽度比值，最好和为1
        userTV.getColumns().addAll(tc_id, tc_user_name, tc_password, tc_browse, tc_download, tc_upload, tc_refactor);
        tc_id.prefWidthProperty().bind(userTV.widthProperty().multiply(0.045));
        tc_user_name.prefWidthProperty().bind(userTV.widthProperty().multiply(0.25));
        tc_password.prefWidthProperty().bind(userTV.widthProperty().multiply(0.25));
        tc_browse.prefWidthProperty().bind(userTV.widthProperty().multiply(0.1125));
        tc_download.prefWidthProperty().bind(userTV.widthProperty().multiply(0.1125));
        tc_upload.prefWidthProperty().bind(userTV.widthProperty().multiply(0.1125));
        tc_refactor.prefWidthProperty().bind(userTV.widthProperty().multiply(0.1125));

        loadUser();// 首次加载用户列表

        // 添加用户按钮和其事件绑定，创建一个用户项，然后自动进入其用户名的编辑状态，编辑好后提交数据库。还有重名的处理。
        Button b1 = new Button("添加用户");
        AnchorPane.setTopAnchor(b1, -30.0);
        AnchorPane.setLeftAnchor(b1, 10.0);
        b1.setOnAction(e -> {
            User user = new User("", "");
            userTV.getItems().add(user);

            // 滚到新的用户项，编辑其用户名
            int row = userTV.getItems().size() - 1;
            userTV.scrollTo(row);
            userTV.layout();
            userTV.getSelectionModel().select(row);
            userTV.getSelectionModel().focus(row);
            userTV.edit(row, tc_user_name);
            // 暂时禁用自身按钮，防止重复触发
            b1.setDisable(true);
            tc_user_name.setOnEditCommit(event -> {
                user.setName(event.getNewValue());
                if (user.getName().equals("")) return;
                if (!UserTools.createUser(user)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("已有同名用户!");
                    alert.show();
                }
                b1.setDisable(false);
                loadUser();
            });
            tc_user_name.setOnEditCancel(event -> {
                userTV.getItems().remove(user);
                b1.setDisable(false);
            });
        });
        root.getChildren().add(b1);

        // 删除用户按钮和其事件绑定，直接在数据库中删除相应项
        Button b2 = new Button("删除用户");
        AnchorPane.setTopAnchor(b2, -30.0);
        AnchorPane.setLeftAnchor(b2, 100.0);
        b2.setOnAction(event -> {
            UserTools.deleteUser(userTV.getSelectionModel().getSelectedItem());
            loadUser();
        });
        root.getChildren().add(b2);

        // 映射目录按钮和其事件绑定，打开系统的路径选择器，选择好后用文件工具类的对象设置路径
        Button b3 = new Button("映射目录");
        AnchorPane.setTopAnchor(b3, -30.0);
        AnchorPane.setLeftAnchor(b3, 200.0);
        b3.setOnAction(event -> {
            DirectoryChooser dc = new DirectoryChooser();
            File rootFile = new File(FilesTools.getInstance().getFilePath());
            dc.setInitialDirectory(rootFile);
            File path = dc.showDialog(primaryStage);
            if (path != null && path.exists()) {
                FilesTools.getInstance().setFilePath(path.getAbsolutePath());
            }

        });
        root.getChildren().add(b3);

        // 修改端口按钮和其事件绑定，用一个提示框获取用户输入，用正则判断为数字且在0-65535。然后重启SocketServer的两个Socket
        b4 = new Button("修改端口");
        AnchorPane.setTopAnchor(b4, -30.0);
        AnchorPane.setLeftAnchor(b4, 300.0);
        b4.setOnAction(event -> Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(SocketServer.getPort()));
            Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                if (!Pattern.matches("^\\d+$", newValue)) {
                    dialog.getEditor().textProperty().set(oldValue);
                }
            });
            ok.setOnAction(e -> {
                int port = Integer.parseInt(dialog.getEditor().getText());
                if (port < 65536) {
                    SocketServer.setPort(port);
                    SocketServer.shutDown();
                    SocketServer.init(null);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("端口号在0-65535!");
                    alert.show();
                }

            });
            dialog.showAndWait();
        }));
        root.getChildren().add(b4);

        primaryStage.show();
    }

    // 设置窗口标题，改了映射路径和端口时调用
    public static void setTitle(String title) {

        Platform.runLater(() -> {
            if (primaryStage != null)
                primaryStage.setTitle(title);
        });
    }

    // 将数据库的用户列表查询到窗口中显示，并绑定其每个属性的监听器到ChangeUser
    private void loadUser() {
        userList = FXCollections.observableArrayList();
        userList.addAll(UserTools.getAllUser());
        userTV.setItems(userList);
        for (User user : userList) {
            user.a_browseProperty().addListener((observable) -> ChangeUser(user));
            user.a_uploadProperty().addListener((observable) -> ChangeUser(user));
            user.a_downloadProperty().addListener((observable) -> ChangeUser(user));
            user.a_refactorProperty().addListener((observable) -> ChangeUser(user));
            user.nameProperty().addListener((observable) -> ChangeUser(user));
            user.pwdProperty().addListener((observable) -> ChangeUser(user));
        }
    }

    // 一旦用户信息改变，就更新数据库
    private void ChangeUser(User user) {
        UserTools.updateUser(user);
    }

    // 窗口被关闭时关闭Socket和数据库连接
    @Override
    public void stop() throws Exception {
        SocketServer.shutDown();
        UserTools.OnClose();
    }
}
