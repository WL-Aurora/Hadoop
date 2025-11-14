package com.lyq.controller;

import com.lyq.exception.ValidationException;
import com.lyq.model.ConnectionResult;
import com.lyq.model.ConnectionStatus;
import com.lyq.model.VMConnectionConfig;
import com.lyq.service.ConfigService;
import com.lyq.service.SSHConnectionService;
import com.lyq.service.ValidationService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 连接配置界面控制器
 * 负责处理用户交互，协调服务层完成连接配置和测试
 */
public class ConnectionController {

    private static final Logger logger = LogManager.getLogger(ConnectionController.class);

    // ========== FXML注入的界面元素 ==========

    // IP地址输入框
    @FXML
    private TextField vm1IpField;

    @FXML
    private TextField vm2IpField;

    @FXML
    private TextField vm3IpField;

    // 虚拟机1配置
    @FXML
    private TextField vm1UsernameField;

    @FXML
    private PasswordField vm1PasswordField;

    @FXML
    private Button vm1PasswordToggleBtn;

    @FXML
    private Button vm1TestBtn;

    @FXML
    private Label vm1StatusLabel;

    @FXML
    private Label vm1ErrorLabel;

    // 虚拟机2配置
    @FXML
    private TextField vm2UsernameField;

    @FXML
    private PasswordField vm2PasswordField;

    @FXML
    private Button vm2PasswordToggleBtn;

    @FXML
    private Button vm2TestBtn;

    @FXML
    private Label vm2StatusLabel;

    @FXML
    private Label vm2ErrorLabel;

    // 虚拟机3配置
    @FXML
    private TextField vm3UsernameField;

    @FXML
    private PasswordField vm3PasswordField;

    @FXML
    private Button vm3PasswordToggleBtn;

    @FXML
    private Button vm3TestBtn;

    @FXML
    private Label vm3StatusLabel;

    @FXML
    private Label vm3ErrorLabel;

    // 操作按钮
    @FXML
    private Button testConnectionBtn;

    @FXML
    private Button nextBtn;

    // 加载动画
    @FXML
    private ProgressIndicator loadingIndicator;

    // ========== 服务层对象 ==========

    private final ValidationService validationService;
    private final ConfigService configService;
    private final SSHConnectionService sshConnectionService;

    // ========== 状态变量 ==========

    // 用于密码显示切换的临时TextField
    private TextField vm1PasswordTextField;
    private TextField vm2PasswordTextField;
    private TextField vm3PasswordTextField;

    // 密码是否显示
    private boolean isVm1PasswordVisible = false;
    private boolean isVm2PasswordVisible = false;
    private boolean isVm3PasswordVisible = false;

    /**
     * 构造函数
     */
    public ConnectionController() {
        this.validationService = new ValidationService();
        this.configService = new ConfigService();
        this.sshConnectionService = new SSHConnectionService();

        logger.info("ConnectionController 初始化");
    }

    /**
     * 初始化方法
     * 在FXML加载完成后自动调用
     */
    @FXML
    public void initialize() {
        logger.info("开始初始化连接配置界面");

        // 注释掉自动加载配置，用户需要手动输入
        // loadSavedConfig();

        // 设置IP地址输入框的实时验证监听器
        setupIpValidationListeners();

        logger.info("连接配置界面初始化完成");
    }

    /**
     * 加载保存的配置
     */
    private void loadSavedConfig() {
        logger.info("开始加载保存的配置");

        try {
            // 检查配置文件是否存在
            if (!configService.configExists()) {
                logger.info("配置文件不存在，使用默认配置");
                return;
            }

            // 加载配置
            List<VMConnectionConfig> configs = configService.loadConfig();

            if (configs == null || configs.isEmpty()) {
                logger.warn("加载的配置为空");
                return;
            }

            // 填充界面字段
            for (VMConnectionConfig config : configs) {
                switch (config.getIndex()) {
                    case 1:
                        vm1IpField.setText(config.getIp());
                        vm1UsernameField.setText(config.getUsername());
                        vm1PasswordField.setText(config.getPassword());
                        break;
                    case 2:
                        vm2IpField.setText(config.getIp());
                        vm2UsernameField.setText(config.getUsername());
                        vm2PasswordField.setText(config.getPassword());
                        break;
                    case 3:
                        vm3IpField.setText(config.getIp());
                        vm3UsernameField.setText(config.getUsername());
                        vm3PasswordField.setText(config.getPassword());
                        break;
                }
            }

            logger.info("配置加载成功，已填充到界面");

        } catch (IOException e) {
            logger.error("加载配置失败", e);
            showAlert(Alert.AlertType.WARNING, "配置加载失败",
                    "无法加载保存的配置: " + e.getMessage() + "\n将使用空配置。");
        }
    }

