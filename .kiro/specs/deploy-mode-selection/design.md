# 部署模式选择界面设计文档

## 概述

部署模式选择界面是用户在完成虚拟机连接配置后的第二个步骤，提供"一键部署"和"自定义部署"两种模式供用户选择。界面设计遵循简洁、直观的原则，确保用户能够快速理解并完成配置。

## 架构

### 整体架构

```
┌─────────────────────────────────────────┐
│         DeployModeController            │
│  (部署模式选择界面控制器)                 │
├─────────────────────────────────────────┤
│  - 处理用户交互                          │
│  - 管理部署模式状态                      │
│  - 验证角色配置                          │
│  - 保存配置到ConfigService               │
└─────────────────────────────────────────┘
              │
              ├──> DeployModeConfig (数据模型)
              ├──> ConfigService (配置服务)
              └──> ValidationService (验证服务)
```

### 界面布局

```
┌────────────────────────────────────────────────────┐
│  Hadoop 分布式集群一键部署系统                       │
│  部署模式选择                                        │
├────────────────────────────────────────────────────┤
│                                                    │
│  ┌──────────────────────────────────────────┐    │
│  │ ○ 一键部署（推荐）                        │    │
│  │   系统自动分配节点角色，快速完成部署       │    │
│  │                                            │    │
│  │   虚拟机1: NameNode + DataNode            │    │
│  │   虚拟机2: ResourceManager + DataNode     │    │
│  │   虚拟机3: SecondaryNameNode + DataNode   │    │
│  └──────────────────────────────────────────┘    │
│                                                    │
│  ┌──────────────────────────────────────────┐    │
│  │ ○ 自定义部署                              │    │
│  │   手动配置每台虚拟机的角色                 │    │
│  │                                            │    │
│  │   [角色配置区域 - 仅在选中时显示]         │    │
│  └──────────────────────────────────────────┘    │
│                                                    │
├────────────────────────────────────────────────────┤
│  [上一步]              [取消]        [下一步]      │
└────────────────────────────────────────────────────┘
```

## 组件和接口

### 1. DeployModeController

**职责：** 控制部署模式选择界面的交互逻辑

**主要方法：**

```java
public class DeployModeController {
    // 初始化界面
    public void initialize();

    // 处理一键部署模式选择
    private void handleQuickDeployMode();

    // 处理自定义部署模式选择
    private void handleCustomDeployMode();

    // 处理角色选择变化
    private void handleRoleSelection(int vmIndex, String role);

    // 验证角色配置
    private boolean validateRoleConfiguration();

    // 保存配置
    private void saveConfiguration();

    // 处理上一步按钮
    private void handlePrevious();

    // 处理下一步按钮
    private void handleNext();

    // 处理取消按钮
    private void handleCancel();
}
```

### 2. DeployModeConfig

**职责：** 存储部署模式配置数据

**数据结构：**

```java
public class DeployModeConfig {
    private DeployMode mode; // QUICK 或 CUSTOM
    private Map<Integer, List<NodeRole>> roleAssignments; // 虚拟机编号 -> 角色列表

    // Getters and Setters
}

public enum DeployMode {
    QUICK,   // 一键部署
    CUSTOM   // 自定义部署
}

public enum NodeRole {
    NAMENODE,
    RESOURCEMANAGER,
    SECONDARYNAMENODE,
    DATANODE  // 默认必选
}
```

### 3. ConfigService

**职责：** 管理配置文件的读写

**主要方法：**

```java
public class ConfigService {
    // 保存部署模式配置
    public void saveDeployModeConfig(DeployModeConfig config) throws IOException;

    // 加载部署模式配置
    public DeployModeConfig loadDeployModeConfig() throws IOException;

    // 检查配置文件是否存在
    public boolean deployModeConfigExists();
}
```

### 4. ValidationService

**职责：** 验证角色配置的合理性

**主要方法：**

```java
public class ValidationService {
    // 验证角色配置
    public void validateRoleConfiguration(Map<Integer, List<NodeRole>> roleAssignments)
        throws ValidationException;

    // 检查必需角色是否已分配
    private boolean hasRequiredRoles(Map<Integer, List<NodeRole>> roleAssignments);
}
```

## 数据模型

### 配置文件格式（JSON）

```json
{
  "deployMode": "QUICK",
  "roleAssignments": {
    "1": ["NAMENODE", "DATANODE"],
    "2": ["RESOURCEMANAGER", "DATANODE"],
    "3": ["SECONDARYNAMENODE", "DATANODE"]
  }
}
```

## 错误处理

### 验证错误

1. **缺少 NameNode 角色**

   - 错误信息：`"必须为至少一台虚拟机分配NameNode角色"`
   - 处理方式：显示错误对话框，阻止进入下一步

2. **缺少 ResourceManager 角色**

   - 错误信息：`"必须为至少一台虚拟机分配ResourceManager角色"`
   - 处理方式：显示错误对话框，阻止进入下一步

3. **缺少 SecondaryNameNode 角色**
   - 错误信息：`"必须为至少一台虚拟机分配SecondaryNameNode角色"`
   - 处理方式：显示错误对话框，阻止进入下一步

### 配置文件错误

1. **配置文件读取失败**

   - 处理方式：记录日志，使用默认配置（一键部署模式）

2. **配置文件保存失败**
   - 处理方式：记录日志，显示警告提示，但不阻止用户继续操作

## 测试策略

### 单元测试

1. **DeployModeController 测试**

   - 测试模式切换逻辑
   - 测试角色选择逻辑
   - 测试配置验证逻辑

2. **ValidationService 测试**

   - 测试必需角色验证
   - 测试各种角色配置组合

3. **ConfigService 测试**
   - 测试配置保存和加载
   - 测试配置文件不存在的情况
   - 测试配置文件损坏的情况

### 集成测试

1. **界面交互测试**

   - 测试模式选择和切换
   - 测试角色配置和验证
   - 测试导航按钮功能

2. **配置持久化测试**
   - 测试配置保存后重新加载
   - 测试配置在界面间传递

## UI 设计规范

### 颜色方案

- 主色调：蓝色（#3498db）
- 成功色：绿色（#27ae60）
- 警告色：橙色（#f39c12）
- 错误色：红色（#e74c3c）
- 背景色：白色（#ffffff）
- 边框色：浅灰（#dee2e6）

### 字体规范

- 标题：System Bold 20px
- 副标题：System Bold 15px
- 正文：System 13px
- 提示文本：System 11px

### 间距规范

- 外边距：30px
- 内边距：15px
- 组件间距：12px
- 按钮间距：15px

### 组件尺寸

- 窗口大小：780x600px（固定）
- 单选按钮：标准大小
- 下拉菜单：宽度 150px
- 按钮：宽度 130px，高度 36px

## 实现注意事项

1. **默认配置**

   - 界面加载时默认选中"一键部署"模式
   - 自定义部署模式下，默认为所有虚拟机分配 DataNode 角色

2. **状态管理**

   - 使用 JavaFX 属性绑定实现界面状态自动更新
   - 模式切换时清除或保留之前的配置

3. **用户体验**

   - 提供清晰的角色说明和提示
   - 验证错误时高亮显示问题区域
   - 使用动画效果平滑切换界面状态

4. **性能优化**

   - 配置验证在用户操作时实时进行
   - 避免频繁的文件 IO 操作

5. **日志记录**
   - 记录用户的模式选择
   - 记录角色配置变化
   - 记录验证错误和配置保存结果
