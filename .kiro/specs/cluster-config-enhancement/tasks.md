# 实现计划 - 集群配置增强

本文档定义了集群配置增强功能的实现任务列表。每个任务都是可执行的代码实现步骤，按照依赖关系组织。

## 任务列表

- [x] 1. 创建数据模型类

- [ ] 1.1 创建 SourceType 枚举类

  - 定义 PRESET 和 LOCAL_FILE 两种类型
  - 添加 description 字段和 getter 方法

  - _需求: 4.1, 5.1_

- [ ] 1.2 创建 JDKConfig 模型类

  - 定义 sourceType、presetVersion、localFilePath 等字段

  - 实现 getter 和 setter 方法
  - 添加 toString 方法
  - _需求: 4.1, 4.2, 4.3_

- [x] 1.3 创建 HadoopConfig 模型类

  - 定义 sourceType、presetVersion、localFilePath 等字段
  - 实现 getter 和 setter 方法
  - 添加 hdfsBlockSize 和 yarnMemory 配置
  - _需求: 5.1, 5.2, 5.3_

- [ ] 1.4 创建 ClusterConfig 模型类

  - 定义 NameNode、SecondaryNameNode、ResourceManager 配置
  - 定义 DataNode 和 NodeManager 列表

  - 定义数据目录路径
  - _需求: 6.4, 6.5, 6.6_

- [x] 1.5 创建 DeploymentConfig 模型类

  - 聚合 VMConnectionConfig、JDKConfig、HadoopConfig、ClusterConfig
  - 添加 hostnames 数组和 deployMode 字段

  - 添加 roleAssignments 映射
  - _需求: 1.4, 2.4, 3.1_

- [x] 1.6 创建 UploadResult 模型类

  - 定义 vmIndex、vmIp、success、fileSize 等字段
  - 添加 errorMessage 字段用于错误信息
  - _需求: 4.7, 5.7_

- [ ] 1.7 创建 CommandResult 模型类

  - 定义 command、exitCode、output、error 字段
  - 添加 executionTime 和 success 字段
  - 提供静态工厂方法创建成功/失败结果

  - _需求: 6.2, 6.3, 6.9_

- [ ] 2. 实现 FileTransferService 服务类
- [x] 2.1 创建 FileTransferService 类骨架

  - 定义类和基本字段（logger）

  - 添加构造函数
  - _需求: 4.7, 5.7_

- [x] 2.2 实现 uploadFile 方法

  - 使用 JSch 的 ChannelSftp 上传文件
  - 实现进度监控回调
  - 添加异常处理和日志记录
  - _需求: 4.7, 5.7_

- [ ] 2.3 实现 uploadToAllVMs 方法

  - 使用线程池并行上传到多台虚拟机
  - 收集每台虚拟机的上传结果
  - 返回 UploadResult 列表

  - _需求: 4.7, 5.7_

- [ ] 2.4 实现 verifyRemoteFile 方法

  - 通过 SSH 执行 ls 命令检查文件是否存在

  - 返回布尔值表示文件存在性
  - _需求: 4.7, 5.7_

- [ ] 2.5 实现 createRemoteDir 辅助方法

  - 通过 SFTP 创建远程目录（支持递归创建）
  - 处理目录已存在的情况

  - _需求: 4.7, 5.7_

- [ ] 3. 增强 SSHConnectionService 服务类
- [ ] 3.1 添加 executeCommandWithLog 方法

  - 执行 SSH 命令并实时输出日志
  - 使用 LogCallback 接口回调日志

  - 返回 CommandResult 对象
  - _需求: 6.2, 6.3, 7.3, 7.4_

- [ ] 3.2 添加 executeCommands 批量执行方法

  - 接收命令列表并依次执行
  - 每个命令执行后回调日志

  - 返回 CommandResult 列表
  - _需求: 6.2, 6.3_

- [ ] 3.3 创建 LogCallback 接口

  - 定义 onLog、onError、onComplete 方法

  - 用于实时日志回调
  - _需求: 7.3, 7.4_

- [ ] 4. 实现 DeploymentService 服务类
- [x] 4.1 创建 DeploymentService 类骨架

  - 注入 SSHConnectionService 和 FileTransferService
  - 定义部署流程常量（目录路径、命令模板等）
  - _需求: 6.1, 6.2_

