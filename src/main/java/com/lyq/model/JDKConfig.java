package com.lyq.model;

/**
 * JDK配置模型
 * 封装JDK的安装配置信息
 */
public class JDKConfig {
    /**
     * JDK来源类型
     */
    private SourceType sourceType;

    /**
     * 预设版本（sourceType=PRESET时使用）
     */
    private String presetVersion;

    /**
     * 本地文件路径（sourceType=LOCAL_FILE时使用）
     */
    private String localFilePath;

    /**
     * 远程安装目录
     */
    private String remoteInstallDir;

    /**
     * JAVA_HOME环境变量路径
     */
    private String javaHome;

    /**
     * 默认构造函数
     */
    public JDKConfig() {
        this.sourceType = SourceType.PRESET;
        this.remoteInstallDir = "/opt/module/jdk";
    }

    // Getter和Setter方法

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getPresetVersion() {
        return presetVersion;
    }

    public void setPresetVersion(String presetVersion) {
        this.presetVersion = presetVersion;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getRemoteInstallDir() {
        return remoteInstallDir;
    }

    public void setRemoteInstallDir(String remoteInstallDir) {
        this.remoteInstallDir = remoteInstallDir;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * 重写toString方法
     * 
     * @return 配置信息的字符串表示
     */
    @Override
    public String toString() {
        return "JDKConfig{" +
                "sourceType=" + sourceType +
                ", presetVersion='" + presetVersion + '\'' +
                ", localFilePath='" + localFilePath + '\'' +
                ", remoteInstallDir='" + remoteInstallDir + '\'' +
                ", javaHome='" + javaHome + '\'' +
                '}';
    }
}
