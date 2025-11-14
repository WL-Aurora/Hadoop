package com.lyq.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.lyq.exception.ConnectionException;
import com.lyq.model.ConnectionResult;
import com.lyq.model.ConnectionStatus;
import com.lyq.model.VMConnectionConfig;
import com.lyq.util.NetworkUtil;
import com.lyq.util.SSHUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH连接服务类
 * 负责管理SSH连接的创建、测试和维护
 */
public class SSHConnectionService {

    private static final Logger logger = LogManager.getLogger(SSHConnectionService.class);

    /**
     * SSH会话缓存
     * Key: 虚拟机IP地址
     * Value: SSH会话对象
     */
    private final Map<String, Session> sessionCache = new ConcurrentHashMap<>();

    /**
     * 最大重连次数
     */
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    /**
     * 测试单个虚拟机连接
     * 
     * @param config 虚拟机连接配置
     * @return 连接测试结果
     */
    public ConnectionResult testConnection(VMConnectionConfig config) {
        logger.info("开始测试虚拟机连接: VM{} - {}", config.getIndex(), config.getIp());

        long startTime = System.currentTimeMillis();
        String vmIp = config.getIp();
        int vmIndex = config.getIndex();

        try {
            // 步骤1: 检查网络可达性
            logger.debug("检查网络可达性: {}", vmIp);
            if (!NetworkUtil.isReachable(vmIp, 5000)) {
                logger.warn("虚拟机{}网络不可达", vmIp);
                return ConnectionResult.failure(
                        vmIndex,
                        vmIp,
                        ConnectionStatus.NETWORK_UNREACHABLE,
                        "网络不通，请检查IP地址和网络配置",
                        null);
            }

            // 步骤2: 检查SSH端口
            logger.debug("检查SSH端口: {}:{}", vmIp, config.getSshPort());
            if (!NetworkUtil.isPortOpen(vmIp, config.getSshPort(), 5000)) {
                logger.warn("虚拟机{}的SSH端口{}未开放", vmIp, config.getSshPort());
                return ConnectionResult.failure(
                        vmIndex,
                        vmIp,
                        ConnectionStatus.SSH_SERVICE_DOWN,
                        "SSH服务未启动，请在虚拟机中启动sshd服务",
                        null);
            }

            // 步骤3: 尝试SSH连接
            logger.debug("尝试建立SSH连接: {}", vmIp);
            Session session = SSHUtil.createSession(
                    vmIp,
                    config.getSshPort(),
                    config.getUsername(),
                    config.getPassword());

            // 设置超时时间
            session.setTimeout(config.getTimeout());

            // 连接
            session.connect();

            // 步骤4: 执行测试命令验证连接
            logger.debug("执行测试命令验证连接: {}", vmIp);
            String testResult = SSHUtil.executeCommand(session, "echo 'connection_test'");

            if (testResult == null || !testResult.contains("connection_test")) {
                logger.warn("虚拟机{}连接验证失败", vmIp);
                SSHUtil.closeSession(session);
                return ConnectionResult.failure(
                        vmIndex,
                        vmIp,
                        ConnectionStatus.UNKNOWN_ERROR,
                        "连接验证失败",
                        "测试命令执行失败");
            }

            // 步骤5: 缓存会话
            sessionCache.put(vmIp, session);

            long responseTime = System.currentTimeMillis() - startTime;
            logger.info("虚拟机{}连接测试成功，响应时间: {}ms", vmIp, responseTime);

            return ConnectionResult.success(
                    vmIndex,
                    vmIp,
                    ConnectionStatus.SUCCESS,
                    "连接成功",
                    responseTime);

        } catch (JSchException e) {
            logger.error("虚拟机{}连接失败: {}", vmIp, e.getMessage());

            // 根据异常类型判断失败原因
            String errorMessage = e.getMessage().toLowerCase();

            if (errorMessage.contains("auth") || errorMessage.contains("password")) {
                return ConnectionResult.failure(
                        vmIndex,
                        vmIp,
                        ConnectionStatus.AUTH_FAILED,
                        "用户名或密码错误，请检查登录凭证",
                        e.getMessage());
            } else if (errorMessage.contains("timeout")) {
                return ConnectionResult.failure(
                        vmIndex,
                        vmIp,
                        ConnectionStatus.TIMEOUT,
                        "连接超时，请检查网络状态和防火墙设置",
                        e.getMessage());
            } else {
                return ConnectionResult.failure(
                        vmIndex,
                        vmIp,
                        ConnectionStatus.UNKNOWN_ERROR,
                        "连接失败: " + e.getMessage(),
                        e.getMessage());
            }

        } catch (SocketTimeoutException e) {
            logger.error("虚拟机{}连接超时", vmIp, e);
            return ConnectionResult.failure(
                    vmIndex,
                    vmIp,
                    ConnectionStatus.TIMEOUT,
                    "连接超时，请检查网络状态和防火墙设置",
                    e.getMessage());

        } catch (IOException e) {
            logger.error("虚拟机{}连接失败", vmIp, e);
            return ConnectionResult.failure(
                    vmIndex,
                    vmIp,
                    ConnectionStatus.UNKNOWN_ERROR,
                    "连接失败: " + e.getMessage(),
                    e.getMessage());

        } catch (Exception e) {
            logger.error("虚拟机{}连接测试发生未知错误", vmIp, e);
            return ConnectionResult.failure(
                    vmIndex,
                    vmIp,
                    ConnectionStatus.UNKNOWN_ERROR,
                    "未知错误: " + e.getMessage(),
                    e.getMessage());
        }
    }

