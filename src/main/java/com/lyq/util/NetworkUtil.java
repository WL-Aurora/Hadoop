package com.lyq.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * 提供网络检测和IP地址验证功能
 */
public class NetworkUtil {
    private static final Logger logger = LogManager.getLogger(NetworkUtil.class);
    
    // IPv4地址格式正则表达式
    private static final String IP_PATTERN = 
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    
    private static final Pattern pattern = Pattern.compile(IP_PATTERN);
    
    /**
     * 检查IP地址是否可达
     * 
     * @param ip IP地址
     * @param timeout 超时时间（毫秒）
     * @return 如果IP可达返回true，否则返回false
     */
    public static boolean isReachable(String ip, int timeout) {
        if (ip == null || ip.trim().isEmpty()) {
            logger.warn("IP地址为空");
            return false;
        }
        
        try {
            InetAddress address = InetAddress.getByName(ip);
            boolean reachable = address.isReachable(timeout);
            logger.debug("IP {} 可达性检测结果: {}", ip, reachable);
            return reachable;
        } catch (IOException e) {
            logger.error("检查IP {} 可达性时发生异常: {}", ip, e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查指定端口是否开放
     * 
     * @param ip IP地址
     * @param port 端口号
     * @param timeout 超时时间（毫秒）
     * @return 如果端口开放返回true，否则返回false
     */
    public static boolean isPortOpen(String ip, int port, int timeout) {
        if (ip == null || ip.trim().isEmpty()) {
            logger.warn("IP地址为空");
            return false;
        }
        
        if (port < 1 || port > 65535) {
            logger.warn("端口号 {} 无效", port);
            return false;
        }
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            logger.debug("IP {} 端口 {} 开放", ip, port);
            return true;
        } catch (IOException e) {
            logger.debug("IP {} 端口 {} 未开放: {}", ip, port, e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证IP地址格式是否正确
     * 
     * @param ip IP地址字符串
     * @return 如果格式正确返回true，否则返回false
     */
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            logger.debug("IP地址为空");
            return false;
        }
        
        boolean valid = pattern.matcher(ip.trim()).matches();
        logger.debug("IP {} 格式验证结果: {}", ip, valid);
        return valid;
    }
    
    /**
     * 获取本机IP地址
     * 
     * @return 本机IP地址，获取失败返回null
     */
    public static String getLocalIpAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            String ip = address.getHostAddress();
            logger.debug("本机IP地址: {}", ip);
            return ip;
        } catch (Exception e) {
            logger.error("获取本机IP地址失败: {}", e.getMessage());
            return null;
        }
    }
}
