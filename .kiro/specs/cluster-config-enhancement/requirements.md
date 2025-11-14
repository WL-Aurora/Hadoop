# 需求文档 - 集群配置增强

## 简介

本功能旨在改进 Hadoop 集群参数配置界面，实现虚拟机连接信息的自动传递、本地文件上传支持，以及真实的 SSH 远程部署操作，提升用户体验和系统实用性。

## 术语表

- **HADS**：Hadoop Auto Deployment System，Hadoop 自动部署系统
- **ClusterConfigController**：集群参数配置界面控制器
- **ConnectionController**：虚拟机连接配置界面控制器
- **DeploymentService**：部署服务，负责执行实际的 SSH 远程部署操作
- **SSHService**：SSH 服务，负责与虚拟机建立连接并执行命令
- **FileTransferService**：文件传输服务，负责上传本地文件到虚拟机

## 需求

### 需求 1：虚拟机连接信息自动传递

**用户故事：** 作为系统用户，我希望在集群参数配置界面自动显示之前输入的虚拟机 IP 地址和连接信息，这样我就不需要重复输入相同的信息。

#### 验收标准

1. WHEN 用户从部署模式选择界面进入集群参数配置界面，THE ClusterConfigController SHALL 自动填充三台虚拟机的 IP 地址
2. WHEN 用户查看集群参数配置界面的 IP 地址字段，THE ClusterConfigController SHALL 将这些字段设置为只读状态
3. WHEN ClusterConfigController 接收连接信息，THE ClusterConfigController SHALL 存储每台虚拟机对应的用户名和密码
4. WHEN DeployModeController 跳转到集群配置界面，THE DeployModeController SHALL 调用 ClusterConfigController 的 setVmConnectionInfo 方法传递所有连接信息
5. THE ClusterConfigController SHALL 记录接收到的连接信息到日志中

### 需求 2：移除普通用户配置区域

**用户故事：** 作为系统用户，我不希望在集群参数配置界面重复输入用户信息，因为我已经在第一个页面为每台虚拟机配置了对应的用户名和密码。

#### 验收标准

1. THE ClusterConfigController SHALL 移除"系统参数"配置区域
2. THE ClusterConfigController SHALL 移除普通用户名输入框
3. THE ClusterConfigController SHALL 移除用户密码输入框
4. WHEN 执行部署操作时，THE DeploymentService SHALL 使用从 ConnectionController 传递的每台虚拟机对应的用户名和密码
5. THE cluster-config.fxml SHALL 不包含 normalUserField 和 normalPasswordField 控件

### 需求 3：主机名首字母大写

**用户故事：** 作为系统用户，我希望主机名遵循标准命名规范（首字母大写），这样集群配置看起来更加专业和规范。

#### 验收标准

1. THE ClusterConfigController SHALL 将虚拟机 1 的默认主机名设置为"Hadoop101"
2. THE ClusterConfigController SHALL 将虚拟机 2 的默认主机名设置为"Hadoop102"
3. THE ClusterConfigController SHALL 将虚拟机 3 的默认主机名设置为"Hadoop103"
4. WHEN 用户修改主机名，THE ClusterConfigController SHALL 允许用户输入自定义主机名
5. THE cluster-config.fxml SHALL 在主机名输入框中显示首字母大写的默认值

### 需求 4：JDK 本地文件上传支持

**用户故事：** 作为系统用户，我希望能够上传本地的 JDK 安装包，这样我就可以使用自定义版本的 JDK 而不局限于预设版本。

#### 验收标准

1. THE ClusterConfigController SHALL 提供"预设版本"和"本地上传"两个单选按钮用于 JDK 来源选择
2. WHEN 用户选择"预设版本"单选按钮，THE ClusterConfigController SHALL 启用 JDK 版本下拉框并禁用文件路径输入框和浏览按钮
3. WHEN 用户选择"本地上传"单选按钮，THE ClusterConfigController SHALL 禁用 JDK 版本下拉框并启用文件路径输入框和浏览按钮
4. WHEN 用户点击 JDK 浏览按钮，THE ClusterConfigController SHALL 打开文件选择对话框并过滤显示.tar.gz、.tgz、.zip 格式的文件
5. WHEN 用户选择本地 JDK 文件，THE ClusterConfigController SHALL 在文件路径输入框中显示所选文件的完整路径
6. WHEN 用户选择本地上传且未选择文件，THE ClusterConfigController SHALL 在开始部署时显示错误提示
7. THE FileTransferService SHALL 支持将本地 JDK 文件上传到三台虚拟机的指定目录

### 需求 5：Hadoop 本地文件上传支持

**用户故事：** 作为系统用户，我希望能够上传本地的 Hadoop 安装包，这样我就可以使用自定义版本的 Hadoop 而不局限于预设版本。

#### 验收标准

