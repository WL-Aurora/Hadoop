package com.lyq.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群配置模型
 * 封装Hadoop集群的配置信息
 */
public class ClusterConfig {
    /**
     * NameNode主机名
     */
    private String nameNodeHost;

    /**
     * NameNode端口
     */
    private int nameNodePort;

    /**
     * NameNode HTTP端口
     */
    private int nameNodeHttpPort;

    /**
     * SecondaryNameNode主机名
     */
    private String secondaryNameNodeHost;

    /**
     * SecondaryNameNode HTTP端口
     */
    private int secondaryNameNodeHttpPort;

    /**
     * ResourceManager主机名
     */
    private String resourceManagerHost;

    /**
     * ResourceManager端口
     */
    private int resourceManagerPort;

    /**
     * ResourceManager Web端口
     */
    private int resourceManagerWebPort;

    /**
     * DataNode主机名列表
     */
    private List<String> dataNodeHosts;

    /**
     * NodeManager主机名列表
     */
    private List<String> nodeManagerHosts;

    /**
     * HDFS数据目录
     */
    private String hdfsDataDir;

    /**
     * HDFS名称目录
     */
    private String hdfsNameDir;

    /**
     * HDFS临时目录
     */
    private String hdfsTempDir;

    /**
     * 默认构造函数
     */
    public ClusterConfig() {
        this.nameNodePort = 9000;
        this.nameNodeHttpPort = 9870;
        this.secondaryNameNodeHttpPort = 9868;
        this.resourceManagerPort = 8032;
        this.resourceManagerWebPort = 8088;
        this.dataNodeHosts = new ArrayList<>();
        this.nodeManagerHosts = new ArrayList<>();
        this.hdfsDataDir = "/opt/module/hadoop/data";
        this.hdfsNameDir = "/opt/module/hadoop/name";
        this.hdfsTempDir = "/opt/module/hadoop/tmp";
    }

    // Getter和Setter方法

    public String getNameNodeHost() {
        return nameNodeHost;
    }

    public void setNameNodeHost(String nameNodeHost) {
        this.nameNodeHost = nameNodeHost;
    }

    public int getNameNodePort() {
        return nameNodePort;
    }

    public void setNameNodePort(int nameNodePort) {
        this.nameNodePort = nameNodePort;
    }

    public int getNameNodeHttpPort() {
        return nameNodeHttpPort;
    }

    public void setNameNodeHttpPort(int nameNodeHttpPort) {
        this.nameNodeHttpPort = nameNodeHttpPort;
    }

    public String getSecondaryNameNodeHost() {
        return secondaryNameNodeHost;
    }

    public void setSecondaryNameNodeHost(String secondaryNameNodeHost) {
        this.secondaryNameNodeHost = secondaryNameNodeHost;
    }

    public int getSecondaryNameNodeHttpPort() {
        return secondaryNameNodeHttpPort;
    }

    public void setSecondaryNameNodeHttpPort(int secondaryNameNodeHttpPort) {
        this.secondaryNameNodeHttpPort = secondaryNameNodeHttpPort;
    }

    public String getResourceManagerHost() {
        return resourceManagerHost;
    }

    public void setResourceManagerHost(String resourceManagerHost) {
        this.resourceManagerHost = resourceManagerHost;
    }

    public int getResourceManagerPort() {
        return resourceManagerPort;
    }

    public void setResourceManagerPort(int resourceManagerPort) {
        this.resourceManagerPort = resourceManagerPort;
    }

    public int getResourceManagerWebPort() {
        return resourceManagerWebPort;
    }

    public void setResourceManagerWebPort(int resourceManagerWebPort) {
        this.resourceManagerWebPort = resourceManagerWebPort;
    }

    public List<String> getDataNodeHosts() {
        return dataNodeHosts;
    }

    public void setDataNodeHosts(List<String> dataNodeHosts) {
        this.dataNodeHosts = dataNodeHosts;
    }

    public List<String> getNodeManagerHosts() {
        return nodeManagerHosts;
    }

    public void setNodeManagerHosts(List<String> nodeManagerHosts) {
        this.nodeManagerHosts = nodeManagerHosts;
    }

    public String getHdfsDataDir() {
        return hdfsDataDir;
    }

    public void setHdfsDataDir(String hdfsDataDir) {
        this.hdfsDataDir = hdfsDataDir;
    }

    public String getHdfsNameDir() {
        return hdfsNameDir;
    }

    public void setHdfsNameDir(String hdfsNameDir) {
        this.hdfsNameDir = hdfsNameDir;
    }

    public String getHdfsTempDir() {
        return hdfsTempDir;
    }

    public void setHdfsTempDir(String hdfsTempDir) {
        this.hdfsTempDir = hdfsTempDir;
    }

    /**
     * 重写toString方法
     * 
     * @return 配置信息的字符串表示
     */
    @Override
    public String toString() {
        return "ClusterConfig{" +
                "nameNodeHost='" + nameNodeHost + '\'' +
                ", nameNodePort=" + nameNodePort +
                ", nameNodeHttpPort=" + nameNodeHttpPort +
                ", secondaryNameNodeHost='" + secondaryNameNodeHost + '\'' +
                ", secondaryNameNodeHttpPort=" + secondaryNameNodeHttpPort +
                ", resourceManagerHost='" + resourceManagerHost + '\'' +
                ", resourceManagerPort=" + resourceManagerPort +
                ", resourceManagerWebPort=" + resourceManagerWebPort +
                ", dataNodeHosts=" + dataNodeHosts +
                ", nodeManagerHosts=" + nodeManagerHosts +
                ", hdfsDataDir='" + hdfsDataDir + '\'' +
                ", hdfsNameDir='" + hdfsNameDir + '\'' +
                ", hdfsTempDir='" + hdfsTempDir + '\'' +
                '}';
    }
}
