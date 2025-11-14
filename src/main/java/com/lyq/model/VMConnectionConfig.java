package com.lyq.model;

/**
 * 虚拟机连接配置模型
 * 封装单个虚拟机的SSH连接配置信息
 */
public class VMConnectionConfig {
    /**
     * 虚拟机编号（1-3）
     */
    private int index;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * SSH登录用户名
     */
    private String username;
    
    /**
     * SSH登录密码
     */
    private String password;
    
    /**
     * SSH端口号（默认22）
     */
    private int sshPort;
    
    /**
     * 连接超时时间（秒，默认30秒）
     */
    private int timeout;
    
    /**
     * 默认构造函数
     */
    public VMConnectionConfig() {
        this.sshPort = 22;
        this.timeout = 30;
    }
    
    /**
     * 完整构造函数
     * @param index 虚拟机编号
     * @param ip IP地址
     * @param hostname 主机名
     * @param username 用户名
     * @param password 密码
     */
    public VMConnectionConfig(int index, String ip, String hostname, String username, String password) {
        this.index = index;
        this.ip = ip;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.sshPort = 22;
        this.timeout = 30;
    }
    
    /**
     * 完整构造函数（包含端口和超时）
     * @param index 虚拟机编号
     * @param ip IP地址
     * @param hostname 主机名
     * @param username 用户名
     * @param password 密码
     * @param sshPort SSH端口
     * @param timeout 超时时间（秒）
     */
    public VMConnectionConfig(int index, String ip, String hostname, String username, 
                             String password, int sshPort, int timeout) {
        this.index = index;
        this.ip = ip;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.sshPort = sshPort;
        this.timeout = timeout;
    }
    
    // Getter和Setter方法
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getSshPort() {
        return sshPort;
    }
    
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    /**
     * 重写toString方法，确保密码脱敏显示
     * @return 配置信息的字符串表示（密码已脱敏）
     */
    @Override
    public String toString() {
        return "VMConnectionConfig{" +
                "index=" + index +
                ", ip='" + ip + '\'' +
                ", hostname='" + hostname + '\'' +
                ", username='" + username + '\'' +
                ", password='***'" +
                ", sshPort=" + sshPort +
                ", timeout=" + timeout +
                '}';
    }
}