- [ ] 4.2 实现 deploy 主方法

  - 定义完整的部署流程（5 个阶段）
  - 使用 Task 在后台线程执行

  - 通过 DeploymentProgressListener 回调进度
  - _需求: 6.1, 7.1, 7.2_

- [ ] 4.3 实现 configureEnvironment 方法

  - 配置主机名（hostnamectl set-hostname）

  - 配置 hosts 文件（echo >> /etc/hosts）
  - 关闭防火墙（systemctl stop firewalld）
  - 禁用 SELinux（setenforce 0）
  - _需求: 6.4, 6.5_

- [ ] 4.4 实现 installJDK 方法

  - 根据 sourceType 判断是预设版本还是本地上传
  - 预设版本：从远程下载 JDK
  - 本地上传：调用 FileTransferService 上传文件
  - 解压 JDK 并配置 JAVA_HOME 环境变量

  - _需求: 6.6, 4.7_

- [ ] 4.5 实现 installHadoop 方法

  - 根据 sourceType 判断是预设版本还是本地上传
  - 预设版本：从远程下载 Hadoop
  - 本地上传：调用 FileTransferService 上传文件

  - 解压 Hadoop 并配置 HADOOP_HOME 环境变量
  - _需求: 6.7, 5.7_

- [ ] 4.6 实现 distributeConfigs 方法

  - 使用 FreeMarker 生成配置文件
  - 将配置文件上传到所有虚拟机

  - 验证配置文件正确性
  - _需求: 6.7_

- [ ] 4.7 实现 initializeHDFS 方法

  - 在 NameNode 上执行 hdfs namenode -format

  - 创建必要的 HDFS 目录
  - _需求: 6.7_

- [ ] 4.8 实现 startClusterServices 方法

  - 启动 HDFS 服务（start-dfs.sh）

  - 启动 YARN 服务（start-yarn.sh）
  - 验证服务启动成功
  - _需求: 6.7_

- [x] 5. 重构 ClusterConfigController 控制器
- [x] 5.1 移除普通用户配置相关代码

  - 删除 normalUserField 和 normalPasswordField 字段
  - 从 initialize 方法中移除相关初始化代码
  - _需求: 2.1, 2.2, 2.3_

- [ ] 5.2 修改主机名默认值为首字母大写

  - 修改 vm1HostnameField 默认值为"Hadoop101"
  - 修改 vm2HostnameField 默认值为"Hadoop102"
  - 修改 vm3HostnameField 默认值为"Hadoop103"
  - _需求: 3.1, 3.2, 3.3_

- [ ] 5.3 添加连接信息存储字段

  - 添加 vmIps、vmUsernames、vmPasswords 数组
  - _需求: 1.3, 2.4_

- [ ] 5.4 实现 setVmConnectionInfo 方法

  - 接收 9 个参数（3 台虚拟机的 IP、用户名、密码）
  - 填充 IP 地址到界面字段

  - 存储连接信息到内存数组
  - 记录日志
  - _需求: 1.1, 1.2, 1.3, 1.4_

- [x] 5.5 添加 JDK 配置相关控件

  - 添加 jdkPresetRadio、jdkUploadRadio 单选按钮
  - 添加 jdkSourceGroup 切换组
  - 添加 jdkFilePathField 文件路径输入框
  - 添加 jdkBrowseBtn 浏览按钮

  - _需求: 4.1, 4.2, 4.3_

- [ ] 5.6 添加 Hadoop 配置相关控件

  - 添加 hadoopPresetRadio、hadoopUploadRadio 单选按钮
  - 添加 hadoopSourceGroup 切换组
  - 添加 hadoopFilePathField 文件路径输入框
  - 添加 hadoopBrowseBtn 浏览按钮
  - _需求: 5.1, 5.2, 5.3_

- [ ] 5.7 实现 setupRadioButtonListeners 方法

  - 监听 jdkSourceGroup 选择变化
  - 监听 hadoopSourceGroup 选择变化
  - 根据选择启用/禁用相应控件
  - _需求: 4.2, 5.2_

- [ ] 5.8 实现 handleBrowseJdk 方法

  - 创建 FileChooser 并设置文件过滤器
  - 打开文件选择对话框
  - 将选择的文件路径填充到 jdkFilePathField
  - _需求: 4.4, 4.5_

- [ ] 5.9 实现 handleBrowseHadoop 方法

  - 创建 FileChooser 并设置文件过滤器
  - 打开文件选择对话框
  - 将选择的文件路径填充到 hadoopFilePathField
  - _需求: 5.4, 5.5_

