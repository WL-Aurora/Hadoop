package com.lyq.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.lyq.model.VMConnectionConfig;
import com.lyq.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置管理服务类
 * 负责虚拟机连接配置的持久化存储和加载
 */
public class ConfigService {

    private static final Logger logger = LogManager.getLogger(ConfigService.class);

    /**
     * 配置文件路径
     */
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".hads";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.json";
    private static final String DEPLOY_MODE_CONFIG_FILE = CONFIG_DIR + File.separator + "deploy-mode-config.json";

    private static final String CONFIG_VERSION = "1.0";

    private final Gson gson;

    public ConfigService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * 保存虚拟机连接配置到本地文件
     * 
     * @param configs 虚拟机连接配置列表
     * @throws IOException 如果保存失败
     */
    public void saveConfig(List<VMConnectionConfig> configs) throws IOException {
        logger.info("开始保存配置到文件: {}", CONFIG_FILE);

        try {
            // 确保配置目录存在
            ensureConfigDirectoryExists();

            // 构建配置数据结构
            Map<String, Object> configData = new HashMap<>();
            configData.put("version", CONFIG_VERSION);
            configData.put("lastModified", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 加密密码并构建虚拟机配置列表
            List<Map<String, Object>> vmList = new ArrayList<>();
            for (VMConnectionConfig config : configs) {
                Map<String, Object> vmData = new HashMap<>();
                vmData.put("index", config.getIndex());
                vmData.put("ip", config.getIp());
                vmData.put("hostname", config.getHostname());
                vmData.put("username", config.getUsername());

                // 加密密码
                try {
                    String encryptedPassword = EncryptionUtil.encrypt(config.getPassword());
                    vmData.put("password", encryptedPassword);
                } catch (Exception e) {
                    logger.error("加密密码失败", e);
                    throw new IOException("加密密码失败: " + e.getMessage(), e);
                }

                vmData.put("sshPort", config.getSshPort());
                vmData.put("timeout", config.getTimeout());

                vmList.add(vmData);
            }

            configData.put("vms", vmList);

            // 序列化为JSON并写入文件
            String jsonContent = gson.toJson(configData);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
                writer.write(jsonContent);
            }

            logger.info("配置保存成功，共{}台虚拟机", configs.size());

        } catch (IOException e) {
            logger.error("保存配置失败", e);
            throw e;
        }
    }

