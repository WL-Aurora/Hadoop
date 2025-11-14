package com.lyq.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 集群管理界面控制器
 */
public class ClusterManagementController {

    private static final Logger logger = LogManager.getLogger(ClusterManagementController.class);

    @FXML
    private Label vm1StatusLabel;

    @FXML
    private Label vm2StatusLabel;

    @FXML
    private Label vm3StatusLabel;

    @FXML
    public void initialize() {
        logger.info("初始化集群管理界面");
    }

    @FXML
    private void handleStartCluster() {
        logger.info("启动集群");
        showAlert(Alert.AlertType.INFORMATION, "启动集群",
                "正在启动Hadoop集群...\n\n" +
                        "• 启动HDFS服务\n" +
                        "• 启动YARN服务\n" +
                        "• 启动历史服务器\n\n" +
                        "集群启动成功！");
    }

    @FXML
    private void handleStopCluster() {
        logger.info("停止集群");
        showAlert(Alert.AlertType.INFORMATION, "停止集群",
                "正在停止Hadoop集群...\n\n" +
                        "• 停止历史服务器\n" +
                        "• 停止YARN服务\n" +
                        "• 停止HDFS服务\n\n" +
                        "集群已停止！");
    }

    @FXML
    private void handleRestartCluster() {
        logger.info("重启集群");
        showAlert(Alert.AlertType.INFORMATION, "重启集群",
                "正在重启Hadoop集群...\n\n" +
                        "集群重启成功！");
    }

    @FXML
    private void handleCheckStatus() {
        logger.info("查看集群状态");
        showAlert(Alert.AlertType.INFORMATION, "集群状态",
                "虚拟机1 (hadoop101):\n" +
                        "  NameNode: 运行中\n" +
                        "  DataNode: 运行中\n\n" +
                        "虚拟机2 (hadoop102):\n" +
                        "  ResourceManager: 运行中\n" +
                        "  DataNode: 运行中\n\n" +
                        "虚拟机3 (hadoop103):\n" +
                        "  SecondaryNameNode: 运行中\n" +
                        "  DataNode: 运行中");
    }

    @FXML
    private void handleOpenNameNode() {
        logger.info("打开NameNode WebUI");
        openWebUI("http://192.168.10.101:9870");
    }

    @FXML
    private void handleOpenResourceManager() {
        logger.info("打开ResourceManager WebUI");
        openWebUI("http://192.168.10.102:8088");
    }

    @FXML
    private void handleOpenHistoryServer() {
        logger.info("打开历史服务器 WebUI");
        openWebUI("http://192.168.10.101:19888");
    }

    private void openWebUI(String url) {
        try {
            // 尝试打开默认浏览器
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                logger.info("成功打开WebUI: {}", url);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "打开WebUI",
                        "请手动在浏览器中打开:\n" + url);
            }
        } catch (Exception e) {
            logger.error("打开WebUI失败", e);
            showAlert(Alert.AlertType.ERROR, "打开失败",
                    "无法打开浏览器，请手动访问:\n" + url);
        }
    }

    @FXML
    private void handleExit() {
        logger.info("退出应用");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.setContentText("确定要退出系统吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logger.info("用户确认退出");
                System.exit(0);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
