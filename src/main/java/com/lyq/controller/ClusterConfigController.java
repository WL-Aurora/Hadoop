package com.lyq.controller;

import com.lyq.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 集群参数配置界面控制器（重构版）
 */
public class ClusterConfigController {

    private static final Logger logger = LogManager.getLogger(ClusterConfigController.class);

    // 网络配置
    @FXML
    private TextField vm1IpField;
    @FXML
    private TextField vm1HostnameField;
    @FXML
    private TextField vm2IpField;
    @FXML
    private TextField vm2HostnameField;
    @FXML
    private TextField vm3IpField;
    @FXML
    private TextField vm3HostnameField;

    // JDK配置
    @FXML
    private RadioButton jdkPresetRadio;
    @FXML
    private RadioButton jdkUploadRadio;
    @FXML
    private ToggleGroup jdkSourceGroup;
    @FXML
    private ComboBox<String> jdkVersionCombo;
    @FXML
    private TextField jdkFilePathField;
    @FXML
    private Button jdkBrowseBtn;

    // Hadoop配置
    @FXML
    private RadioButton hadoopPresetRadio;
    @FXML
    private RadioButton hadoopUploadRadio;
    @FXML
    private ToggleGroup hadoopSourceGroup;
    @FXML
    private ComboBox<String> hadoopVersionCombo;
    @FXML
    private TextField hadoopFilePathField;
    @FXML
    private Button hadoopBrowseBtn;

    // 其他配置
    @FXML
    private ComboBox<String> hdfsBlockSizeCombo;
    @FXML
    private ComboBox<String> yarnMemoryCombo;

    // 按钮
    @FXML
    private Button previousBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button startDeployBtn;

    // 连接信息存储
    private String[] vmIps = new String[3];
    private String[] vmUsernames = new String[3];
    private String[] vmPasswords = new String[3];

    @FXML
    public void initialize() {
        logger.info("初始化集群参数配置界面");

        // 初始化下拉框
        jdkVersionCombo.setItems(FXCollections.observableArrayList(
                "JDK 1.8.0_212", "JDK 1.8.0_281", "JDK 1.8.0_301"));
        jdkVersionCombo.setValue("JDK 1.8.0_212");

        hadoopVersionCombo.setItems(FXCollections.observableArrayList(
                "Hadoop 3.1.3", "Hadoop 2.7.7", "Hadoop 3.3.4"));
        hadoopVersionCombo.setValue("Hadoop 3.1.3");

        hdfsBlockSizeCombo.setItems(FXCollections.observableArrayList(
                "64MB", "128MB", "256MB"));
        hdfsBlockSizeCombo.setValue("128MB");

        yarnMemoryCombo.setItems(FXCollections.observableArrayList(
                "1GB", "2GB", "4GB", "8GB"));
        yarnMemoryCombo.setValue("2GB");

        setupRadioButtonListeners();

        logger.info("集群参数配置界面初始化完成");
    }