    /**
     * 从本地文件加载虚拟机连接配置
     * 
     * @return 虚拟机连接配置列表
     * @throws IOException 如果加载失败
     */
    @SuppressWarnings("unchecked")
    public List<VMConnectionConfig> loadConfig() throws IOException {
        logger.info("开始从文件加载配置: {}", CONFIG_FILE);

        // 检查配置文件是否存在
        if (!configExists()) {
            logger.warn("配置文件不存在，返回默认配置");
            return createDefaultConfig();
        }

        try {
            // 读取JSON文件
            String jsonContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                jsonContent = sb.toString();
            }

            // 解析JSON
            Map<String, Object> configData = gson.fromJson(jsonContent, Map.class);

            if (configData == null || !configData.containsKey("vms")) {
                logger.warn("配置文件格式不正确，返回默认配置");
                return createDefaultConfig();
            }

            // 解析虚拟机配置列表
            List<Map<String, Object>> vmList = (List<Map<String, Object>>) configData.get("vms");
            List<VMConnectionConfig> configs = new ArrayList<>();

            for (Map<String, Object> vmData : vmList) {
                VMConnectionConfig config = new VMConnectionConfig();

                // 解析基本信息
                config.setIndex(((Double) vmData.get("index")).intValue());
                config.setIp((String) vmData.get("ip"));
                config.setHostname((String) vmData.get("hostname"));
                config.setUsername((String) vmData.get("username"));

                // 解密密码
                String encryptedPassword = (String) vmData.get("password");
                try {
                    String decryptedPassword = EncryptionUtil.decrypt(encryptedPassword);
                    config.setPassword(decryptedPassword);
                } catch (Exception e) {
                    logger.error("解密密码失败", e);
                    throw new IOException("解密密码失败: " + e.getMessage(), e);
                }

                // 解析端口和超时设置
                if (vmData.containsKey("sshPort")) {
                    config.setSshPort(((Double) vmData.get("sshPort")).intValue());
                }

                if (vmData.containsKey("timeout")) {
                    config.setTimeout(((Double) vmData.get("timeout")).intValue());
                }

                configs.add(config);
            }

            logger.info("配置加载成功，共{}台虚拟机", configs.size());
            return configs;

        } catch (JsonSyntaxException e) {
            logger.error("配置文件JSON格式错误", e);
            throw new IOException("配置文件损坏，JSON格式错误: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("加载配置失败", e);
            throw new IOException("加载配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查配置文件是否存在
     * 
     * @return true表示存在，false表示不存在
     */
    public boolean configExists() {
        File configFile = new File(CONFIG_FILE);
        boolean exists = configFile.exists() && configFile.isFile();
        logger.debug("配置文件{}存在", exists ? "" : "不");
        return exists;
    }

    /**
     * 创建默认配置
     * 
     * @return 默认的虚拟机连接配置列表（3台虚拟机，信息为空）
     */
    public List<VMConnectionConfig> createDefaultConfig() {
        logger.info("创建默认配置");

        List<VMConnectionConfig> configs = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            VMConnectionConfig config = new VMConnectionConfig();
            config.setIndex(i);
            config.setIp("");
            config.setHostname("hadoop10" + i);
            config.setUsername("");
            config.setPassword("");
            config.setSshPort(22);
            config.setTimeout(30000);

            configs.add(config);
        }

        return configs;
    }

    /**
     * 确保配置目录存在，如果不存在则创建
     * 
     * @throws IOException 如果创建目录失败
     */
    private void ensureConfigDirectoryExists() throws IOException {
        Path configPath = Paths.get(CONFIG_DIR);

        if (!Files.exists(configPath)) {
            logger.info("配置目录不存在，创建目录: {}", CONFIG_DIR);
            try {
                Files.createDirectories(configPath);
            } catch (IOException e) {
                logger.error("创建配置目录失败", e);
                throw new IOException("创建配置目录失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 删除配置文件
     * 
     * @return true表示删除成功，false表示删除失败
     */
    public boolean deleteConfig() {
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            boolean deleted = configFile.delete();
            if (deleted) {
                logger.info("配置文件已删除");
            } else {
                logger.warn("配置文件删除失败");
            }
            return deleted;
        }

        logger.debug("配置文件不存在，无需删除");
        return true;
    }

    /**
     * 保存部署模式配置到本地文件
     * 
     * @param config 部署模式配置
     * @throws IOException 如果保存失败
     */
    public void saveDeployModeConfig(com.lyq.model.DeployModeConfig config) throws IOException {
        logger.info("开始保存部署模式配置到文件: {}", DEPLOY_MODE_CONFIG_FILE);

        try {
            // 确保配置目录存在
            ensureConfigDirectoryExists();

            // 构建配置数据结构
            Map<String, Object> configData = new HashMap<>();
            configData.put("version", CONFIG_VERSION);
            configData.put("lastModified", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configData.put("deployMode", config.getMode().name());

            // 构建角色分配数据
            Map<String, List<String>> roleAssignmentsData = new HashMap<>();
            for (Map.Entry<Integer, List<com.lyq.model.NodeRole>> entry : config.getRoleAssignments().entrySet()) {
                List<String> roleNames = new ArrayList<>();
                for (com.lyq.model.NodeRole role : entry.getValue()) {
                    roleNames.add(role.name());
                }
                roleAssignmentsData.put(String.valueOf(entry.getKey()), roleNames);
            }

            configData.put("roleAssignments", roleAssignmentsData);

            // 序列化为JSON并写入文件
            String jsonContent = gson.toJson(configData);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(DEPLOY_MODE_CONFIG_FILE), StandardCharsets.UTF_8))) {
                writer.write(jsonContent);
            }

            logger.info("部署模式配置保存成功，模式: {}", config.getMode());

        } catch (IOException e) {
            logger.error("保存部署模式配置失败", e);
            throw e;
        }
    }

    /**
     * 从本地文件加载部署模式配置
     * 
     * @return 部署模式配置
     * @throws IOException 如果加载失败
     */
    @SuppressWarnings("unchecked")
    public com.lyq.model.DeployModeConfig loadDeployModeConfig() throws IOException {
        logger.info("开始从文件加载部署模式配置: {}", DEPLOY_MODE_CONFIG_FILE);

        // 检查配置文件是否存在
        if (!deployModeConfigExists()) {
            logger.warn("部署模式配置文件不存在，返回默认配置");
            return new com.lyq.model.DeployModeConfig();
        }

        try {
            // 读取JSON文件
            String jsonContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(DEPLOY_MODE_CONFIG_FILE), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                jsonContent = sb.toString();
            }

            // 解析JSON
            Map<String, Object> configData = gson.fromJson(jsonContent, Map.class);

            if (configData == null) {
                logger.warn("部署模式配置文件格式不正确，返回默认配置");
                return new com.lyq.model.DeployModeConfig();
            }

            // 创建配置对象
            com.lyq.model.DeployModeConfig config = new com.lyq.model.DeployModeConfig();

            // 解析部署模式
            if (configData.containsKey("deployMode")) {
                String modeStr = (String) configData.get("deployMode");
                try {
                    com.lyq.model.DeployMode mode = com.lyq.model.DeployMode.valueOf(modeStr);
                    config.setMode(mode);
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的部署模式: {}, 使用默认模式", modeStr);
                }
            }

            // 解析角色分配
            if (configData.containsKey("roleAssignments")) {
                Map<String, List<String>> roleAssignmentsData = (Map<String, List<String>>) configData
                        .get("roleAssignments");

                Map<Integer, List<com.lyq.model.NodeRole>> roleAssignments = new HashMap<>();

                for (Map.Entry<String, List<String>> entry : roleAssignmentsData.entrySet()) {
                    int vmIndex = Integer.parseInt(entry.getKey());
                    List<com.lyq.model.NodeRole> roles = new ArrayList<>();

                    for (String roleName : entry.getValue()) {
                        try {
                            com.lyq.model.NodeRole role = com.lyq.model.NodeRole.valueOf(roleName);
                            roles.add(role);
                        } catch (IllegalArgumentException e) {
                            logger.warn("无效的角色名称: {}", roleName);
                        }
                    }

                    roleAssignments.put(vmIndex, roles);
                }

                config.setRoleAssignments(roleAssignments);
            }

            logger.info("部署模式配置加载成功，模式: {}", config.getMode());
            return config;

        } catch (JsonSyntaxException e) {
            logger.error("部署模式配置文件JSON格式错误", e);
            throw new IOException("配置文件损坏，JSON格式错误: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("加载部署模式配置失败", e);
            throw new IOException("加载配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查部署模式配置文件是否存在
     * 
     * @return true表示存在，false表示不存在
     */
    public boolean deployModeConfigExists() {
        File configFile = new File(DEPLOY_MODE_CONFIG_FILE);
        boolean exists = configFile.exists() && configFile.isFile();
        logger.debug("部署模式配置文件{}存在", exists ? "" : "不");
        return exists;
    }
}
