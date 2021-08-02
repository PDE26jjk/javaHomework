package com.pde.SocketClient.GUI;

import com.pde.SocketClient.SocketClient;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.util.regex.Pattern;

/**
 * 负责登录窗口的创建和显示、还有有效性判断
 */
public class LoginWindow {

    private static Stage loginWindow;

    private static void init(boolean isFirst) {
        loginWindow = new Stage();
        loginWindow.setWidth(450);
        loginWindow.setHeight(200);
        loginWindow.initOwner(IndexWindow.getPrimaryStage());
        loginWindow.initModality(Modality.APPLICATION_MODAL);

        AnchorPane root = new AnchorPane();
        loginWindow.setScene(new Scene(root));

        loginWindow.setTitle("登录");
        loginWindow.setResizable(false);

        Label ipLabel = new Label("服务端IP: ");
        Label portLabel = new Label("端口: ");
        ipLabel.setLayoutX(20);
        ipLabel.setLayoutY(22);
        root.getChildren().add(ipLabel);
        portLabel.setLayoutX(300);
        portLabel.setLayoutY(22);
        root.getChildren().add(portLabel);

        TextField ip = new TextField("");
        ip.setLayoutY(18);
        ip.setLayoutX(90);
        ip.setPrefWidth(200);
        TextField port = new TextField("");
        port.setLayoutY(18);
        port.setLayoutX(340);

        port.setPrefWidth(60);
        root.getChildren().add(ip);
        root.getChildren().add(port);

        Label userNameLabel = new Label("账号: ");
        Label passwordLabel = new Label("密码: ");
        userNameLabel.setLayoutX(20);
        userNameLabel.setLayoutY(62);
        root.getChildren().add(userNameLabel);
        passwordLabel.setLayoutX(212);
        passwordLabel.setLayoutY(62);
        root.getChildren().add(passwordLabel);

        TextField userName = new TextField("");
        userName.setLayoutY(58);
        userName.setLayoutX(60);
        userName.setPrefWidth(145);

        PasswordField password = new PasswordField();
        password.setLayoutY(58);
        password.setLayoutX(255);
        password.setPrefWidth(145);

        root.getChildren().add(userName);
        root.getChildren().add(password);

        Button b1 = new Button("登录");
        Button b2 = new Button("退出");

        b1.setLayoutY(100);
        b1.setLayoutX(100);

        b2.setLayoutY(100);
        b2.setLayoutX(300);

        root.getChildren().add(b1);
        root.getChildren().add(b2);

        b2.setOnAction(event -> onClose());
        loginWindow.setOnCloseRequest(event -> onClose());

        b1.setOnAction(event -> {
            // 判断ip合法性
            boolean ipCheck = Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$",
                    ip.getText()) || isIPv6(ip.getText());
            boolean portCheck = false;
            int portNum = -1;
            // 判断ip合法性
            if (Pattern.matches("^\\d+$", port.getText())) {
                portNum = Integer.parseInt(port.getText());
                if (portNum < 65535) portCheck = true;
            }

            if (!ipCheck) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("请输入类似127.0.0.1的IP!");
                alert.show();
            } else if (!portCheck) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("请输入0-65535的端口号!");
                alert.show();
            } else {

                // TODO: 按钮登录
                if (SocketClient.getInstance().login(userName.getText(), password.getText(), ip.getText(), portNum)) {
                    loginWindow.hide();
                    IndexWindow.setTitle("客户端 - 用户 " + userName.getText() + " 已连接到" + ip.getText());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("登录失败");
                    alert.show();
                }
            }
        });

        // todo 登录窗口默认参数
        ip.setText("127.0.0.1");
        port.setText("9000");
        userName.setText("pde");
        password.setText("123456");
        loginWindow.show();

        if (!isFirst) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("连接已断开!请重新登录!!");
            alert.show();
        }
    }

    private static void showLoginWindow(boolean isFirst) {
        Platform.runLater(() -> {
            init(isFirst);
        });
    }
    // 出错后登录
    public static void reLogin() {
        IndexWindow.setTitle("客户端 - 未连接");
        showLoginWindow(false);
    }
    // 首次或登出后登录
    public static void login() {
        IndexWindow.setTitle("客户端 - 未连接");
        showLoginWindow(true);
    }

    private static Stage getInstance() {
        return loginWindow;
    }

    /**
     * 判断一个ip是否是ipv6
     *
     * @param ip ip
     * @return true则是
     */
    public static boolean isIPv6(String ip) {
        return Pattern.matches("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$",
                ip);
    }

    private static void onClose() {
        // TODO: 登录窗口关闭
        loginWindow.close();
        IndexWindow.getPrimaryStage().close();
    }


}
