package com.lyq.model;

/**
 * 部署模式枚举
 * 定义Hadoop集群的两种部署方式
 */
public enum DeployMode {
    /**
     * 一键部署模式
     * 系统自动分配节点角色，快速完成部署
     */
    QUICK("一键部署"),

    /**
     * 自定义部署模式
     * 用户手动配置每台虚拟机的角色
     */
    CUSTOM("自定义部署");

    private final String displayName;

    DeployMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
