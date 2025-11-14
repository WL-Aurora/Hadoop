package com.lyq.exception;

import com.lyq.model.ConnectionStatus;

/**
 * 连接异常类
 * 用于封装SSH连接过程中发生的各种异常情况
 * 
 * 需求: 需求 4（连接失败诊断）
 */
public class ConnectionException extends Exception {
    
    /**
     * 连接状态
     */
    private final ConnectionStatus status;
    
    /**
     * 虚拟机IP地址
     */
    private final String vmIp;
    
    /**
     * 构造函数 - 仅包含消息
     * 
     * @param message 异常消息
     * @param status 连接状态
     * @param vmIp 虚拟机IP地址
     */
    public ConnectionException(String message, ConnectionStatus status, String vmIp) {
        super(message);
        this.status = status;
        this.vmIp = vmIp;
    }
    
    /**
     * 构造函数 - 包含消息和原因
     * 
     * @param message 异常消息
     * @param cause 异常原因
     * @param status 连接状态
     * @param vmIp 虚拟机IP地址
     */
    public ConnectionException(String message, Throwable cause, ConnectionStatus status, String vmIp) {
        super(message, cause);
        this.status = status;
        this.vmIp = vmIp;
    }
    
    /**
     * 获取连接状态
     * 
     * @return 连接状态
     */
    public ConnectionStatus getStatus() {
        return status;
    }
    
    /**
     * 获取虚拟机IP地址
     * 
     * @return 虚拟机IP地址
     */
    public String getVmIp() {
        return vmIp;
    }
    
    /**
     * 获取详细的错误信息
     * 包含状态描述和IP地址
     * 
     * @return 详细错误信息
     */
    public String getDetailedMessage() {
        return String.format("虚拟机 %s 连接失败: %s - %s", 
            vmIp, 
            status.getDescription(), 
            getMessage());
    }
    
    @Override
    public String toString() {
        return String.format("ConnectionException{status=%s, vmIp='%s', message='%s'}", 
            status, vmIp, getMessage());
    }
}
