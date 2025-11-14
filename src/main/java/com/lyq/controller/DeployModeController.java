package com.lyq.controller;

import com.lyq.exception.ValidationException;
import com.lyq.model.DeployMode;
import com.lyq.model.DeployModeConfig;
import com.lyq.model.NodeRole;
import com.lyq.service.ConfigService;
import com.lyq.service.ValidationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

/**
 * 部署模式选择界面控制器
 * 负责处理用户选择部署模式和配置节点角色
 */
public class DeployModeController {

    private static final Logger logger = LogManager.getLogger(DeployModeController.class);

    // ========== FXML注入的界面元素 ==========

    // 部署模式单选按钮
    @FXML
    private RadioButton quickModeRadio;

    @FXML
    private RadioButton customModeRadio;

    @FXML
    private ToggleGroup deployModeGroup;

    // 一键部署模式详情
    @FXML
    private VBox quickModeDetails;

    // 自定义部署模式详情
    @FXML
    private VBox customModeDetails;

    // 虚拟机1角色复选框
    @FXML
    private CheckBox vm1NameNodeCheck;

    @FXML
    private CheckBox vm1ResourceManagerCheck;

    @FXML
    private CheckBox vm1SecondaryNameNodeCheck;

    // 虚拟机2角色复选框
    @FXML
    private CheckBox vm2NameNodeCheck;

    @FXML
    private CheckBox vm2ResourceManagerCheck;

    @FXML
    private CheckBox vm2SecondaryNameNodeCheck;

    // 虚拟机3角色复选框
    @FXML
    private CheckBox vm3NameNodeCheck;

    @FXML
    private CheckBox vm3ResourceManagerCheck;

    @FXML
    private CheckBox vm3SecondaryNameNodeCheck;

    // 验证错误标签
    @FXML
    private Label validationErrorLabel;

    // 导航按钮
    @FXML
    private Button previousBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button nextBtn;

    // ========== 服务层对象 ==========

    private final ConfigService configService;
    private final ValidationService validationService;

    // ========== 状态变量 ==========

    private DeployModeConfig deployModeConfig;

    /**
     * 构造函数
     */
    public DeployModeController() {
        this.configService = new ConfigService();
        this.validationService = new ValidationService();
        this.deployModeConfig = new DeployModeConfig();

        logger.info("DeployModeController 初始化");
    }

    /**
     * 初始化方法
     * 在FXML加载完成后自动调用
     */
    @FXML
    public void initialize() {
        logger.info("开始初始化部署模式选择界面");

        // 设置单选按钮监听器
        setupModeListeners();

        // 设置复选框监听器
        setupCheckBoxListeners();

        // 加载保存的配置
        loadSavedConfig();

        logger.info("部署模式选择界面初始化完成");
    }