- [ ] 5.10 实现 validateConfiguration 方法

  - 验证主机名不为空
  - 验证本地上传时文件路径不为空
  - 验证本地文件存在且可读
  - 返回验证结果
  - _需求: 8.1, 8.2, 8.3, 8.4_

- [ ] 5.11 实现 buildDeploymentConfig 方法

  - 构建 JDKConfig 对象
  - 构建 HadoopConfig 对象
  - 构建 ClusterConfig 对象
  - 构建 DeploymentConfig 对象
  - _需求: 8.7_





- [ ] 5.12 修改 handleStartDeploy 方法

  - 调用 validateConfiguration 验证配置
  - 调用 buildDeploymentConfig 构建配置对象



  - 加载部署进度界面
  - 传递 DeploymentConfig 到 DeploymentProgressController
  - _需求: 8.5, 8.6, 8.7_

- [x] 5.13 完善 handlePrevious 方法


  - 加载部署模式选择界面
  - 切换场景
  - _需求: 2.4_

- [x] 5.14 完善 handleCancel 方法


  - 显示确认对话框
  - 用户确认后退出应用
  - _需求: 2.4_

- [x] 6. 修改 DeployModeController 控制器

- [ ] 6.1 修改 handleNext 方法传递连接信息

  - 从 ConfigService 加载虚拟机连接配置
  - 加载 ClusterConfigController
  - 调用 setVmConnectionInfo 传递连接信息
  - 切换到集群配置界面
  - _需求: 1.4_


- [ ] 7. 更新 cluster-config.fxml 界面文件
- [ ] 7.1 移除系统参数区域

  - 删除 normalUserField 和 normalPasswordField 控件
  - 删除相关 Label 和布局容器




  - _需求: 2.1, 2.2, 2.5_

- [ ] 7.2 修改主机名默认值


  - 修改 vm1HostnameField 的 text 属性为"Hadoop101"
  - 修改 vm2HostnameField 的 text 属性为"Hadoop102"
  - 修改 vm3HostnameField 的 text 属性为"Hadoop103"
  - _需求: 3.1, 3.2, 3.3, 3.5_


- [ ] 7.3 重构 Hadoop 组件配置区域

  - 添加 JDK 配置子区域（单选按钮+下拉框+文件选择）
  - 添加 Hadoop 配置子区域（单选按钮+下拉框+文件选择）
  - 保留 HDFS 块大小和 YARN 内存配置

  - _需求: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3_

- [ ] 7.4 添加 JDK 配置控件

  - 添加 jdkPresetRadio 和 jdkUploadRadio 单选按钮
  - 添加 jdkVersionCombo 下拉框
  - 添加 jdkFilePathField 文本框
  - 添加 jdkBrowseBtn 按钮，绑定 handleBrowseJdk 方法
  - _需求: 4.1, 4.2, 4.3, 4.4_

- [ ] 7.5 添加 Hadoop 配置控件

  - 添加 hadoopPresetRadio 和 hadoopUploadRadio 单选按钮
  - 添加 hadoopVersionCombo 下拉框
  - 添加 hadoopFilePathField 文本框
  - 添加 hadoopBrowseBtn 按钮，绑定 handleBrowseHadoop 方法
  - _需求: 5.1, 5.2, 5.3, 5.4_

- [ ] 8. 更新 cluster-config.css 样式文件
- [ ] 8.1 添加 subsection-title 样式类

  - 定义字体颜色、大小、粗细
  - _需求: 4.1, 5.1_

- [ ] 8.2 添加 config-radio 样式类

  - 定义单选按钮的文字颜色和大小
  - 定义选中状态的样式
  - _需求: 4.1, 5.1_

- [ ] 8.3 添加 file-path-field 样式类

  - 定义文件路径输入框的背景色、边框、内边距
  - 定义焦点状态的样式
  - _需求: 4.3, 5.3_

- [ ] 8.4 添加 browse-btn 样式类

  - 定义浏览按钮的背景色、文字颜色
  - 定义 hover 和 pressed 状态的样式
  - 定义 disabled 状态的样式
  - _需求: 4.3, 5.3_

- [ ] 9. 增强 DeploymentProgressController 控制器
- [ ] 9.1 添加 deploymentConfig 字段

  - 接收从 ClusterConfigController 传递的配置
  - _需求: 7.1_

