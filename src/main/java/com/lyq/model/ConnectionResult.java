package com.lyq.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 连接测试结果模型
 * 封装SSH连接测试的结果信息
 */
public class ConnectionResult {
    /**
     * 虚拟机编号
     */
    private int vmIndex;
    
    /**
     * 虚拟机IP地址
     */
    private String vmIp;
    
    /**
     * 连接状态
     */
    private ConnectionStatus status;
    
    /**
     * 结果消息（简短描述）
     */
    private String message;
    
    /**
     * 错误详情（详细的错误信息）
     */
    private String errorDetail;
    
    /**
     * 响应时间（毫秒）
     */
    private long responseTime;
    
    /**
     * 测试时间
     */
    private LocalDateTime testTime;
    
    /**
     * 私有构造函数，使用工厂方法创建实例
     */
    private ConnectionResult() {
        this.testTime = LocalDateTime.now();
    }
    
    /**
     * 创建成功的连接结果
     * @param vmIndex 虚拟机编号
     * @param vmIp 虚拟机IP地址
     * @param responseTime 响应时间（毫秒）
     * @return 成功的连接结果对象
     */
    public static ConnectionResult success(int vmIndex, String vmIp, long responseTime) {
        ConnectionResult result = new ConnectionResult();
        result.vmIndex = vmIndex;
        result.vmIp = vmIp;
        result.status = ConnectionStatus.SUCCESS;
        result.message = "连接成功";
        result.responseTime = responseTime;
        return result;
    }
    
    /**
     * 创建成功的连接结果（带自定义状态和消息）
     * @param vmIndex 虚拟机编号
     * @param vmIp 虚拟机IP地址
     * @param status 连接状态
     * @param message 结果消息
     * @param responseTime 响应时间（毫秒）
     * @return 成功的连接结果对象
     */
    public static ConnectionResult success(int vmIndex, String vmIp, 
                                          ConnectionStatus status, String message, 
                                          long responseTime) {
        ConnectionResult result = new ConnectionResult();
        result.vmIndex = vmIndex;
        result.vmIp = vmIp;
        result.status = status;
        result.message = message;
        result.responseTime = responseTime;
        return result;
    }
    
    /**
     * 创建失败的连接结果
     * @param vmIndex 虚拟机编号
     * @param vmIp 虚拟机IP地址
     * @param status 失败状态
     * @param errorDetail 错误详情
     * @return 失败的连接结果对象
     */
    public static ConnectionResult failure(int vmIndex, String vmIp, 
                                          ConnectionStatus status, String errorDetail) {
        ConnectionResult result = new ConnectionResult();
        result.vmIndex = vmIndex;
        result.vmIp = vmIp;
        result.status = status;
        result.message = status.getDescription();
        result.errorDetail = errorDetail;
        result.responseTime = -1;
        return result;
    }
    
    /**
     * 创建失败的连接结果（带自定义消息）
     * @param vmIndex 虚拟机编号
     * @param vmIp 虚拟机IP地址
     * @param status 失败状态
     * @param message 结果消息
     * @param errorDetail 错误详情
     * @return 失败的连接结果对象
     */
    public static ConnectionResult failure(int vmIndex, String vmIp, 
                                          ConnectionStatus status, String message, 
                                          String errorDetail) {
        ConnectionResult result = new ConnectionResult();
        result.vmIndex = vmIndex;
        result.vmIp = vmIp;
        result.status = status;
        result.message = message;
        result.errorDetail = errorDetail;
        result.responseTime = -1;
        return result;
    }
    
    /**
     * 创建失败的连接结果（简化版本，使用默认错误消息）
     * @param vmIndex 虚拟机编号
     * @param vmIp 虚拟机IP地址
     * @param errorDetail 错误详情
     * @return 失败的连接结果对象
     */
    public static ConnectionResult failure(int vmIndex, String vmIp, String errorDetail) {
        return failure(vmIndex, vmIp, ConnectionStatus.UNKNOWN_ERROR, errorDetail);
    }
    
    // Getter和Setter方法
    
    public int getVmIndex() {
        return vmIndex;
    }
    
    public void setVmIndex(int vmIndex) {
        this.vmIndex = vmIndex;
    }
    
    public String getVmIp() {
        return vmIp;
    }
    
    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }
    
    public ConnectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorDetail() {
        return errorDetail;
    }
    
    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
    
    public LocalDateTime getTestTime() {
        return testTime;
    }
    
    public void setTestTime(LocalDateTime testTime) {
        this.testTime = testTime;
    }
    
    /**
     * 判断连接是否成功
     * @return 如果连接成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status != null && status.isSuccess();
    }
    
    /**
     * 获取格式化的测试时间
     * @return 格式化后的时间字符串
     */
    public String getFormattedTestTime() {
        if (testTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return testTime.format(formatter);
    }
    
    /**
     * 获取用户友好的结果描述
     * @return 结果描述字符串
     */
    public String getUserFriendlyMessage() {
        if (isSuccess()) {
            return String.format("虚拟机%d（%s）连接成功，响应时间：%dms", 
                               vmIndex, vmIp, responseTime);
        } else {
            String detail = errorDetail != null && !errorDetail.isEmpty() 
                          ? "，详情：" + errorDetail 
                          : "";
            return String.format("虚拟机%d（%s）%s%s", 
                               vmIndex, vmIp, message, detail);
        }
    }
    
    @Override
    public String toString() {
        return "ConnectionResult{" +
                "vmIndex=" + vmIndex +
                ", vmIp='" + vmIp + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", errorDetail='" + errorDetail + '\'' +
                ", responseTime=" + responseTime +
                ", testTime=" + testTime +
                '}';
    }
}
