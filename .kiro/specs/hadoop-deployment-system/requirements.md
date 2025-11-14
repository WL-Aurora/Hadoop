# Hadoop 一键部署系统需求文档

## 简介

Hadoop 一键部署系统是一个基于 JavaFX 的图形化工具，通过远程连接 3 台虚拟机，自动完成 Hadoop 分布式集群的部署。系统需要用户提供本地的 JDK 和 Hadoop 安装包，然后自动上传、配置和部署。

## 术语表

- **系统（System）**：Hadoop 一键部署系统
- **用户（User）**：使用本系统部署 Hadoop 集群的操作人员
- **虚拟机（VM）**：运行 CentOS 7 的虚拟机节点
- **主部署节点（Primary Node）**：执行主要部署操作的节点（通常是虚拟机 1 或 NameNode 所在节点）
- **安装包（Package）**：JDK 和 Hadoop 的 tar.gz 压缩包文件
- **环境变量（Environment Variable）**：JAVA_HOME、HADOOP_HOME 等系统环境变量

## 需求

### 需求 1：虚拟机连接配置

**用户故事：** 作为用户，我想配置 3 台虚拟机的连接信息，以便系统能够远程连接并部署 Hadoop 集群

#### 验收标准

1. WHEN 用户启动系统，THE 系统 SHALL 显示虚拟机连接配置界面
2. WHEN 用户输入虚拟机 IP 地址，THE 系统 SHALL 验证 IP 地址格式的合法性
3. WHEN 用户输入用户名和密码，THE 系统 SHALL 支持每台虚拟机独立配置
4. WHEN 用户点击"测试"按钮，THE 系统 SHALL 通过 SSH 连接测试虚拟机的可达性
5. WHEN 所有虚拟机测试成功，THE 系统 SHALL 启用"下一步"按钮并保存连接配置

### 需求 2：安装包选择

**用户故事：** 作为用户，我想选择本地的 JDK 和 Hadoop 安装包，以便系统上传到虚拟机进行安装

#### 验收标准

1. WHEN 用户进入安装包选择界面，THE 系统 SHALL 提供"选择 JDK 安装包"按钮
2. WHEN 用户进入安装包选择界面，THE 系统 SHALL 提供"选择 Hadoop 安装包"按钮
3. WHEN 用户选择 JDK 安装包，THE 系统 SHALL 验证文件格式为 tar.gz
4. WHEN 用户选择 Hadoop 安装包，THE 系统 SHALL 验证文件格式为 tar.gz
5. WHEN 用户选择安装包，THE 系统 SHALL 显示文件名和文件大小
6. WHEN 用户未选择必需的安装包，THE 系统 SHALL 禁用"下一步"按钮

### 需求 3：部署模式选择

**用户故事：** 作为用户，我想选择部署模式，以便根据需求快速部署或灵活配置集群

#### 验收标准

1. WHEN 用户进入部署模式选择界面，THE 系统 SHALL 显示"一键部署"和"自定义部署"两个选项
2. WHEN 用户选择"一键部署"，THE 系统 SHALL 自动分配节点角色
3. WHEN 用户选择"自定义部署"，THE 系统 SHALL 允许用户为每台虚拟机配置角色
4. WHEN 用户配置角色，THE 系统 SHALL 验证必需角色已被分配
5. WHEN 用户点击"下一步"，THE 系统 SHALL 保存部署模式配置

### 需求 4：集群参数配置

**用户故事：** 作为用户，我想配置集群参数，以便自定义 Hadoop 集群的运行环境

#### 验收标准

1. WHEN 用户进入集群参数配置界面，THE 系统 SHALL 显示之前配置的虚拟机 IP 地址（只读）
2. WHEN 用户进入集群参数配置界面，THE 系统 SHALL 提供主机名配置（可编辑）
3. WHEN 用户进入集群参数配置界面，THE 系统 SHALL 提供普通用户名和密码配置
4. WHEN 用户进入集群参数配置界面，THE 系统 SHALL 提供 HDFS 块大小和 YARN 内存配置
5. WHEN 用户点击"开始部署"，THE 系统 SHALL 验证所有配置项并开始部署流程

### 需求 5：自动化部署流程

**用户故事：** 作为用户，我想系统自动完成部署，以便无需手动执行复杂的命令

#### 验收标准

