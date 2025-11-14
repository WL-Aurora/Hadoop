package com.lyq.model;

/**
 * 软件来源类型枚举
 * 用于指定JDK和Hadoop的安装来源
 */
public enum SourceType {
    /**
     * 预设版本 - 从预定义的版本列表中选择
     */
    PRESET("预设版本"),

    /**
     * 本地上传 - 从本地文件系统上传安装包
     */
    LOCAL_FILE("本地上传");

    private final String description;

    /**
     * 构造函数
     * 
     * @param description 类型描述
     */
    SourceType(String description) {
        this.description = description;
    }

    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getDescription() {
        return description;
    }
}
