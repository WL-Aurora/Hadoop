package com.lyq.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 部署进度界面控制器
 */
public class DeployProgressController {

    private static final Logger logger = LogManager.getLogger(DeployProgressController.class);

    @FXML
    private Label currentStepLabel;

    @FXML
    private Label progressPercentLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextArea logTextArea;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button finishBtn;

    private boolean isDeploying = false;

    @FXML
    public void initialize() {
        logger.info("初始化部署进度界面");

        // 模拟部署过程
        startMockDeployment();
    }

    /**
     * 模拟部署过程（演示用）
     */
    private void startMockDeployment() {
        isDeploying = true;

        new Thread(() -> {
            try {
                // 步骤1: 配置SSH免密登录
                updateProgress(0.1, "配置SSH免密登录");
                addLog("[INFO] 开始配置SSH免密登录...");
                Thread.sleep(2000);
                addLog("[SUCCESS] SSH免密登录配置完成");

                // 步骤2: 安装系统依赖
                updateProgress(0.2, "安装系统依赖");
                addLog("[INFO] 开始安装系统依赖...");
                Thread.sleep(2000);
                addLog("[SUCCESS] 系统依赖安装完成");

                // 步骤3: 配置时间同步
                updateProgress(0.3, "配置时间同步");
                addLog("[INFO] 开始配置时间同步...");
                Thread.sleep(1500);
                addLog("[SUCCESS] 时间同步配置完成");

                // 步骤4: 安装JDK
                updateProgress(0.4, "安装JDK");
                addLog("[INFO] 开始安装JDK 1.8.0_212...");
                Thread.sleep(2500);
                addLog("[SUCCESS] JDK安装完成");

                // 步骤5: 安装Hadoop
                updateProgress(0.6, "安装Hadoop");
                addLog("[INFO] 开始安装Hadoop 3.1.3...");
                Thread.sleep(3000);
                addLog("[SUCCESS] Hadoop安装完成");

                // 步骤6: 生成配置文件
                updateProgress(0.7, "生成配置文件");
                addLog("[INFO] 开始生成Hadoop配置文件...");
                Thread.sleep(1500);
                addLog("[SUCCESS] 配置文件生成完成");

                // 步骤7: 分发配置文件
                updateProgress(0.8, "分发配置文件");
                addLog("[INFO] 开始分发配置文件到各节点...");
                Thread.sleep(2000);
                addLog("[SUCCESS] 配置文件分发完成");

                // 步骤8: 格式化HDFS
                updateProgress(0.9, "格式化HDFS");
                addLog("[INFO] 开始格式化HDFS NameNode...");
                Thread.sleep(2000);
                addLog("[SUCCESS] HDFS格式化完成");

                // 步骤9: 启动集群
                updateProgress(0.95, "启动集群服务");
                addLog("[INFO] 开始启动Hadoop集群...");
                Thread.sleep(2500);
                addLog("[SUCCESS] HDFS服务启动成功");
                addLog("[SUCCESS] YARN服务启动成功");
                addLog("[SUCCESS] 历史服务器启动成功");

                // 完成
                updateProgress(1.0, "部署完成");
                addLog("[SUCCESS] ========================================");
                addLog("[SUCCESS] Hadoop集群部署成功！");
                addLog("[SUCCESS] ========================================");
                addLog("[INFO] NameNode WebUI: http://192.168.10.101:9870");
                addLog("[INFO] ResourceManager WebUI: http://192.168.10.102:8088");

                onDeploymentComplete();

            } catch (InterruptedException e) {
                logger.error("部署过程被中断", e);
                addLog("[ERROR] 部署过程被中断");
            }
        }).start();
    }

    /**
     * 更新进度
     */
    private void updateProgress(double progress, String step) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressPercentLabel.setText(String.format("%.0f%%", progress * 100));
            currentStepLabel.setText(step);
        });
    }

    /**
     * 添加日志
     */
    private void addLog(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logTextArea.appendText(String.format("[%s] %s\n", timestamp, message));
        });
    }

    /**
     * 部署完成
     */
    private void onDeploymentComplete() {
        Platform.runLater(() -> {
            isDeploying = false;
            cancelBtn.setVisible(false);
            finishBtn.setVisible(true);
            finishBtn.setDisable(false);
        });
    }

    @FXML
    private void handleCancel() {
        if (isDeploying) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认取消");
            alert.setHeaderText(null);
            alert.setContentText("确定要取消部署吗？\n已完成的操作将无法回滚。");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    logger.info("用户取消部署");
                    addLog("[WARNING] 用户取消部署");
                    isDeploying = false;
                }
            });
        }
    }

    @FXML
    private void handleFinish() {
        logger.info("部署完成，进入集群管理界面");

        try {
            // 加载集群管理界面
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/cluster-management.fxml"));
            javafx.scene.Parent root = loader.load();

            // 获取当前Stage
            javafx.stage.Stage stage = (javafx.stage.Stage) finishBtn.getScene().getWindow();

            // 设置新场景
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 780, 600);
            stage.setScene(scene);
            stage.setTitle("Hadoop 分布式集群一键部署系统 - 集群管理");

            logger.info("成功跳转到集群管理界面");

        } catch (java.io.IOException e) {
            logger.error("加载集群管理界面失败", e);
            showAlert(Alert.AlertType.ERROR, "界面加载失败",
                    "无法加载集群管理界面: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