1. WHEN 部署开始，THE 系统 SHALL 卸载虚拟机自带的 JDK
2. WHEN 卸载完成，THE 系统 SHALL 上传用户选择的 JDK 安装包到主部署节点
3. WHEN JDK 上传完成，THE 系统 SHALL 解压 JDK 并配置 JAVA_HOME 环境变量
4. WHEN JDK 配置完成，THE 系统 SHALL 上传 Hadoop 安装包到主部署节点
5. WHEN Hadoop 上传完成，THE 系统 SHALL 解压 Hadoop 并配置 HADOOP_HOME 环境变量
6. WHEN 环境变量配置完成，THE 系统 SHALL 生成 Hadoop 配置文件
7. WHEN 配置文件生成完成，THE 系统 SHALL 分发 JDK、Hadoop 和配置文件到其他节点
8. WHEN 分发完成，THE 系统 SHALL 格式化 HDFS NameNode
9. WHEN 格式化完成，THE 系统 SHALL 启动 Hadoop 集群服务
10. WHEN 所有步骤完成，THE 系统 SHALL 显示部署成功并跳转到集群管理界面

### 需求 6：部署进度显示

**用户故事：** 作为用户，我想实时查看部署进度，以便了解当前部署状态

#### 验收标准

1. WHEN 部署开始，THE 系统 SHALL 显示当前执行的步骤名称
2. WHEN 部署进行中，THE 系统 SHALL 实时更新进度条百分比
3. WHEN 部署进行中，THE 系统 SHALL 在日志区域显示详细的执行日志
4. WHEN 某个步骤失败，THE 系统 SHALL 显示错误信息并提供重试选项
5. WHEN 部署完成，THE 系统 SHALL 显示"完成"按钮并允许进入集群管理界面

### 需求 7：集群管理

**用户故事：** 作为用户，我想管理已部署的集群，以便启动、停止和监控集群状态

#### 验收标准

1. WHEN 用户进入集群管理界面，THE 系统 SHALL 显示 3 个节点的状态信息
2. WHEN 用户进入集群管理界面，THE 系统 SHALL 提供启动、停止、重启集群的操作按钮
3. WHEN 用户点击"打开"按钮，THE 系统 SHALL 在默认浏览器中打开对应的 WebUI
4. WHEN 用户查看节点状态，THE 系统 SHALL 显示每个节点的角色和运行状态
5. WHEN 用户点击"退出"，THE 系统 SHALL 显示确认对话框并关闭应用程序

### 需求 8：配置数据传递

**用户故事：** 作为用户，我希望在不同界面间配置数据能够正确传递，以便后续界面能够使用之前的配置

#### 验收标准

1. WHEN 用户从虚拟机连接配置界面进入下一步，THE 系统 SHALL 将 IP 地址传递到后续界面
2. WHEN 用户从部署模式选择界面进入下一步，THE 系统 SHALL 将部署模式和角色配置传递到后续界面
3. WHEN 用户在集群参数配置界面，THE 系统 SHALL 显示之前配置的虚拟机 IP 地址
4. WHEN 用户开始部署，THE 系统 SHALL 使用所有之前配置的参数执行部署
5. WHEN 配置数据传递失败，THE 系统 SHALL 记录错误日志并提示用户

### 需求 9：文件上传进度

**用户故事：** 作为用户，我想查看文件上传进度，以便了解大文件上传的状态

#### 验收标准

1. WHEN 系统上传 JDK 安装包，THE 系统 SHALL 显示上传进度百分比
2. WHEN 系统上传 Hadoop 安装包，THE 系统 SHALL 显示上传进度百分比
3. WHEN 文件上传中断，THE 系统 SHALL 支持断点续传
4. WHEN 文件上传失败，THE 系统 SHALL 显示错误信息并提供重试选项
5. WHEN 文件上传完成，THE 系统 SHALL 验证文件完整性

### 需求 10：错误处理和恢复

**用户故事：** 作为用户，我希望系统能够处理部署过程中的错误，以便在出现问题时能够恢复

#### 验收标准

1. WHEN 网络连接中断，THE 系统 SHALL 显示错误提示并提供重新连接选项
2. WHEN 某个步骤执行失败，THE 系统 SHALL 记录详细错误日志
3. WHEN 部署失败，THE 系统 SHALL 提供"重试"和"取消"两个选项
4. WHEN 用户选择重试，THE 系统 SHALL 从失败的步骤继续执行
5. WHEN 用户取消部署，THE 系统 SHALL 清理已上传的临时文件
