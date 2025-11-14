package com.lyq.model;

import java.util.*;

/**
 * 部署模式配置数据模型
 * 存储用户选择的部署模式和节点角色分配信息
 */
public class DeployModeConfig {

    /**
     * 部署模式（一键部署或自定义部署）
     */
    private DeployMode mode;

    /**
     * 节点角色分配
     * Key: 虚拟机编号（1-3）
     * Value: 该虚拟机分配的角色列表
     */
    private Map<Integer, List<NodeRole>> roleAssignments;

    /**
     * 默认构造函数
     * 初始化为一键部署模式
     */
    public DeployModeConfig() {
        this.mode = DeployMode.QUICK;
        this.roleAssignments = new HashMap<>();
        initializeQuickDeployMode();
    }

    /**
     * 初始化一键部署模式的默认角色分配
     */
    private void initializeQuickDeployMode() {
        // 虚拟机1: NameNode + DataNode + NodeManager
        roleAssignments.put(1, Arrays.asList(NodeRole.NAMENODE, NodeRole.DATANODE, NodeRole.NODEMANAGER));

        // 虚拟机2: ResourceManager + DataNode + NodeManager
        roleAssignments.put(2, Arrays.asList(NodeRole.RESOURCEMANAGER, NodeRole.DATANODE, NodeRole.NODEMANAGER));

        // 虚拟机3: SecondaryNameNode + DataNode + NodeManager
        roleAssignments.put(3, Arrays.asList(NodeRole.SECONDARYNAMENODE, NodeRole.DATANODE, NodeRole.NODEMANAGER));
    }

    /**
     * 初始化自定义部署模式的默认角色分配
     * 所有虚拟机默认有DataNode和NodeManager角色
     */
    public void initializeCustomDeployMode() {
        roleAssignments.clear();
        for (int i = 1; i <= 3; i++) {
            roleAssignments.put(i, new ArrayList<>(Arrays.asList(NodeRole.DATANODE, NodeRole.NODEMANAGER)));
        }
    }

    /**
     * 为指定虚拟机添加角色
     * 
     * @param vmIndex 虚拟机编号（1-3）
     * @param role    要添加的角色
     */
    public void addRole(int vmIndex, NodeRole role) {
        roleAssignments.computeIfAbsent(vmIndex, k -> new ArrayList<>());
        if (!roleAssignments.get(vmIndex).contains(role)) {
            roleAssignments.get(vmIndex).add(role);
        }
    }

    /**
     * 从指定虚拟机移除角色
     * 注意：DataNode角色不能被移除
     * 
     * @param vmIndex 虚拟机编号（1-3）
     * @param role    要移除的角色
     */
    public void removeRole(int vmIndex, NodeRole role) {
        if (role == NodeRole.DATANODE) {
            // DataNode是必选角色，不能移除
            return;
        }

        List<NodeRole> roles = roleAssignments.get(vmIndex);
        if (roles != null) {
            roles.remove(role);
        }
    }

    /**
     * 获取指定虚拟机的角色列表
     * 
     * @param vmIndex 虚拟机编号（1-3）
     * @return 角色列表
     */
    public List<NodeRole> getRoles(int vmIndex) {
        return roleAssignments.getOrDefault(vmIndex, new ArrayList<>());
    }

    /**
     * 检查指定虚拟机是否包含某个角色
     * 
     * @param vmIndex 虚拟机编号（1-3）
     * @param role    要检查的角色
     * @return true表示包含该角色
     */
    public boolean hasRole(int vmIndex, NodeRole role) {
        List<NodeRole> roles = roleAssignments.get(vmIndex);
        return roles != null && roles.contains(role);
    }

    // Getters and Setters

    public DeployMode getMode() {
        return mode;
    }

    public void setMode(DeployMode mode) {
        this.mode = mode;
        if (mode == DeployMode.QUICK) {
            initializeQuickDeployMode();
        } else {
            initializeCustomDeployMode();
        }
    }

    public Map<Integer, List<NodeRole>> getRoleAssignments() {
        return roleAssignments;
    }

    public void setRoleAssignments(Map<Integer, List<NodeRole>> roleAssignments) {
        this.roleAssignments = roleAssignments;
    }

    @Override
    public String toString() {
        return "DeployModeConfig{" +
                "mode=" + mode +
                ", roleAssignments=" + roleAssignments +
                '}';
    }
}