    private void setupRadioButtonListeners() {
        // 只有当新控件存在时才设置监听器（兼容旧FXML）
        if (jdkSourceGroup != null) {
            jdkSourceGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
                boolean isUpload = newVal == jdkUploadRadio;
                if (jdkVersionCombo != null)
                    jdkVersionCombo.setDisable(isUpload);
                if (jdkFilePathField != null)
                    jdkFilePathField.setDisable(!isUpload);
                if (jdkBrowseBtn != null)
                    jdkBrowseBtn.setDisable(!isUpload);
            });
        }

        if (hadoopSourceGroup != null) {
            hadoopSourceGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
                boolean isUpload = newVal == hadoopUploadRadio;
                if (hadoopVersionCombo != null)
                    hadoopVersionCombo.setDisable(isUpload);
                if (hadoopFilePathField != null)
                    hadoopFilePathField.setDisable(!isUpload);
                if (hadoopBrowseBtn != null)
                    hadoopBrowseBtn.setDisable(!isUpload);
            });
        }
    }

    public void setVmConnectionInfo(String vm1Ip, String vm2Ip, String vm3Ip,
            String vm1User, String vm2User, String vm3User,
            String vm1Pass, String vm2Pass, String vm3Pass) {
        vm1IpField.setText(vm1Ip);
        vm2IpField.setText(vm2Ip);
        vm3IpField.setText(vm3Ip);

        vmIps[0] = vm1Ip;
        vmIps[1] = vm2Ip;
        vmIps[2] = vm3Ip;
        vmUsernames[0] = vm1User;
        vmUsernames[1] = vm2User;
        vmUsernames[2] = vm3User;
        vmPasswords[0] = vm1Pass;
        vmPasswords[1] = vm2Pass;
        vmPasswords[2] = vm3Pass;

        logger.info("已设置虚拟机连接信息");
    }

    @FXML
    private void handleBrowseJdk() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择JDK安装包");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("压缩文件", "*.tar.gz", "*.tgz", "*.zip"),
                new FileChooser.ExtensionFilter("所有文件", "*.*"));

        File file = fileChooser.showOpenDialog(jdkBrowseBtn.getScene().getWindow());
        if (file != null) {
            jdkFilePathField.setText(file.getAbsolutePath());
            logger.info("选择JDK文件: {}", file.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseHadoop() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择Hadoop安装包");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("压缩文件", "*.tar.gz", "*.tgz", "*.zip"),
                new FileChooser.ExtensionFilter("所有文件", "*.*"));

        File file = fileChooser.showOpenDialog(hadoopBrowseBtn.getScene().getWindow());
        if (file != null) {
            hadoopFilePathField.setText(file.getAbsolutePath());
            logger.info("选择Hadoop文件: {}", file.getAbsolutePath());
        }
    }

    @FXML
    private void handlePrevious() {
        logger.info("返回部署模式选择界面");
        showAlert(Alert.AlertType.INFORMATION, "提示", "返回功能开发中");
    }

    @FXML
    private void handleCancel() {
        logger.info("取消部署");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认取消");
        alert.setContentText("确定要取消部署吗？");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.exit(0);
            }
        });
    }

    @FXML
    private void handleStartDeploy() {
        logger.info("开始部署");

        if (!validateConfiguration()) {
            return;
        }

        DeploymentConfig config = buildDeploymentConfig();

        showAlert(Alert.AlertType.INFORMATION, "提示", "部署功能开发中");
    }

    private boolean validateConfiguration() {
        if (vm1HostnameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "验证失败", "虚拟机1主机名不能为空");
            return false;
        }

        if (jdkUploadRadio.isSelected() && jdkFilePathField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "验证失败", "请选择JDK文件");
            return false;
        }

        if (hadoopUploadRadio.isSelected() && hadoopFilePathField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "验证失败", "请选择Hadoop文件");
            return false;
        }

        return true;
    }

    private DeploymentConfig buildDeploymentConfig() {
        DeploymentConfig config = new DeploymentConfig();

        // 构建VM配置
        List<VMConnectionConfig> vmConfigs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            VMConnectionConfig vmConfig = new VMConnectionConfig();
            vmConfig.setIndex(i + 1);
            vmConfig.setIp(vmIps[i]);
            vmConfig.setUsername(vmUsernames[i]);
            vmConfig.setPassword(vmPasswords[i]);
            vmConfigs.add(vmConfig);
        }
        config.setVmConfigs(vmConfigs);

        // 设置主机名
        config.setHostnames(new String[] {
                vm1HostnameField.getText(),
                vm2HostnameField.getText(),
                vm3HostnameField.getText()
        });

        // JDK配置
        JDKConfig jdkConfig = new JDKConfig();
        jdkConfig.setSourceType(jdkPresetRadio.isSelected() ? SourceType.PRESET : SourceType.LOCAL_FILE);
        jdkConfig.setPresetVersion(jdkVersionCombo.getValue());
        jdkConfig.setLocalFilePath(jdkFilePathField.getText());
        config.setJdkConfig(jdkConfig);

        // Hadoop配置
        HadoopConfig hadoopConfig = new HadoopConfig();
        hadoopConfig.setSourceType(hadoopPresetRadio.isSelected() ? SourceType.PRESET : SourceType.LOCAL_FILE);
        hadoopConfig.setPresetVersion(hadoopVersionCombo.getValue());
        hadoopConfig.setLocalFilePath(hadoopFilePathField.getText());
        hadoopConfig.setHdfsBlockSize(hdfsBlockSizeCombo.getValue());
        hadoopConfig.setYarnMemory(yarnMemoryCombo.getValue());
        config.setHadoopConfig(hadoopConfig);

        return config;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
