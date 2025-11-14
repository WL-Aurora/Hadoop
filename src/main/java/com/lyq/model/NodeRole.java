package com.lyq.model;

/**
 * 节点角色枚举
 * 定义Hadoop集群中各节点可以承担的角色
 */
public enum NodeRole {
    /**
     * NameNode - HDFS的主节点
     * 负责管理文件系统的命名空间
     */
    NAMENODE("NameNode"),

    /**
     * ResourceManager - YARN的主节点
     * 负责集群资源管理
     */
    RESOURCEMANAGER("ResourceManager"),

    /**
     * SecondaryNameNode - HDFS的辅助节点
     * 负责定期合并命名空间镜像
     */
    SECONDARYNAMENODE("SecondaryNameNode"),

    /**
     * DataNode - HDFS的数据节点
     * 负责存储实际数据（默认必选角色）
     */
    DATANODE("DataNode"),

    /**
     * NodeManager - YARN的节点管理器
     * 负责管理单个节点上的资源和任务（默认必选角色）
     */
    NODEMANAGER("NodeManager");

    private final String displayName;

    NodeRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