    /**
     * 设置部署模式单选按钮监听器
     */
    private void setupModeListeners() {
        quickModeRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                handleQuickDeployMode();
            }
        });

        customModeRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                handleCustomDeployMode();
            }
        });
    }

    /**
     * 设置角色复选框监听器
     */
    private void setupCheckBoxListeners() {
        // 虚拟机1
        vm1NameNodeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(1, NodeRole.NAMENODE, newValue);
        });
        vm1ResourceManagerCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(1, NodeRole.RESOURCEMANAGER, newValue);
        });
        vm1SecondaryNameNodeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(1, NodeRole.SECONDARYNAMENODE, newValue);
        });

        // 虚拟机2
        vm2NameNodeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(2, NodeRole.NAMENODE, newValue);
        });
        vm2ResourceManagerCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(2, NodeRole.RESOURCEMANAGER, newValue);
        });
        vm2SecondaryNameNodeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(2, NodeRole.SECONDARYNAMENODE, newValue);
        });

        // 虚拟机3
        vm3NameNodeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(3, NodeRole.NAMENODE, newValue);
        });
        vm3ResourceManagerCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(3, NodeRole.RESOURCEMANAGER, newValue);
        });
        vm3SecondaryNameNodeCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRoleSelection(3, NodeRole.SECONDARYNAMENODE, newValue);
        });
    }

    /**
     * 加载保存的配置
     */
    private void loadSavedConfig() {
        logger.info("开始加载保存的部署模式配置");

        try {
            if (configService.deployModeConfigExists()) {
                deployModeConfig = configService.loadDeployModeConfig();

                // 根据配置更新界面
                if (deployModeConfig.getMode() == DeployMode.QUICK) {
                    quickModeRadio.setSelected(true);
                } else {
                    customModeRadio.setSelected(true);
                    updateCustomModeUI();
                }

                logger.info("部署模式配置加载成功: {}", deployModeConfig.getMode());
            } else {
                logger.info("配置文件不存在，使用默认配置");
            }
        } catch (IOException e) {
            logger.error("加载部署模式配置失败", e);
            showAlert(Alert.AlertType.WARNING, "配置加载失败",
                    "无法加载保存的配置: " + e.getMessage() + "\n将使用默认配置。");
        }
    }

    /**
     * 处理一键部署模式选择
     */
    private void handleQuickDeployMode() {
        logger.info("用户选择一键部署模式");

        // 更新配置
        deployModeConfig.setMode(DeployMode.QUICK);

        // 显示一键部署详情，隐藏自定义配置
        quickModeDetails.setVisible(true);
        quickModeDetails.setManaged(true);
        customModeDetails.setVisible(false);
        customModeDetails.setManaged(false);

        // 清除验证错误
        hideValidationError();
    }

    /**
     * 处理自定义部署模式选择
     */
    private void handleCustomDeployMode() {
        logger.info("用户选择自定义部署模式");

        // 更新配置
        deployModeConfig.setMode(DeployMode.CUSTOM);

        // 隐藏一键部署详情，显示自定义配置
        quickModeDetails.setVisible(false);
        quickModeDetails.setManaged(false);
        customModeDetails.setVisible(true);
        customModeDetails.setManaged(true);

        // 更新自定义模式界面
        updateCustomModeUI();

        // 清除验证错误
        hideValidationError();
    }

    /**
     * 更新自定义模式界面
     * 根据当前配置更新复选框状态
     */
    private void updateCustomModeUI() {
        // 虚拟机1
        vm1NameNodeCheck.setSelected(deployModeConfig.hasRole(1, NodeRole.NAMENODE));
        vm1ResourceManagerCheck.setSelected(deployModeConfig.hasRole(1, NodeRole.RESOURCEMANAGER));
        vm1SecondaryNameNodeCheck.setSelected(deployModeConfig.hasRole(1, NodeRole.SECONDARYNAMENODE));

        // 虚拟机2
        vm2NameNodeCheck.setSelected(deployModeConfig.hasRole(2, NodeRole.NAMENODE));
        vm2ResourceManagerCheck.setSelected(deployModeConfig.hasRole(2, NodeRole.RESOURCEMANAGER));
        vm2SecondaryNameNodeCheck.setSelected(deployModeConfig.hasRole(2, NodeRole.SECONDARYNAMENODE));

        // 虚拟机3
        vm3NameNodeCheck.setSelected(deployModeConfig.hasRole(3, NodeRole.NAMENODE));
        vm3ResourceManagerCheck.setSelected(deployModeConfig.hasRole(3, NodeRole.RESOURCEMANAGER));
        vm3SecondaryNameNodeCheck.setSelected(deployModeConfig.hasRole(3, NodeRole.SECONDARYNAMENODE));
    }

    /**
     * 处理角色选择变化
     * 
     * @param vmIndex  虚拟机编号（1-3）
     * @param role     角色
     * @param selected 是否选中
     */
    private void handleRoleSelection(int vmIndex, NodeRole role, boolean selected) {
        logger.debug("虚拟机{}角色{}变化: {}", vmIndex, role, selected ? "选中" : "取消");

        if (selected) {
            deployModeConfig.addRole(vmIndex, role);
        } else {
            deployModeConfig.removeRole(vmIndex, role);
        }

        // 清除验证错误
        hideValidationError();
    }

    /**
     * 验证角色配置
     * 
     * @return true表示验证通过，false表示验证失败
     */
    private boolean validateRoleConfiguration() {
        logger.info("验证角色配置");

        try {
            validationService.validateRoleConfiguration(deployModeConfig.getRoleAssignments());
            hideValidationError();
            return true;
        } catch (ValidationException e) {
            logger.warn("角色配置验证失败: {}", e.getMessage());
            showValidationError(e.getMessage());
            return false;
        }
    }

    /**
     * 显示验证错误
     * 
     * @param message 错误消息
     */
    private void showValidationError(String message) {
        validationErrorLabel.setText(message);
        validationErrorLabel.setVisible(true);
        validationErrorLabel.setManaged(true);
    }

    /**
     * 隐藏验证错误
     */
    private void hideValidationError() {
        validationErrorLabel.setText("");
        validationErrorLabel.setVisible(false);
        validationErrorLabel.setManaged(false);
    }

    /**
     * 保存配置
     */
    private void saveConfiguration() {
        logger.info("保存部署模式配置");

        try {
            configService.saveDeployModeConfig(deployModeConfig);
            logger.info("部署模式配置保存成功");
        } catch (IOException e) {
            logger.error("保存部署模式配置失败", e);
            showAlert(Alert.AlertType.WARNING, "配置保存失败",
                    "无法保存配置: " + e.getMessage() + "\n但不影响继续操作。");
        }
    }

    /**
     * 处理上一步按钮点击
     * 返回虚拟机连接配置界面
     */
    @FXML
    private void handlePrevious() {
        logger.info("用户点击上一步按钮");

        // 保存当前配置
        saveConfiguration();

        // TODO: 返回虚拟机连接配置界面
        showAlert(Alert.AlertType.INFORMATION, "功能开发中",
                "返回上一步功能正在开发中！\n\n" +
                        "将返回到虚拟机连接配置界面。");
    }

    /**
     * 处理下一步按钮点击
     * 进入集群参数配置界面
     */
    @FXML
    private void handleNext() {
        logger.info("用户点击下一步按钮");

        // 如果是自定义模式，需要验证角色配置
        if (deployModeConfig.getMode() == DeployMode.CUSTOM) {
            if (!validateRoleConfiguration()) {
                return;
            }
        }

        // 保存配置
        saveConfiguration();

        try {
            // 加载集群参数配置界面
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/cluster-config.fxml"));
            javafx.scene.Parent root = loader.load();

            // 获取控制器并传递连接信息
            com.lyq.controller.ClusterConfigController controller = loader.getController();

            // 从ConfigService读取连接信息
            try {
                java.util.List<com.lyq.model.VMConnectionConfig> configs = configService.loadConfig();
                if (configs != null && configs.size() >= 3) {
                    controller.setVmConnectionInfo(
                            configs.get(0).getIp(), configs.get(1).getIp(), configs.get(2).getIp(),
                            configs.get(0).getUsername(), configs.get(1).getUsername(), configs.get(2).getUsername(),
                            configs.get(0).getPassword(), configs.get(1).getPassword(), configs.get(2).getPassword());
                    logger.info("已传递连接信息到集群配置界面");
                }
            } catch (Exception e) {
                logger.warn("读取连接信息失败: {}", e.getMessage());
            }

            // 获取当前Stage
            javafx.stage.Stage stage = (javafx.stage.Stage) nextBtn.getScene().getWindow();

            // 设置新场景
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 780, 600);
            stage.setScene(scene);
            stage.setTitle("Hadoop 分布式集群一键部署系统 - 集群参数配置");

            logger.info("成功跳转到集群参数配置界面");

        } catch (java.io.IOException e) {
            logger.error("加载集群参数配置界面失败", e);
            showAlert(Alert.AlertType.ERROR, "界面加载失败",
                    "无法加载集群参数配置界面: " + e.getMessage());
        }
    }

    /**
     * 处理取消按钮点击
     * 显示确认对话框并退出
     */
    @FXML
    private void handleCancel() {
        logger.info("用户点击取消按钮");

        // 显示确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.setContentText("确定要退出部署流程吗？\n当前配置将不会保存。");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("用户确认退出");
            // TODO: 退出应用程序
            showAlert(Alert.AlertType.INFORMATION, "退出",
                    "退出功能正在开发中！");
        } else {
            logger.info("用户取消退出");
        }
    }

    /**
     * 显示Alert对话框
     * 
     * @param alertType 对话框类型
     * @param title     标题
     * @param content   内容
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 获取当前部署模式配置
     * 
     * @return 部署模式配置
     */
    public DeployModeConfig getDeployModeConfig() {
        return deployModeConfig;
    }
}