    /**
     * 批量测试所有虚拟机连接
     * 
     * @param configs 虚拟机连接配置列表
     * @return 连接测试结果列表
     */
    public List<ConnectionResult> testAllConnections(List<VMConnectionConfig> configs) {
        logger.info("开始批量测试{}台虚拟机连接", configs.size());

        List<ConnectionResult> results = new ArrayList<>();

        for (VMConnectionConfig config : configs) {
            ConnectionResult result = testConnection(config);
            results.add(result);
        }

        // 统计结果
        long successCount = results.stream()
                .filter(r -> r.getStatus() == ConnectionStatus.SUCCESS)
                .count();

        logger.info("批量连接测试完成，成功: {}/{}", successCount, configs.size());

        return results;
    }

    /**
     * 获取或创建SSH会话
     * 如果会话已存在且连接正常，则返回缓存的会话
     * 否则创建新会话
     * 
     * @param config 虚拟机连接配置
     * @return SSH会话对象
     * @throws ConnectionException 如果连接失败
     */
    public Session getSession(VMConnectionConfig config) throws ConnectionException {
        String vmIp = config.getIp();

        // 检查缓存中是否存在会话
        Session session = sessionCache.get(vmIp);

        if (session != null && SSHUtil.isSessionConnected(session)) {
            logger.debug("使用缓存的SSH会话: {}", vmIp);
            return session;
        }

        // 创建新会话
        logger.info("创建新的SSH会话: {}", vmIp);

        try {
            session = SSHUtil.createSession(
                    vmIp,
                    config.getSshPort(),
                    config.getUsername(),
                    config.getPassword());

            session.setTimeout(config.getTimeout());
            session.connect();

            // 缓存会话
            sessionCache.put(vmIp, session);

            logger.info("SSH会话创建成功: {}", vmIp);
            return session;

        } catch (JSchException e) {
            logger.error("创建SSH会话失败: {}", vmIp, e);

            String errorMessage = e.getMessage().toLowerCase();
            ConnectionStatus status;

            if (errorMessage.contains("auth") || errorMessage.contains("password")) {
                status = ConnectionStatus.AUTH_FAILED;
            } else if (errorMessage.contains("timeout")) {
                status = ConnectionStatus.TIMEOUT;
            } else {
                status = ConnectionStatus.UNKNOWN_ERROR;
            }

            throw new ConnectionException(
                    "创建SSH会话失败: " + e.getMessage(),
                    e,
                    status,
                    vmIp);
        }
    }

    /**
     * 关闭指定虚拟机的SSH会话
     * 
     * @param vmIp 虚拟机IP地址
     */
    public void closeSession(String vmIp) {
        Session session = sessionCache.remove(vmIp);

        if (session != null) {
            SSHUtil.closeSession(session);
            logger.info("SSH会话已关闭: {}", vmIp);
        } else {
            logger.debug("虚拟机{}没有活动的SSH会话", vmIp);
        }
    }

    /**
     * 关闭所有SSH会话
     */
    public void closeAllSessions() {
        logger.info("开始关闭所有SSH会话，共{}个", sessionCache.size());

        for (Map.Entry<String, Session> entry : sessionCache.entrySet()) {
            String vmIp = entry.getKey();
            Session session = entry.getValue();

            SSHUtil.closeSession(session);
            logger.debug("SSH会话已关闭: {}", vmIp);
        }

        sessionCache.clear();
        logger.info("所有SSH会话已关闭");
    }

    /**
     * 执行远程命令
     * 
     * @param vmIp    虚拟机IP地址
     * @param command 要执行的命令
     * @return 命令执行结果
     * @throws ConnectionException 如果执行失败
     */
    public String executeCommand(String vmIp, String command) throws ConnectionException {
        Session session = sessionCache.get(vmIp);

        if (session == null || !SSHUtil.isSessionConnected(session)) {
            throw new ConnectionException(
                    "SSH会话不存在或已断开: " + vmIp,
                    ConnectionStatus.UNKNOWN_ERROR,
                    vmIp);
        }

        try {
            return SSHUtil.executeCommand(session, command);
        } catch (JSchException | IOException e) {
            logger.error("执行远程命令失败: {}", vmIp, e);
            throw new ConnectionException(
                    "执行远程命令失败: " + e.getMessage(),
                    e,
                    ConnectionStatus.UNKNOWN_ERROR,
                    vmIp);
        }
    }