- [ ] 9.2 实现 setDeploymentConfig 方法

  - 接收 DeploymentConfig 参数
  - 存储到字段中
  - _需求: 7.1_

- [ ] 9.3 修改 startDeployment 方法

  - 创建 DeploymentService 实例
  - 调用 deploy 方法并传递 deploymentConfig
  - 实现 DeploymentProgressListener 接口
  - _需求: 7.1, 7.2_

- [ ] 9.4 实现 DeploymentProgressListener 回调方法

  - 实现 onStepChange 更新当前步骤显示
  - 实现 onProgressChange 更新进度百分比
  - 实现 onLog 在日志区域显示日志
  - 实现 onError 显示错误信息
  - _需求: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

- [ ] 9.5 实现日志自动滚动功能

  - 监听日志文本区域内容变化
  - 自动滚动到最新日志
  - _需求: 7.7_

- [ ] 10. 创建 FreeMarker 配置文件模板
- [ ] 10.1 创建 hadoop-env.sh.ftl 模板

  - 定义 JAVA_HOME 变量
  - 定义 HADOOP_HOME 变量
  - _需求: 6.7_

- [ ] 10.2 创建 core-site.xml.ftl 模板

  - 定义 fs.defaultFS 属性
  - 定义 hadoop.tmp.dir 属性
  - _需求: 6.7_

- [ ] 10.3 创建 hdfs-site.xml.ftl 模板

  - 定义 dfs.replication 属性
  - 定义 dfs.namenode.name.dir 属性
  - 定义 dfs.datanode.data.dir 属性
  - 定义 dfs.blocksize 属性
  - _需求: 6.7_

- [ ] 10.4 创建 yarn-site.xml.ftl 模板

  - 定义 yarn.resourcemanager.hostname 属性
  - 定义 yarn.nodemanager.resource.memory-mb 属性
  - _需求: 6.7_

- [ ] 10.5 创建 workers.ftl 模板

  - 列出所有 DataNode 主机名
  - _需求: 6.7_

- [ ] 11. 集成测试和验证
- [ ] 11.1 测试连接信息传递

  - 从 ConnectionController 输入连接信息
  - 验证 ClusterConfigController 正确接收并显示
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 11.2 测试主机名首字母大写

  - 验证界面显示 Hadoop101/102/103
  - _需求: 3.1, 3.2, 3.3_

- [ ] 11.3 测试 JDK 本地上传功能

  - 选择"本地上传"单选按钮
  - 点击浏览按钮选择文件
  - 验证文件路径正确显示
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 11.4 测试 Hadoop 本地上传功能

  - 选择"本地上传"单选按钮
  - 点击浏览按钮选择文件
  - 验证文件路径正确显示
  - _需求: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 11.5 测试配置验证功能

  - 测试主机名为空的错误提示
  - 测试本地上传未选择文件的错误提示
  - 测试文件不存在的错误提示
  - _需求: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

- [ ] 11.6 测试真实 SSH 部署操作

  - 准备 3 台 CentOS 虚拟机
  - 执行完整部署流程
  - 验证虚拟机上主机名配置正确
  - 验证虚拟机上 hosts 文件配置正确
  - 验证 JDK 和 Hadoop 安装成功
  - 验证配置文件生成正确
  - 验证 HDFS 和 YARN 服务启动成功
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10, 6.11, 6.12_

- [ ] 11.7 测试部署日志实时显示
  - 验证 SSH 命令输出实时显示在日志区域
  - 验证错误信息以 ERROR 级别显示
  - 验证日志自动滚动到最新条目
  - _需求: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

## 实现顺序建议

1. **第一阶段：数据模型** - 任务 1（1.1-1.7）
2. **第二阶段：服务层** - 任务 2、3、4（2.1-4.8）
3. **第三阶段：界面层** - 任务 5、6、7、8（5.1-8.4）
4. **第四阶段：部署进度** - 任务 9（9.1-9.5）
5. **第五阶段：配置模板** - 任务 10（10.1-10.5）
6. **第六阶段：集成测试** - 任务 11（11.1-11.7）

## 注意事项

1. 每个任务完成后进行单元测试
2. 任务 4（DeploymentService）是核心，需要仔细实现
3. 任务 11.6 需要真实的虚拟机环境
4. 所有 SSH 操作都要有详细的日志记录
5. 文件上传要支持大文件和进度显示