    /**
     * 设置IP地址输入框的实时验证监听器
     */
    private void setupIpValidationListeners() {
        // 为每个IP输入框添加文本变化监听器
        vm1IpField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleIpChange(vm1IpField, vm1ErrorLabel);
        });

        vm2IpField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleIpChange(vm2IpField, vm2ErrorLabel);
        });

        vm3IpField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleIpChange(vm3IpField, vm3ErrorLabel);
        });
    }

    /**
     * 处理IP地址输入变化
     * 实时验证IP格式并更新界面反馈
     * 
     * @param ipField    IP输入框
     * @param errorLabel 错误提示标签
     */
    private void handleIpChange(TextField ipField, Label errorLabel) {
        String ip = ipField.getText();

        // 如果输入为空，清除错误提示
        if (ip == null || ip.trim().isEmpty()) {
            ipField.getStyleClass().remove("error-field");
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            return;
        }

        // 验证IP格式
        try {
            validationService.validateIpAddress(ip);

            // 验证通过，清除错误样式
            ipField.getStyleClass().remove("error-field");
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            logger.debug("IP地址格式验证通过: {}", ip);

        } catch (ValidationException e) {
            // 验证失败，显示错误样式和提示
            if (!ipField.getStyleClass().contains("error-field")) {
                ipField.getStyleClass().add("error-field");
            }

            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            logger.debug("IP地址格式验证失败: {}", ip);
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
     * 处理虚拟机1密码显示切换
     */
    @FXML
    private void handleVm1PasswordToggle() {
        logger.debug("切换虚拟机1密码显示状态");
        togglePassword(1, vm1PasswordField, vm1PasswordTextField, vm1PasswordToggleBtn, isVm1PasswordVisible);
        isVm1PasswordVisible = !isVm1PasswordVisible;
    }

    /**
     * 处理虚拟机2密码显示切换
     */
    @FXML
    private void handleVm2PasswordToggle() {
        logger.debug("切换虚拟机2密码显示状态");
        togglePassword(2, vm2PasswordField, vm2PasswordTextField, vm2PasswordToggleBtn, isVm2PasswordVisible);
        isVm2PasswordVisible = !isVm2PasswordVisible;
    }

    /**
     * 处理虚拟机3密码显示切换
     */
    @FXML
    private void handleVm3PasswordToggle() {
        logger.debug("切换虚拟机3密码显示状态");
        togglePassword(3, vm3PasswordField, vm3PasswordTextField, vm3PasswordToggleBtn, isVm3PasswordVisible);
        isVm3PasswordVisible = !isVm3PasswordVisible;
    }

    /**
     * 切换密码显示状态
     * 
     * @param vmIndex           虚拟机编号
     * @param passwordField     密码输入框
     * @param passwordTextField 明文密码输入框
     * @param toggleBtn         切换按钮
     * @param isVisible         当前是否可见
     */
    private void togglePassword(int vmIndex, PasswordField passwordField, TextField passwordTextField,
            Button toggleBtn, boolean isVisible) {
        if (isVisible) {
            // 当前是显示状态，切换为隐藏
            hidePassword(vmIndex, passwordField, passwordTextField, toggleBtn);
        } else {
            // 当前是隐藏状态，切换为显示
            showPassword(vmIndex, passwordField, passwordTextField, toggleBtn);
        }
    }

    /**
     * 显示密码（明文）
     */
    private void showPassword(int vmIndex, PasswordField passwordField, TextField passwordTextField, Button toggleBtn) {
        // 创建TextField用于显示明文密码
        if (passwordTextField == null) {
            passwordTextField = new TextField();
            passwordTextField.setPromptText(passwordField.getPromptText());
            passwordTextField.setPrefWidth(passwordField.getPrefWidth());
            passwordTextField.getStyleClass().addAll(passwordField.getStyleClass());

            // 更新引用
            switch (vmIndex) {
                case 1:
                    vm1PasswordTextField = passwordTextField;
                    break;
                case 2:
                    vm2PasswordTextField = passwordTextField;
                    break;
                case 3:
                    vm3PasswordTextField = passwordTextField;
                    break;
            }
        }

        // 同步密码内容
        passwordTextField.setText(passwordField.getText());

        // 获取PasswordField的父容器
        HBox parent = (HBox) passwordField.getParent();
        int index = parent.getChildren().indexOf(passwordField);

        // 替换为TextField
        parent.getChildren().set(index, passwordTextField);

        // 更新按钮文本
        toggleBtn.setText("隐藏");

        logger.debug("虚拟机{}密码已切换为显示状态", vmIndex);
    }

    /**
     * 隐藏密码（密文）
     */
    private void hidePassword(int vmIndex, PasswordField passwordField, TextField passwordTextField, Button toggleBtn) {
        if (passwordTextField != null) {
            // 同步密码内容
            passwordField.setText(passwordTextField.getText());

            // 获取TextField的父容器
            HBox parent = (HBox) passwordTextField.getParent();
            int index = parent.getChildren().indexOf(passwordTextField);

            // 替换为PasswordField
            parent.getChildren().set(index, passwordField);
        }

        // 更新按钮文本
        toggleBtn.setText("显示");

        logger.debug("虚拟机{}密码已切换为隐藏状态", vmIndex);
    }

    /**
     * 处理虚拟机1单独测试
     */
    @FXML
    private void handleTestVm1Connection() {
        logger.info("用户点击虚拟机1测试按钮");
        testSingleVM(1);
    }

    /**
     * 处理虚拟机2单独测试
     */
    @FXML
    private void handleTestVm2Connection() {
        logger.info("用户点击虚拟机2测试按钮");
        testSingleVM(2);
    }

    /**
     * 处理虚拟机3单独测试
     */
    @FXML
    private void handleTestVm3Connection() {
        logger.info("用户点击虚拟机3测试按钮");
        testSingleVM(3);
    }

    /**
     * 测试单个虚拟机连接
     * 
     * @param vmIndex 虚拟机编号（1-3）
     */
    private void testSingleVM(int vmIndex) {
        // 获取对应的输入框和按钮
        TextField ipField;
        TextField usernameField;
        Button testBtn;
        Label statusLabel;
        Label errorLabel;

        switch (vmIndex) {
            case 1:
                ipField = vm1IpField;
                usernameField = vm1UsernameField;
                testBtn = vm1TestBtn;
                statusLabel = vm1StatusLabel;
                errorLabel = vm1ErrorLabel;
                break;
            case 2:
                ipField = vm2IpField;
                usernameField = vm2UsernameField;
                testBtn = vm2TestBtn;
                statusLabel = vm2StatusLabel;
                errorLabel = vm2ErrorLabel;
                break;
            case 3:
                ipField = vm3IpField;
                usernameField = vm3UsernameField;
                testBtn = vm3TestBtn;
                statusLabel = vm3StatusLabel;
                errorLabel = vm3ErrorLabel;
                break;
            default:
                logger.error("无效的虚拟机编号: {}", vmIndex);
                return;
        }

        // 验证输入
        String ip = ipField.getText();
        String username = usernameField.getText();
        String password = getPassword(vmIndex);

        try {
            validationService.validateIpAddress(ip);
            validationService.validateUsername(username);
            validationService.validatePassword(password);
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "虚拟机" + vmIndex + "输入验证失败", e.getMessage());
            return;
        }

        // 禁用测试按钮，设置测试中样式
        testBtn.setDisable(true);
        testBtn.getStyleClass().removeAll("success", "failure");
        testBtn.getStyleClass().add("testing");
        testBtn.setText("测试中");

        // 更新状态为测试中
        statusLabel.setText("测试中");
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add("status-testing");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // 创建后台任务
        Task<ConnectionResult> testTask = new Task<ConnectionResult>() {
            @Override
            protected ConnectionResult call() throws Exception {
                logger.info("开始测试虚拟机{}连接", vmIndex);

                // 构建配置
                VMConnectionConfig config = new VMConnectionConfig();
                config.setIndex(vmIndex);
                config.setIp(ip.trim());
                config.setHostname("hadoop10" + vmIndex);
                config.setUsername(username.trim());
                config.setPassword(password);
                config.setSshPort(22);
                config.setTimeout(30000);

                // 测试连接
                return sshConnectionService.testConnection(config);
            }

            @Override
            protected void succeeded() {
                logger.info("虚拟机{}连接测试完成", vmIndex);

                ConnectionResult result = getValue();

                // 更新界面显示
                updateConnectionStatus(result);

                // 更新测试按钮状态
                testBtn.setDisable(false);
                testBtn.getStyleClass().remove("testing");

                if (result.getStatus() == ConnectionStatus.SUCCESS) {
                    // 成功：绿色背景，显示"测试成功"
                    testBtn.getStyleClass().add("success");
                    testBtn.setText("测试成功");
                } else {
                    // 失败：红色背景
                    testBtn.getStyleClass().add("failure");
                    testBtn.setText("测试");
                }

                // 如果成功，检查是否所有虚拟机都测试成功
                checkAllConnectionsSuccess();
            }

            @Override
            protected void failed() {
                logger.error("虚拟机{}连接测试失败", vmIndex, getException());

                // 显示错误
                statusLabel.setText("✗ 测试失败");
                statusLabel.getStyleClass().removeAll("status-testing", "status-success");
                statusLabel.getStyleClass().add("status-error");

                errorLabel.setText("测试过程中发生错误: " + getException().getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);

                // 恢复按钮状态，设置失败样式
                testBtn.setDisable(false);
                testBtn.getStyleClass().removeAll("testing", "success");
                testBtn.getStyleClass().add("failure");
                testBtn.setText("测试");
            }
        };

        // 在新线程中执行任务
        Thread testThread = new Thread(testTask);
        testThread.setDaemon(true);
        testThread.start();
    }

    /**
     * 检查所有虚拟机是否都测试成功
     * 如果都成功，启用下一步按钮并保存配置
     */
    private void checkAllConnectionsSuccess() {
        // 检查所有测试按钮的样式类（检查是否都有success样式）
        boolean vm1Success = vm1TestBtn.getStyleClass().contains("success");
        boolean vm2Success = vm2TestBtn.getStyleClass().contains("success");
        boolean vm3Success = vm3TestBtn.getStyleClass().contains("success");

        if (vm1Success && vm2Success && vm3Success) {
            logger.info("所有虚拟机连接测试成功");

            // 启用下一步按钮
            nextBtn.setDisable(false);

            // 保存配置
            saveConfig();

            logger.info("下一步按钮已启用");
        } else {
            logger.debug("还有虚拟机未测试成功: VM1={}, VM2={}, VM3={}", vm1Success, vm2Success, vm3Success);
        }
    }

    /**
     * 处理测试连接按钮点击
     * 在后台线程执行连接测试，避免阻塞UI
     */
    @FXML
    private void handleTestConnection() {
        logger.info("用户点击测试连接按钮");

        // 验证输入
        if (!validateInputs()) {
            return;
        }

        // 禁用测试按钮，显示加载动画
        testConnectionBtn.setDisable(true);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);
        nextBtn.setDisable(true);

        // 重置状态显示
        resetConnectionStatus();

        // 创建后台任务
        Task<List<ConnectionResult>> testTask = new Task<List<ConnectionResult>>() {
            @Override
            protected List<ConnectionResult> call() throws Exception {
                logger.info("开始后台连接测试任务");

                // 构建配置列表
                List<VMConnectionConfig> configs = buildConfigList();

                // 批量测试连接
                return sshConnectionService.testAllConnections(configs);
            }

            @Override
            protected void succeeded() {
                logger.info("连接测试任务完成");

                List<ConnectionResult> results = getValue();

                // 更新界面显示
                for (ConnectionResult result : results) {
                    updateConnectionStatus(result);
                    // 同时更新测试按钮的状态
                    updateTestButtonStatus(result);
                }

                // 检查是否所有连接都成功
                boolean allSuccess = results.stream()
                        .allMatch(r -> r.getStatus() == ConnectionStatus.SUCCESS);

                if (allSuccess) {
                    logger.info("所有虚拟机连接测试成功");

                    // 启用下一步按钮
                    nextBtn.setDisable(false);

                    // 保存配置
                    saveConfig();

                    // 显示成功提示
                    showAlert(Alert.AlertType.INFORMATION, "连接测试成功",
                            "所有虚拟机连接测试成功！\n配置已自动保存，可以进行下一步操作。");
                } else {
                    logger.warn("部分虚拟机连接测试失败");

                    // 显示失败提示
                    showAlert(Alert.AlertType.WARNING, "连接测试失败",
                            "部分虚拟机连接测试失败，请检查失败项并修正后重试。");
                }

                // 恢复按钮状态
                testConnectionBtn.setDisable(false);
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
            }

            @Override
            protected void failed() {
                logger.error("连接测试任务失败", getException());

                // 显示错误提示
                showAlert(Alert.AlertType.ERROR, "连接测试失败",
                        "连接测试过程中发生错误: " + getException().getMessage());

                // 恢复按钮状态
                testConnectionBtn.setDisable(false);
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
            }
        };

        // 在新线程中执行任务
        Thread testThread = new Thread(testTask);
        testThread.setDaemon(true);
        testThread.start();
    }

    /**
     * 验证用户输入
     * 
     * @return true表示验证通过，false表示验证失败
     */
    private boolean validateInputs() {
        logger.debug("验证用户输入");

        // 验证IP地址
        String vm1Ip = vm1IpField.getText();
        String vm2Ip = vm2IpField.getText();
        String vm3Ip = vm3IpField.getText();

        try {
            validationService.validateIpAddress(vm1Ip);
            validationService.validateIpAddress(vm2Ip);
            validationService.validateIpAddress(vm3Ip);
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "输入验证失败", e.getMessage());
            return false;
        }

        // 验证虚拟机1用户名和密码
        try {
            validationService.validateUsername(vm1UsernameField.getText());
            validationService.validatePassword(getPassword(1));
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "虚拟机1输入验证失败", e.getMessage());
            return false;
        }

        // 验证虚拟机2用户名和密码
        try {
            validationService.validateUsername(vm2UsernameField.getText());
            validationService.validatePassword(getPassword(2));
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "虚拟机2输入验证失败", e.getMessage());
            return false;
        }

        // 验证虚拟机3用户名和密码
        try {
            validationService.validateUsername(vm3UsernameField.getText());
            validationService.validatePassword(getPassword(3));
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "虚拟机3输入验证失败", e.getMessage());
            return false;
        }

        logger.debug("用户输入验证通过");
        return true;
    }

    /**
     * 获取指定虚拟机的密码（考虑显示/隐藏状态）
     * 
     * @param vmIndex 虚拟机编号
     * @return 密码字符串
     */
    private String getPassword(int vmIndex) {
        switch (vmIndex) {
            case 1:
                if (isVm1PasswordVisible && vm1PasswordTextField != null) {
                    return vm1PasswordTextField.getText();
                } else {
                    return vm1PasswordField.getText();
                }
            case 2:
                if (isVm2PasswordVisible && vm2PasswordTextField != null) {
                    return vm2PasswordTextField.getText();
                } else {
                    return vm2PasswordField.getText();
                }
            case 3:
                if (isVm3PasswordVisible && vm3PasswordTextField != null) {
                    return vm3PasswordTextField.getText();
                } else {
                    return vm3PasswordField.getText();
                }
            default:
                return "";
        }
    }

    /**
     * 构建虚拟机配置列表
     * 
     * @return 配置列表
     */
    private List<VMConnectionConfig> buildConfigList() {
        List<VMConnectionConfig> configs = new ArrayList<>();

        // 虚拟机1
        VMConnectionConfig config1 = new VMConnectionConfig();
        config1.setIndex(1);
        config1.setIp(vm1IpField.getText().trim());
        config1.setHostname("hadoop101");
        config1.setUsername(vm1UsernameField.getText().trim());
        config1.setPassword(getPassword(1));
        config1.setSshPort(22);
        config1.setTimeout(30000);
        configs.add(config1);

        // 虚拟机2
        VMConnectionConfig config2 = new VMConnectionConfig();
        config2.setIndex(2);
        config2.setIp(vm2IpField.getText().trim());
        config2.setHostname("hadoop102");
        config2.setUsername(vm2UsernameField.getText().trim());
        config2.setPassword(getPassword(2));
        config2.setSshPort(22);
        config2.setTimeout(30000);
        configs.add(config2);

        // 虚拟机3
        VMConnectionConfig config3 = new VMConnectionConfig();
        config3.setIndex(3);
        config3.setIp(vm3IpField.getText().trim());
        config3.setHostname("hadoop103");
        config3.setUsername(vm3UsernameField.getText().trim());
        config3.setPassword(getPassword(3));
        config3.setSshPort(22);
        config3.setTimeout(30000);
        configs.add(config3);

        return configs;
    }

    /**
     * 重置连接状态显示
     */
    private void resetConnectionStatus() {
        // 重置虚拟机1状态
        vm1StatusLabel.setText("测试中");
        vm1StatusLabel.getStyleClass().removeAll("status-success", "status-error");
        vm1StatusLabel.getStyleClass().add("status-testing");
        vm1ErrorLabel.setText("");
        vm1ErrorLabel.setVisible(false);
        vm1ErrorLabel.setManaged(false);

        // 重置虚拟机1测试按钮
        vm1TestBtn.setDisable(true);
        vm1TestBtn.getStyleClass().removeAll("success", "failure");
        vm1TestBtn.getStyleClass().add("testing");
        vm1TestBtn.setText("测试中");

        // 重置虚拟机2状态
        vm2StatusLabel.setText("测试中");
        vm2StatusLabel.getStyleClass().removeAll("status-success", "status-error");
        vm2StatusLabel.getStyleClass().add("status-testing");
        vm2ErrorLabel.setText("");
        vm2ErrorLabel.setVisible(false);
        vm2ErrorLabel.setManaged(false);

        // 重置虚拟机2测试按钮
        vm2TestBtn.setDisable(true);
        vm2TestBtn.getStyleClass().removeAll("success", "failure");
        vm2TestBtn.getStyleClass().add("testing");
        vm2TestBtn.setText("测试中");

        // 重置虚拟机3状态
        vm3StatusLabel.setText("测试中");
        vm3StatusLabel.getStyleClass().removeAll("status-success", "status-error");
        vm3StatusLabel.getStyleClass().add("status-testing");
        vm3ErrorLabel.setText("");
        vm3ErrorLabel.setVisible(false);
        vm3ErrorLabel.setManaged(false);

        // 重置虚拟机3测试按钮
        vm3TestBtn.setDisable(true);
        vm3TestBtn.getStyleClass().removeAll("success", "failure");
        vm3TestBtn.getStyleClass().add("testing");
        vm3TestBtn.setText("测试中");
    }

    /**
     * 更新测试按钮状态
     * 
     * @param result 连接测试结果
     */
    private void updateTestButtonStatus(ConnectionResult result) {
        Button testBtn;

        // 根据虚拟机编号选择对应的按钮
        switch (result.getVmIndex()) {
            case 1:
                testBtn = vm1TestBtn;
                break;
            case 2:
                testBtn = vm2TestBtn;
                break;
            case 3:
                testBtn = vm3TestBtn;
                break;
            default:
                logger.warn("未知的虚拟机编号: {}", result.getVmIndex());
                return;
        }

        // 在JavaFX应用线程中更新UI
        Platform.runLater(() -> {
            testBtn.setDisable(false);
            testBtn.getStyleClass().remove("testing");

            if (result.getStatus() == ConnectionStatus.SUCCESS) {
                // 成功：绿色背景，显示"测试成功"
                testBtn.getStyleClass().removeAll("failure");
                testBtn.getStyleClass().add("success");
                testBtn.setText("测试成功");
            } else {
                // 失败：红色背景
                testBtn.getStyleClass().removeAll("success");
                testBtn.getStyleClass().add("failure");
                testBtn.setText("测试");
            }
        });
    }

    /**
     * 更新连接状态显示
     * 
     * @param result 连接测试结果
     */
    private void updateConnectionStatus(ConnectionResult result) {
        logger.debug("更新虚拟机{}连接状态: {}", result.getVmIndex(), result.getStatus());

        Label statusLabel;
        Label errorLabel;

        // 根据虚拟机编号选择对应的标签
        switch (result.getVmIndex()) {
            case 1:
                statusLabel = vm1StatusLabel;
                errorLabel = vm1ErrorLabel;
                break;
            case 2:
                statusLabel = vm2StatusLabel;
                errorLabel = vm2ErrorLabel;
                break;
            case 3:
                statusLabel = vm3StatusLabel;
                errorLabel = vm3ErrorLabel;
                break;
            default:
                logger.warn("未知的虚拟机编号: {}", result.getVmIndex());
                return;
        }

        // 在JavaFX应用线程中更新UI
        Platform.runLater(() -> {
            if (result.getStatus() == ConnectionStatus.SUCCESS) {
                // 连接成功
                statusLabel.setText("✓ " + result.getMessage());
                statusLabel.getStyleClass().removeAll("status-testing", "status-error");
                statusLabel.getStyleClass().add("status-success");

                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);

            } else {
                // 连接失败
                statusLabel.setText("✗ " + result.getStatus().getDescription());
                statusLabel.getStyleClass().removeAll("status-testing", "status-success");
                statusLabel.getStyleClass().add("status-error");

                errorLabel.setText(result.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });
    }

    /**
     * 保存配置到本地文件
     */
    private void saveConfig() {
        logger.info("保存配置到本地文件");

        try {
            List<VMConnectionConfig> configs = buildConfigList();
            configService.saveConfig(configs);
            logger.info("配置保存成功");
        } catch (IOException e) {
            logger.error("保存配置失败", e);
            showAlert(Alert.AlertType.WARNING, "配置保存失败",
                    "无法保存配置: " + e.getMessage());
        }
    }

    /**
     * 处理下一步按钮点击
     * 跳转到下一个部署步骤界面（预留接口）
     */
    @FXML
    private void handleNext() {
        logger.info("用户点击下一步按钮");

        try {
            // 加载部署模式选择界面
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/deploy-mode.fxml"));
            javafx.scene.Parent root = loader.load();

            // 获取当前Stage
            javafx.stage.Stage stage = (javafx.stage.Stage) nextBtn.getScene().getWindow();

            // 设置新场景
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 780, 600);
            stage.setScene(scene);
            stage.setTitle("Hadoop 分布式集群一键部署系统 - 部署模式选择");

            logger.info("成功跳转到部署模式选择界面");

        } catch (java.io.IOException e) {
            logger.error("加载部署模式选择界面失败", e);
            showAlert(Alert.AlertType.ERROR, "界面加载失败",
                    "无法加载部署模式选择界面: " + e.getMessage());
        }
    }

    /**
     * 获取SSH连接服务实例
     * 供其他模块使用已建立的SSH连接
     * 
     * @return SSH连接服务实例
     */
    public SSHConnectionService getSshConnectionService() {
        return sshConnectionService;
    }

    /**
     * 清理资源
     * 在控制器销毁时调用，关闭所有SSH会话
     */
    public void cleanup() {
        logger.info("清理ConnectionController资源");

        if (sshConnectionService != null) {
            sshConnectionService.closeAllSessions();
        }

        logger.info("资源清理完成");
    }
}
