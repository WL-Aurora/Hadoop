package com.lyq.model;

/**
 * 连接状态枚举
 * 定义虚拟机SSH连接的各种状态
 */
public enum ConnectionStatus {
    /**
     * 未测试 - 初始状态，尚未进行连接测试
     */
    NOT_TESTED("未测试"),
    
    /**
     * 测试中 - 正在进行连接测试
     */
    TESTING("测试中"),
    
    /**
     * 连接成功 - SSH连接建立成功
     */
    SUCCESS("连接成功"),
    
    /**
     * 网络不可达 - 目标IP地址无法访问
     */
    NETWORK_UNREACHABLE("网络不可达"),
    
    /**
     * SSH服务未启动 - SSH端口未开放或服务未运行
     */
    SSH_SERVICE_DOWN("SSH服务未启动"),
    
    /**
     * 认证失败 - 用户名或密码错误
     */
    AUTH_FAILED("认证失败"),
    
    /**
     * 连接超时 - 连接请求超过设定的超时时间
     */
    TIMEOUT("连接超时"),
    
    /**
     * 未知错误 - 其他未分类的错误
     */
    UNKNOWN_ERROR("未知错误");
    
    /**
     * 状态的中文描述
     */
    private final String description;
    
    /**
     * 构造函数
     * @param description 状态的中文描述
     */
    ConnectionStatus(String description) {
        this.description = description;
    }
    
    /**
     * 获取状态的中文描述
     * @return 中文描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为成功状态
     * @return 如果是成功状态返回true，否则返回false
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * 判断是否为失败状态
     * @return 如果是失败状态返回true，否则返回false
     */
    public boolean isFailure() {
        return this != SUCCESS && this != NOT_TESTED && this != TESTING;
    }
}