1. THE ClusterConfigController SHALL 提供"预设版本"和"本地上传"两个单选按钮用于 Hadoop 来源选择
2. WHEN 用户选择"预设版本"单选按钮，THE ClusterConfigController SHALL 启用 Hadoop 版本下拉框并禁用文件路径输入框和浏览按钮
3. WHEN 用户选择"本地上传"单选按钮，THE ClusterConfigController SHALL 禁用 Hadoop 版本下拉框并启用文件路径输入框和浏览按钮
4. WHEN 用户点击 Hadoop 浏览按钮，THE ClusterConfigController SHALL 打开文件选择对话框并过滤显示.tar.gz、.tgz、.zip 格式的文件
5. WHEN 用户选择本地 Hadoop 文件，THE ClusterConfigController SHALL 在文件路径输入框中显示所选文件的完整路径
6. WHEN 用户选择本地上传且未选择文件，THE ClusterConfigController SHALL 在开始部署时显示错误提示
7. THE FileTransferService SHALL 支持将本地 Hadoop 文件上传到三台虚拟机的指定目录

### 需求 6：真实 SSH 远程部署操作

**用户故事：** 作为系统用户，我希望部署日志能够反映真实的虚拟机操作过程，这样我就能了解系统实际在虚拟机上执行了哪些操作。

#### 验收标准

1. WHEN 用户点击"开始部署"按钮，THE DeploymentService SHALL 通过 SSH 连接到三台虚拟机
2. WHEN DeploymentService 执行部署步骤，THE DeploymentService SHALL 在虚拟机上执行实际的 Shell 命令
3. WHEN DeploymentService 执行命令，THE DeploymentService SHALL 将 SSH 命令执行的输出记录到部署日志中
4. WHEN DeploymentService 配置主机名，THE DeploymentService SHALL 在虚拟机上执行 hostnamectl 命令修改主机名
5. WHEN DeploymentService 配置 hosts 文件，THE DeploymentService SHALL 通过 SSH 在虚拟机上修改/etc/hosts 文件
6. WHEN DeploymentService 安装 JDK，THE DeploymentService SHALL 在虚拟机上解压 JDK 文件并配置环境变量
7. WHEN DeploymentService 安装 Hadoop，THE DeploymentService SHALL 在虚拟机上解压 Hadoop 文件并配置环境变量
8. WHEN DeploymentService 遇到 SSH 连接错误，THE DeploymentService SHALL 记录详细的错误信息到日志并通知用户
9. WHEN DeploymentService 遇到命令执行失败，THE DeploymentService SHALL 记录命令输出和错误信息到日志
10. THE DeploymentService SHALL 移除所有本地模拟的部署逻辑
11. THE SSHService SHALL 提供 executeCommand 方法用于在远程虚拟机上执行 Shell 命令
12. THE SSHService SHALL 提供 uploadFile 方法用于上传文件到远程虚拟机

### 需求 7：部署进度实时反馈

**用户故事：** 作为系统用户，我希望在部署过程中看到实时的进度更新和日志输出，这样我就能了解部署的当前状态和是否出现问题。

#### 验收标准

1. WHEN DeploymentService 开始执行部署步骤，THE DeploymentProgressController SHALL 更新当前步骤显示
2. WHEN DeploymentService 完成一个部署步骤，THE DeploymentProgressController SHALL 更新整体进度百分比
3. WHEN DeploymentService 记录日志，THE DeploymentProgressController SHALL 在部署日志文本区域实时显示日志内容
4. WHEN DeploymentService 执行 SSH 命令，THE DeploymentProgressController SHALL 显示命令执行的输出结果
5. WHEN DeploymentService 上传文件，THE DeploymentProgressController SHALL 显示文件传输进度
6. WHEN 部署过程中发生错误，THE DeploymentProgressController SHALL 以 ERROR 级别显示错误信息
7. THE DeploymentProgressController SHALL 自动滚动日志文本区域以显示最新的日志条目

### 需求 8：配置信息验证

**用户故事：** 作为系统用户，我希望系统在开始部署前验证我的配置信息，这样可以避免因配置错误导致部署失败。

#### 验收标准

1. WHEN 用户点击"开始部署"按钮，THE ClusterConfigController SHALL 验证所有主机名不为空
2. WHEN 用户选择本地上传 JDK，THE ClusterConfigController SHALL 验证 JDK 文件路径不为空
3. WHEN 用户选择本地上传 Hadoop，THE ClusterConfigController SHALL 验证 Hadoop 文件路径不为空
4. WHEN 用户选择本地文件，THE ClusterConfigController SHALL 验证文件存在且可读
5. WHEN 配置验证失败，THE ClusterConfigController SHALL 显示具体的错误提示信息
6. WHEN 配置验证失败，THE ClusterConfigController SHALL 阻止进入部署进度界面
7. WHEN 配置验证成功，THE ClusterConfigController SHALL 收集所有配置信息并传递给 DeploymentService
