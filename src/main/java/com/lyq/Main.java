package com.lyq;

import com.lyq.controller.ConnectionController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hadoop 自动部署系统主程序入口
 * 负责启动 JavaFX 应用程序并加载主界面
 */
public class Main extends Application {

    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * 应用程序标题
     */
    private static final String APP_TITLE = "Hadoop 分布式集群一键部署系统 - 虚拟机连接配置";

    /**
     * 窗口宽度
     */
    private static final int WINDOW_WIDTH = 780;

    /**
     * 窗口高度
     */
    private static final int WINDOW_HEIGHT = 600;

    /**
     * 控制器实例，用于资源清理
     */
    private ConnectionController controller;

    /**
     * 应用程序启动方法
     * 
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("启动 Hadoop 自动部署系统");

        try {
            // 加载 FXML 文件
            logger.debug("加载 FXML 界面文件");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/connection.fxml"));
            Parent root = loader.load();

            // 获取控制器实例
            controller = loader.getController();

            // 创建场景
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // 配置主窗口
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // 禁止调整窗口大小，固定尺寸

            // 设置窗口关闭事件处理
            primaryStage.setOnCloseRequest(event -> {
                logger.info("用户关闭应用程序");
                cleanup();
            });

            // 显示窗口
            primaryStage.show();

            logger.info("应用程序启动成功");

        } catch (Exception e) {
            logger.error("启动应用程序失败", e);
            showErrorAndExit("启动失败", "无法启动应用程序: " + e.getMessage());
        }
    }

    /**
     * 应用程序停止方法
     * 在应用关闭时自动调用，用于清理资源
     */
    @Override
    public void stop() {
        logger.info("应用程序正在关闭");
        cleanup();
        logger.info("应用程序已关闭");
    }

    /**
     * 清理资源
     * 关闭所有 SSH 会话，释放系统资源
     */
    private void cleanup() {
        logger.debug("开始清理资源");

        try {
            if (controller != null) {
                controller.cleanup();
            }
        } catch (Exception e) {
            logger.error("清理资源时发生错误", e);
        }

        logger.debug("资源清理完成");
    }

    /**
     * 显示错误信息并退出应用程序
     * 
     * @param title   错误标题
     * @param message 错误信息
     */
    private void showErrorAndExit(String title, String message) {
        logger.error("{}: {}", title, message);

        // 在 JavaFX 应用线程中显示错误对话框
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            // 退出应用程序
            System.exit(1);
        });
    }

    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Hadoop 自动部署系统启动");
        logger.info("版本: 1.0.0");
        logger.info("========================================");

        // 启动 JavaFX 应用程序
        launch(args);
    }
}