    /**
     * 自动重连
     * 
     * @param config     虚拟机连接配置
     * @param maxRetries 最大重试次数
     * @return SSH会话对象，如果重连失败则返回null
     */
    private Session reconnect(VMConnectionConfig config, int maxRetries) {
        String vmIp = config.getIp();
        logger.info("开始自动重连: {}，最大重试次数: {}", vmIp, maxRetries);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("重连尝试 {}/{}: {}", attempt, maxRetries, vmIp);

                Session session = SSHUtil.createSession(
                        vmIp,
                        config.getSshPort(),
                        config.getUsername(),
                        config.getPassword());

                session.setTimeout(config.getTimeout());
                session.connect();

                // 缓存会话
                sessionCache.put(vmIp, session);

                logger.info("重连成功: {}，尝试次数: {}", vmIp, attempt);
                return session;

            } catch (JSchException e) {
                logger.warn("重连失败 {}/{}: {}", attempt, maxRetries, vmIp, e);

                if (attempt < maxRetries) {
                    // 等待一段时间后重试
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("重连等待被中断", ie);
                        break;
                    }
                }
            }
        }

        logger.error("重连失败，已达到最大重试次数: {}", vmIp);
        return null;
    }

    /**
     * 尝试重连指定虚拟机
     * 
     * @param config 虚拟机连接配置
     * @return 重连是否成功
     */
    public boolean tryReconnect(VMConnectionConfig config) {
        Session session = reconnect(config, MAX_RECONNECT_ATTEMPTS);
        return session != null && SSHUtil.isSessionConnected(session);
    }

    /**
     * 获取当前活动的会话数量
     * 
     * @return 活动会话数量
     */
    public int getActiveSessionCount() {
        int count = 0;
        for (Session session : sessionCache.values()) {
            if (SSHUtil.isSessionConnected(session)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 检查指定虚拟机的会话是否活动
     * 
     * @param vmIp 虚拟机IP地址
     * @return true表示会话活动，false表示会话不活动或不存在
     */
    public boolean isSessionActive(String vmIp) {
        Session session = sessionCache.get(vmIp);
        return session != null && SSHUtil.isSessionConnected(session);
    }

    /**
     * 执行命令并实时输出日志
     * 
     * @param session     SSH会话
     * @param command     要执行的命令
     * @param logCallback 日志回调接口
     * @return 命令执行结果
     */
    public com.lyq.model.CommandResult executeCommandWithLog(Session session, String command,
            LogCallback logCallback) {
        logger.info("执行命令（带日志）: {}", command);

        com.jcraft.jsch.ChannelExec execChannel = null;
        long startTime = System.currentTimeMillis();

        try {
            execChannel = (com.jcraft.jsch.ChannelExec) session.openChannel("exec");
            execChannel.setCommand(command);

            java.io.InputStream in = execChannel.getInputStream();
            java.io.InputStream err = execChannel.getErrStream();

            execChannel.connect();

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            byte[] buffer = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int len = in.read(buffer);
                    if (len < 0)
                        break;
                    String line = new String(buffer, 0, len);
                    output.append(line);
                    if (logCallback != null) {
                        logCallback.onLog(line);
                    }
                }

                while (err.available() > 0) {
                    int len = err.read(buffer);
                    if (len < 0)
                        break;
                    String line = new String(buffer, 0, len);
                    error.append(line);
                    if (logCallback != null) {
                        logCallback.onError(line);
                    }
                }

                if (execChannel.isClosed()) {
                    if (in.available() > 0 || err.available() > 0) {
                        continue;
                    }
                    break;
                }

                Thread.sleep(100);
            }

            int exitCode = execChannel.getExitStatus();
            long executionTime = System.currentTimeMillis() - startTime;

            if (logCallback != null) {
                logCallback.onComplete();
            }

            logger.info("命令执行完成，退出码: {}, 耗时: {}ms", exitCode, executionTime);

            return new com.lyq.model.CommandResult(
                    command,
                    exitCode,
                    output.toString(),
                    error.toString(),
                    executionTime,
                    exitCode == 0);

        } catch (Exception e) {
            logger.error("命令执行失败: {}", command, e);
            if (logCallback != null) {
                logCallback.onError("命令执行异常: " + e.getMessage());
            }
            return com.lyq.model.CommandResult.failure(command, e.getMessage());
        } finally {
            if (execChannel != null && execChannel.isConnected()) {
                execChannel.disconnect();
            }
        }
    }

    /**
     * 批量执行命令
     * 
     * @param session     SSH会话
     * @param commands    命令列表
     * @param logCallback 日志回调接口
     * @return 执行结果列表
     */
    public List<com.lyq.model.CommandResult> executeCommands(Session session,
            List<String> commands,
            LogCallback logCallback) {
        logger.info("批量执行{}条命令", commands.size());

        List<com.lyq.model.CommandResult> results = new ArrayList<>();

        for (String command : commands) {
            com.lyq.model.CommandResult result = executeCommandWithLog(session, command, logCallback);
            results.add(result);

            if (!result.isSuccess()) {
                logger.warn("命令执行失败，停止后续命令: {}", command);
                break;
            }
        }

        return results;
    }

}
