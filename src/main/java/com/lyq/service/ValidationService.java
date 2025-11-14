package com.lyq.service;

import com.lyq.exception.ValidationException;
import com.lyq.model.VMConnectionConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

/**
 * 验证服务类
 * 负责验证用户输入的连接配置信息
 */
public class ValidationService {

    private static final Logger logger = LogManager.getLogger(ValidationService.class);

    /**
     * IP地址格式正则表达式
     * 验证IPv4地址格式，每个段范围0-255
     */
    private static final String IP_PATTERN = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern ipPattern = Pattern.compile(IP_PATTERN);

    /**
     * 用户名格式正则表达式
     * 允许字母、数字、下划线、连字符，长度1-32
     */
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{1,32}$";

    private static final Pattern usernamePattern = Pattern.compile(USERNAME_PATTERN);

    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址字符串
     * @throws ValidationException 如果IP地址格式不正确
     */
    public void validateIpAddress(String ip) throws ValidationException {
        logger.debug("验证IP地址: {}", ip);

        // 检查是否为空
        if (ip == null || ip.trim().isEmpty()) {
            throw new ValidationException("IP地址不能为空", "ip", ip);
        }

        // 检查格式
        if (!ipPattern.matcher(ip.trim()).matches()) {
            throw new ValidationException("IP地址格式不正确，请输入有效的IPv4地址", "ip", ip);
        }

        logger.debug("IP地址验证通过: {}", ip);
    }

    /**
     * 验证用户名格式
     * 
     * @param username 用户名字符串
     * @throws ValidationException 如果用户名格式不正确
     */
    public void validateUsername(String username) throws ValidationException {
        logger.debug("验证用户名: {}", username);

        // 检查是否为空
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("用户名不能为空", "username", username);
        }

        // 检查格式
        if (!usernamePattern.matcher(username.trim()).matches()) {
            throw new ValidationException(
                    "用户名格式不正确，只能包含字母、数字、下划线和连字符，长度1-32个字符",
                    "username",
                    username);
        }

        logger.debug("用户名验证通过: {}", username);
    }

    /**
     * 验证密码
     * 
     * @param password 密码字符串
     * @throws ValidationException 如果密码为空
     */
    public void validatePassword(String password) throws ValidationException {
        logger.debug("验证密码");

        // 检查是否为空
        if (password == null || password.isEmpty()) {
            throw new ValidationException("密码不能为空", "password", "***");
        }

        logger.debug("密码验证通过");
    }

    /**
     * 验证完整的虚拟机连接配置
     * 
     * @param config 虚拟机连接配置对象
     * @throws ValidationException 如果配置验证失败
     */
    public void validateConfig(VMConnectionConfig config) throws ValidationException {
        logger.debug("验证虚拟机连接配置: {}", config);

        if (config == null) {
            throw new ValidationException("配置对象不能为空", "config", "null");
        }

        // 验证IP地址
        validateIpAddress(config.getIp());

        // 验证用户名
        validateUsername(config.getUsername());

        // 验证密码
        validatePassword(config.getPassword());

        // 验证SSH端口
        if (config.getSshPort() <= 0 || config.getSshPort() > 65535) {
            throw new ValidationException(
                    "SSH端口必须在1-65535之间",
                    "sshPort",
                    String.valueOf(config.getSshPort()));
        }

        // 验证超时时间
        if (config.getTimeout() <= 0) {
            throw new ValidationException(
                    "超时时间必须大于0",
                    "timeout",
                    String.valueOf(config.getTimeout()));
        }

        logger.info("虚拟机连接配置验证通过: VM{} - {}", config.getIndex(), config.getIp());
    }

    /**
     * 检查IP地址格式是否有效（不抛出异常）
     * 
     * @param ip IP地址字符串
     * @return true表示格式有效，false表示格式无效
     */
    public boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        return ipPattern.matcher(ip.trim()).matches();
    }

    /**
     * 检查用户名格式是否有效（不抛出异常）
     * 
     * @param username 用户名字符串
     * @return true表示格式有效，false表示格式无效
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return usernamePattern.matcher(username.trim()).matches();
    }

    /**
     * 验证角色配置
     * 检查必需角色（NameNode、ResourceManager、SecondaryNameNode）是否已分配
     * 
     * @param roleAssignments 角色分配映射（虚拟机编号 -> 角色列表）
     * @throws ValidationException 如果角色配置不合理
     */
    public void validateRoleConfiguration(
            java.util.Map<Integer, java.util.List<com.lyq.model.NodeRole>> roleAssignments)
            throws ValidationException {
        logger.debug("验证角色配置");

        if (roleAssignments == null || roleAssignments.isEmpty()) {
            throw new ValidationException("角色配置不能为空", "roleAssignments", "null");
        }

        // 检查必需角色是否已分配
        boolean hasNameNode = false;
        boolean hasResourceManager = false;
        boolean hasSecondaryNameNode = false;

        for (java.util.List<com.lyq.model.NodeRole> roles : roleAssignments.values()) {
            if (roles.contains(com.lyq.model.NodeRole.NAMENODE)) {
                hasNameNode = true;
            }
            if (roles.contains(com.lyq.model.NodeRole.RESOURCEMANAGER)) {
                hasResourceManager = true;
            }
            if (roles.contains(com.lyq.model.NodeRole.SECONDARYNAMENODE)) {
                hasSecondaryNameNode = true;
            }
        }

        // 验证必需角色
        if (!hasNameNode) {
            throw new ValidationException(
                    "必须为至少一台虚拟机分配NameNode角色",
                    "roleAssignments",
                    "missing NameNode");
        }

        if (!hasResourceManager) {
            throw new ValidationException(
                    "必须为至少一台虚拟机分配ResourceManager角色",
                    "roleAssignments",
                    "missing ResourceManager");
        }

        if (!hasSecondaryNameNode) {
            throw new ValidationException(
                    "必须为至少一台虚拟机分配SecondaryNameNode角色",
                    "roleAssignments",
                    "missing SecondaryNameNode");
        }

        // 验证所有虚拟机都有DataNode角色
        for (int i = 1; i <= 3; i++) {
            java.util.List<com.lyq.model.NodeRole> roles = roleAssignments.get(i);
            if (roles == null || !roles.contains(com.lyq.model.NodeRole.DATANODE)) {
                throw new ValidationException(
                        "虚拟机" + i + "必须包含DataNode角色",
                        "roleAssignments",
                        "missing DataNode on VM" + i);
            }
        }

        logger.info("角色配置验证通过");
    }
}
