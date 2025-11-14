package com.lyq.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部署配置模型
 * 封装完整的Hadoop集群部署配置信息
 */
public class DeploymentConfig {
    /**
     * 虚拟机连接配置列表
     */
    private List<VMConnectionConfig> vmConfigs;

    /**
     * 主机名数组 [Hadoop101, Hadoop102, Hadoop103]
     */
    private String[] hostnames;

    /**
     * JDK配置
     */
    private JDKConfig jdkConfig;

    /**
     * Hadoop配置
     */
    private HadoopConfig hadoopConfig;

    /**
     * 集群配置
     */
    private ClusterConfig clusterConfig;

    /**
     * 部署模式
     */
    private DeployMode deployMode;

    /**
     * 角色分配（自定义模式使用）
     * Key: 虚拟机编号（1-3）
     * Value: 角色列表
     */
    private Map<Integer, List<NodeRole>> roleAssignments;

    /**
     * 默认构造函数
     */
    public DeploymentConfig() {
        this.vmConfigs = new ArrayList<>();
        this.hostnames = new String[3];
        this.jdkConfig = new JDKConfig();
        this.hadoopConfig = new HadoopConfig();
        this.clusterConfig = new ClusterConfig();
        this.roleAssignments = new HashMap<>();
    }

    // Getter和Setter方法

    public List<VMConnectionConfig> getVmConfigs() {
        return vmConfigs;
    }

    public void setVmConfigs(List<VMConnectionConfig> vmConfigs) {
        this.vmConfigs = vmConfigs;
    }

    public String[] getHostnames() {
        return hostnames;
    }

    public void setHostnames(String[] hostnames) {
        this.hostnames = hostnames;
    }

    public JDKConfig getJdkConfig() {
        return jdkConfig;
    }

    public void setJdkConfig(JDKConfig jdkConfig) {
        this.jdkConfig = jdkConfig;
    }

    public HadoopConfig getHadoopConfig() {
        return hadoopConfig;
    }

    public void setHadoopConfig(HadoopConfig hadoopConfig) {
        this.hadoopConfig = hadoopConfig;
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public DeployMode getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(DeployMode deployMode) {
        this.deployMode = deployMode;
    }

    public Map<Integer, List<NodeRole>> getRoleAssignments() {
        return roleAssignments;
    }

    public void setRoleAssignments(Map<Integer, List<NodeRole>> roleAssignments) {
        this.roleAssignments = roleAssignments;
    }

    /**
     * 重写toString方法
     * 
     * @return 配置信息的字符串表示
     */
    @Override
    public String toString() {
        return "DeploymentConfig{" +
                "vmConfigs=" + vmConfigs +
                ", hostnames=" + java.util.Arrays.toString(hostnames) +
                ", jdkConfig=" + jdkConfig +
                ", hadoopConfig=" + hadoopConfig +
                ", clusterConfig=" + clusterConfig +
                ", deployMode=" + deployMode +
                ", roleAssignments=" + roleAssignments +
                '}';
    }
}
