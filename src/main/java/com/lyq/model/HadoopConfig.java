package com.lyq.model;

/**
 * Hadoop配置模型
 * 封装Hadoop的安装配置信息
 */
public class HadoopConfig {
    /**
     * Hadoop来源类型
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
     * HDFS块大小配置
     */
    private String hdfsBlockSize;

    /**
     * YARN总内存配置
     */
    private String yarnMemory;

    /**
     * 默认构造函数
     */
    public HadoopConfig() {
        this.sourceType = SourceType.PRESET;
        this.remoteInstallDir = "/opt/module/hadoop";
        this.hdfsBlockSize = "128MB";
        this.yarnMemory = "2GB";
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

    public String getHdfsBlockSize() {
        return hdfsBlockSize;
    }

    public void setHdfsBlockSize(String hdfsBlockSize) {
        this.hdfsBlockSize = hdfsBlockSize;
    }

    public String getYarnMemory() {
        return yarnMemory;
    }

    public void setYarnMemory(String yarnMemory) {
        this.yarnMemory = yarnMemory;
    }

    /**
     * 重写toString方法
     * 
     * @return 配置信息的字符串表示
     */
    @Override
    public String toString() {
        return "HadoopConfig{" +
                "sourceType=" + sourceType +
                ", presetVersion='" + presetVersion + '\'' +
                ", localFilePath='" + localFilePath + '\'' +
                ", remoteInstallDir='" + remoteInstallDir + '\'' +
                ", hdfsBlockSize='" + hdfsBlockSize + '\'' +
                ", yarnMemory='" + yarnMemory + '\'' +
                '}';
    }
}
