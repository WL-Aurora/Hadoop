package com.lyq.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * SSH工具类
 * 提供SSH连接和远程命令执行功能
 */
public class SSHUtil {
    private static final Logger logger = LogManager.getLogger(SSHUtil.class);
    
    // 默认SSH端口
    private static final int DEFAULT_SSH_PORT = 22;
    // 默认连接超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 30000;
    
    /**
     * 创建SSH会话
     * 
     * @param host 主机地址
     * @param port SSH端口
     * @param username 用户名
     * @param password 密码
     * @return SSH会话对象
     * @throws JSchException 创建会话失败时抛出异常
     */
    public static Session createSession(String host, int port, String username, String password) 
            throws JSchException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("主机地址不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            JSch jsch = new JSch();
            
            // 配置JSch属性
            configureJSch(jsch);
            
            // 创建会话
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            
            // 设置会话属性
            Properties config = new Properties();
            // 禁用严格主机密钥检查
            config.put("StrictHostKeyChecking", "no");
            // 禁用主机密钥检查
            config.put("PreferredAuthentications", "password");
            // 设置连接超时
            session.setTimeout(DEFAULT_TIMEOUT);
            session.setConfig(config);
            
            logger.debug("SSH会话已创建: {}@{}:{}", username, host, port);
            return session;
        } catch (JSchException e) {
            logger.error("创建SSH会话失败 {}@{}:{} - {}", username, host, port, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 创建SSH会话（使用默认端口22）
     * 
     * @param host 主机地址
     * @param username 用户名
     * @param password 密码
     * @return SSH会话对象
     * @throws JSchException 创建会话失败时抛出异常
     */
    public static Session createSession(String host, String username, String password) 
            throws JSchException {
        return createSession(host, DEFAULT_SSH_PORT, username, password);
    }

    /**
     * 执行远程命令
     * 
     * @param session SSH会话
     * @param command 要执行的命令
     * @return 命令执行结果
     * @throws JSchException SSH操作失败时抛出异常
     * @throws IOException 读取输出失败时抛出异常
     */
    public static String executeCommand(Session session, String command) 
            throws JSchException, IOException {
        if (session == null || !session.isConnected()) {
            throw new IllegalStateException("SSH会话未连接");
        }
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("命令不能为空");
        }
        
        ChannelExec channel = null;
        try {
            // 创建执行通道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            // 获取输入流和错误流
            InputStream inputStream = channel.getInputStream();
            InputStream errorStream = channel.getErrStream();
            
            // 连接通道
            channel.connect();
            
            // 读取标准输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 读取错误输出
            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }
            
            // 等待命令执行完成
            while (!channel.isClosed()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("命令执行被中断", e);
                }
            }
            
            int exitStatus = channel.getExitStatus();
            
            // 记录日志（脱敏处理）
            String safeCommand = command.contains("password") ? "***" : command;
            logger.debug("执行命令: {} - 退出状态: {}", safeCommand, exitStatus);
            
            if (exitStatus != 0 && error.length() > 0) {
                logger.warn("命令执行有错误输出: {}", error.toString().trim());
            }
            
            // 返回输出结果
            String result = output.toString().trim();
            if (result.isEmpty() && error.length() > 0) {
                result = error.toString().trim();
            }
            
            return result;
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }
    
    /**
     * 检查会话是否已连接
     * 
     * @param session SSH会话
     * @return 如果会话已连接返回true，否则返回false
     */
    public static boolean isSessionConnected(Session session) {
        boolean connected = session != null && session.isConnected();
        if (session != null) {
            logger.debug("会话 {} 连接状态: {}", session.getHost(), connected);
        }
        return connected;
    }
    
    /**
     * 安全关闭会话
     * 
     * @param session SSH会话
     */
    public static void closeSession(Session session) {
        if (session != null) {
            try {
                if (session.isConnected()) {
                    String host = session.getHost();
                    session.disconnect();
                    logger.debug("SSH会话已关闭: {}", host);
                }
            } catch (Exception e) {
                logger.error("关闭SSH会话时发生异常: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 配置JSch属性
     * 
     * @param jsch JSch实例
     */
    private static void configureJSch(JSch jsch) {
        // 可以在这里添加全局JSch配置
        // 例如：设置已知主机文件、添加身份验证等
        try {
            // 设置日志级别
            JSch.setLogger(new JSchLogger());
        } catch (Exception e) {
            logger.warn("配置JSch时发生异常: {}", e.getMessage());
        }
    }
    
    /**
     * JSch日志适配器
     * 将JSch的日志输出到Log4j2
     */
    private static class JSchLogger implements com.jcraft.jsch.Logger {
        @Override
        public boolean isEnabled(int level) {
            return true;
        }
        
        @Override
        public void log(int level, String message) {
            switch (level) {
                case com.jcraft.jsch.Logger.DEBUG:
                    logger.debug("[JSch] {}", message);
                    break;
                case com.jcraft.jsch.Logger.INFO:
                    logger.info("[JSch] {}", message);
                    break;
                case com.jcraft.jsch.Logger.WARN:
                    logger.warn("[JSch] {}", message);
                    break;
                case com.jcraft.jsch.Logger.ERROR:
                case com.jcraft.jsch.Logger.FATAL:
                    logger.error("[JSch] {}", message);
                    break;
                default:
                    logger.trace("[JSch] {}", message);
                    break;
            }
        }
    }
}
