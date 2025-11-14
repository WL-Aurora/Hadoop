# 设计文档 - 集群配置增强

## 概述

本设计文档描述了 Hadoop 集群参数配置界面的增强方案，包括虚拟机连接信息的自动传递、本地文件上传功能、以及真实 SSH 远程部署操作的技术实现。

### 设计目标

1. **简化用户操作** - 自动传递连接信息，避免重复输入
2. **增强灵活性** - 支持本地 JDK 和 Hadoop 文件上传
3. **真实部署** - 通过 SSH 在虚拟机上执行真实操作
4. **实时反馈** - 显示真实的 SSH 命令执行输出

### 技术栈

- **JavaFX 17+** - 图形界面框架
- **JSch** - SSH 连接和命令执行
- **FreeMarker** - 配置文件模板引擎
- **log4j2** - 日志管理

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Connection   │→ │ DeployMode   │→ │ClusterConfig │      │
│  │ Controller   │  │ Controller   │  │ Controller   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         ↓                                    ↓               │
└─────────────────────────────────────────────────────────────┘
         ↓                                    ↓
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ SSHConnection│  │FileTransfer  │  │ Deployment   │      │
│  │   Service    │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
         ↓                    ↓                   ↓
┌─────────────────────────────────────────────────────────────┐
│                        Utility Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   SSHUtil    │  │  FileUtil    │  │ NetworkUtil  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 数据流设计

```
ConnectionController (输入连接信息)
         ↓
    保存到内存
         ↓
DeployModeController (选择部署模式)
         ↓
    传递连接信息
         ↓
ClusterConfigController (配置集群参数)
         ↓
    收集所有配置
         ↓
DeploymentService (执行部署)
         ↓
    SSH远程操作虚拟机
```

## 组件和接口设计

### 1. 控制器层 (Controller Layer)

#### 1.1 ConnectionController 增强

**职责：** 收集并存储虚拟机连接信息

**新增方法：**

```java
/**
 * 获取虚拟机连接配置列表
 * @return 三台虚拟机的连接配置
 */
public List<VMConnectionConfig> getVmConnectionConfigs()

/**
 * 获取虚拟机连接信息数组
 * @return [IP数组, 用户名数组, 密码数组]
 */
public String[][] getConnectionInfo()
```

**数据存储：**

- 在内存中保存三台虚拟机的连接配置
- 通过 ConfigService 持久化到本地文件

#### 1.2 DeployModeController 增强

**职责：** 传递连接信息到下一个界面

**修改方法：**

```java
@FXML
private void handleNext() {
    // 1. 加载ClusterConfigController
    FXMLLoader loader = new FXMLLoader(...);
    Parent root = loader.load();
    ClusterConfigController controller = loader.getController();

    // 2. 从ConfigService读取连接信息
    List<VMConnectionConfig> configs = configService.loadConfig();

    // 3. 传递连接信息
    controller.setVmConnectionInfo(
        configs.get(0).getIp(), configs.get(1).getIp(), configs.get(2).getIp(),
        configs.get(0).getUsername(), configs.get(1).getUsername(), configs.get(2).getUsername(),
        configs.get(0).getPassword(), configs.get(1).getPassword(), configs.get(2).getPassword()
    );

    // 4. 切换场景
    stage.setScene(new Scene(root));
}
```

#### 1.3 ClusterConfigController 重构

**职责：** 集群参数配置和验证

**新增字段：**

```java
// 连接信息存储
private String[] vmIps = new String[3];
private String[] vmUsernames = new String[3];
private String[] vmPasswords = new String[3];

// JDK配置
@FXML private RadioButton jdkPresetRadio;
@FXML private RadioButton jdkUploadRadio;
@FXML private ToggleGroup jdkSourceGroup;
@FXML private ComboBox<String> jdkVersionCombo;
@FXML private TextField jdkFilePathField;
@FXML private Button jdkBrowseBtn;

// Hadoop配置
@FXML private RadioButton hadoopPresetRadio;
@FXML private RadioButton hadoopUploadRadio;
@FXML private ToggleGroup hadoopSourceGroup;
@FXML private ComboBox<String> hadoopVersionCombo;
@FXML private TextField hadoopFilePathField;
@FXML private Button hadoopBrowseBtn;
```

**新增方法：**

```java
/**
 * 设置虚拟机连接信息
 */
public void setVmConnectionInfo(String vm1Ip, String vm2Ip, String vm3Ip,
                               String vm1User, String vm2User, String vm3User,
                               String vm1Pass, String vm2Pass, String vm3Pass)

/**
 * 浏览JDK文件
 */
@FXML
private void handleBrowseJdk()

/**
 * 浏览Hadoop文件
 */
@FXML
private void handleBrowseHadoop()

/**
 * 验证配置信息
 */
private boolean validateConfiguration()

/**
 * 构建部署配置对象
 */
private DeploymentConfig buildDeploymentConfig()
```

### 2. 服务层 (Service Layer)

#### 2.1 FileTransferService (新建)

**职责：** 处理本地文件上传到虚拟机

**接口设计：**

```java
public class FileTransferService {

    /**
     * 上传文件到单台虚拟机
     * @param session SSH会话
     * @param localFilePath 本地文件路径
     * @param remoteDir 远程目录
     * @param progressCallback 进度回调
     * @return 上传是否成功
     */
    public boolean uploadFile(Session session, String localFilePath,
                             String remoteDir, ProgressCallback progressCallback)

    /**
     * 批量上传文件到多台虚拟机
     * @param configs 虚拟机配置列表
     * @param localFilePath 本地文件路径
     * @param remoteDir 远程目录
     * @param progressCallback 进度回调
     * @return 上传结果列表
     */
    public List<UploadResult> uploadToAllVMs(List<VMConnectionConfig> configs,
                                            String localFilePath,
                                            String remoteDir,
                                            ProgressCallback progressCallback)

    /**
     * 验证远程文件是否存在
     * @param session SSH会话
     * @param remoteFilePath 远程文件路径
     * @return 文件是否存在
     */
    public boolean verifyRemoteFile(Session session, String remoteFilePath)
}
```

**实现要点：**

- 使用 JSch 的 ChannelSftp 进行文件传输
- 支持大文件传输（分块上传）
- 提供进度回调接口
- 支持断点续传（可选）

#### 2.2 DeploymentService (新建)

**职责：** 执行真实的 SSH 远程部署操作

**接口设计：**

```java
public class DeploymentService {

    private final SSHConnectionService sshService;
    private final FileTransferService fileTransferService;
    private final ConfigGeneratorService configGeneratorService;

    /**
     * 执行完整部署流程
     * @param deployConfig 部署配置
     * @param progressListener 进度监听器
     */
    public void deploy(DeploymentConfig deployConfig,
                      DeploymentProgressListener progressListener)

    /**
     * 配置主机名
     * @param session SSH会话
     * @param hostname 主机名
     */
    private void configureHostname(Session session, String hostname)

    /**
     * 配置hosts文件
     * @param session SSH会话
     * @param hostsEntries hosts条目列表
     */
    private void configureHosts(Session session, List<HostEntry> hostsEntries)

    /**
     * 安装JDK
     * @param session SSH会话
     * @param jdkSource JDK来源（预设或本地文件）
     * @param jdkPath JDK路径
     */
    private void installJDK(Session session, String jdkSource, String jdkPath)

    /**
     * 安装Hadoop
     * @param session SSH会话
     * @param hadoopSource Hadoop来源
     * @param hadoopPath Hadoop路径
     */
    private void installHadoop(Session session, String hadoopSource, String hadoopPath)

    /**
     * 生成并分发配置文件
     * @param configs 虚拟机配置列表
     * @param clusterConfig 集群配置
     */
    private void distributeConfigs(List<VMConnectionConfig> configs,
                                  ClusterConfig clusterConfig)

    /**
     * 初始化HDFS
     * @param nameNodeSession NameNode的SSH会话
     */
    private void initializeHDFS(Session nameNodeSession)

    /**
     * 启动集群服务
     * @param configs 虚拟机配置列表
     * @param deployMode 部署模式
     */
    private void startClusterServices(List<VMConnectionConfig> configs,
                                     DeployMode deployMode)
}
```

**部署流程：**

```
1. 环境预处理
   ├─ 配置主机名 (hostnamectl set-hostname)
   ├─ 配置hosts文件 (echo >> /etc/hosts)
   ├─ 关闭防火墙 (systemctl stop firewalld)
   └─ 禁用SELinux (setenforce 0)

2. 软件安装
   ├─ 上传/下载JDK
   ├─ 解压JDK (tar -zxvf)
   ├─ 配置JAVA_HOME
   ├─ 上传/下载Hadoop
   ├─ 解压Hadoop
   └─ 配置HADOOP_HOME

3. 配置文件生成与分发
   ├─ 生成hadoop-env.sh
   ├─ 生成core-site.xml
   ├─ 生成hdfs-site.xml
   ├─ 生成mapred-site.xml
   ├─ 生成yarn-site.xml
   ├─ 生成workers文件
   └─ 分发到所有节点

4. 集群初始化
   ├─ 格式化NameNode (hdfs namenode -format)
   └─ 创建必要目录

5. 启动服务
   ├─ 启动HDFS (start-dfs.sh)
   └─ 启动YARN (start-yarn.sh)
```

#### 2.3 SSHConnectionService 增强

**新增方法：**

```java
/**
 * 执行命令并实时输出日志
 * @param session SSH会话
 * @param command 命令
 * @param logCallback 日志回调
 * @return 命令执行结果
 */
public CommandResult executeCommandWithLog(Session session, String command,
                                          LogCallback logCallback)

/**
 * 批量执行命令
 * @param session SSH会话
 * @param commands 命令列表
 * @param logCallback 日志回调
 * @return 执行结果列表
 */
public List<CommandResult> executeCommands(Session session, List<String> commands,
                                          LogCallback logCallback)
```

## 数据模型设计

### 1. DeploymentConfig (新建)

**职责：** 封装完整的部署配置信息

```java
public class DeploymentConfig {
    // 虚拟机连接信息
    private List<VMConnectionConfig> vmConfigs;

    // 网络配置
    private String[] hostnames;  // [Hadoop101, Hadoop102, Hadoop103]

    // JDK配置
    private JDKConfig jdkConfig;

    // Hadoop配置
    private HadoopConfig hadoopConfig;

    // 集群配置
    private ClusterConfig clusterConfig;

    // 部署模式
    private DeployMode deployMode;

    // 角色分配（自定义模式）
    private Map<Integer, List<NodeRole>> roleAssignments;
}
```

### 2. JDKConfig (新建)

**职责：** JDK 配置信息

```java
public class JDKConfig {
    // JDK来源类型
    private SourceType sourceType;  // PRESET | LOCAL_FILE

    // 预设版本（sourceType=PRESET时使用）
    private String presetVersion;  // "JDK 1.8.0_212"

    // 本地文件路径（sourceType=LOCAL_FILE时使用）
    private String localFilePath;

    // 远程安装目录
    private String remoteInstallDir;  // "/opt/module/jdk"

    // 环境变量配置
    private String javaHome;
}
```

### 3. HadoopConfig (新建)

**职责：** Hadoop 配置信息

```java
public class HadoopConfig {
    // Hadoop来源类型
    private SourceType sourceType;  // PRESET | LOCAL_FILE

    // 预设版本
    private String presetVersion;  // "Hadoop 3.1.3"

    // 本地文件路径
    private String localFilePath;

    // 远程安装目录
    private String remoteInstallDir;  // "/opt/module/hadoop"

    // HDFS配置
    private String hdfsBlockSize;  // "128MB"

    // YARN配置
    private String yarnMemory;  // "2GB"
}
```

### 4. ClusterConfig (新建)

**职责：** 集群级别配置

```java
public class ClusterConfig {
    // NameNode配置
    private String nameNodeHost;
    private int nameNodePort;  // 9000
    private int nameNodeHttpPort;  // 9870

    // SecondaryNameNode配置
    private String secondaryNameNodeHost;
    private int secondaryNameNodeHttpPort;  // 9868

    // ResourceManager配置
    private String resourceManagerHost;
    private int resourceManagerPort;  // 8032
    private int resourceManagerWebPort;  // 8088

    // DataNode配置
    private List<String> dataNodeHosts;

    // NodeManager配置
    private List<String> nodeManagerHosts;

    // 数据目录
    private String hdfsDataDir;  // "/opt/module/hadoop/data"
    private String hdfsNameDir;  // "/opt/module/hadoop/name"
    private String hdfsTempDir;  // "/opt/module/hadoop/tmp"
}
```

### 5. UploadResult (新建)

**职责：** 文件上传结果

```java
public class UploadResult {
    private int vmIndex;
    private String vmIp;
    private boolean success;
    private String localFilePath;
    private String remoteFilePath;
    private long fileSize;
    private long uploadTime;  // 毫秒
    private String errorMessage;
}
```

### 6. CommandResult (新建)

**职责：** 命令执行结果

```java
public class CommandResult {
    private String command;
    private int exitCode;
    private String output;
    private String error;
    private long executionTime;  // 毫秒
    private boolean success;
}
```

### 7. SourceType (新建枚举)

**职责：** 软件来源类型

```java
public enum SourceType {
    PRESET("预设版本"),
    LOCAL_FILE("本地上传");

    private final String description;

    SourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

## 界面设计

### 1. cluster-config.fxml 重构

**布局结构：**

```xml
<VBox>
    <!-- 标题 -->
    <Label text="集群参数配置"/>

    <ScrollPane>
        <VBox spacing="15">
            <!-- 网络配置区 -->
            <VBox styleClass="config-section">
                <Label text="网络配置" styleClass="section-title"/>
                <GridPane>
                    <!-- VM1: IP(只读) + 主机名 -->
                    <TextField fx:id="vm1IpField" editable="false"/>
                    <TextField fx:id="vm1HostnameField" text="Hadoop101"/>

                    <!-- VM2: IP(只读) + 主机名 -->
                    <TextField fx:id="vm2IpField" editable="false"/>
                    <TextField fx:id="vm2HostnameField" text="Hadoop102"/>

                    <!-- VM3: IP(只读) + 主机名 -->
                    <TextField fx:id="vm3IpField" editable="false"/>
                    <TextField fx:id="vm3HostnameField" text="Hadoop103"/>
                </GridPane>
            </VBox>

            <!-- Hadoop组件配置区 -->
            <VBox styleClass="config-section">
                <Label text="Hadoop 组件配置" styleClass="section-title"/>

                <!-- JDK配置 -->
                <VBox spacing="8">
                    <Label text="JDK 配置" styleClass="subsection-title"/>
                    <HBox>
                        <RadioButton fx:id="jdkPresetRadio" text="预设版本:"
                                    selected="true" toggleGroup="$jdkSourceGroup"/>
                        <ComboBox fx:id="jdkVersionCombo"/>
                    </HBox>
                    <HBox>
                        <RadioButton fx:id="jdkUploadRadio" text="本地上传:"
                                    toggleGroup="$jdkSourceGroup"/>
                        <TextField fx:id="jdkFilePathField" disable="true"/>
                        <Button fx:id="jdkBrowseBtn" text="浏览"
                               onAction="#handleBrowseJdk" disable="true"/>
                    </HBox>
                </VBox>

                <!-- Hadoop配置 -->
                <VBox spacing="8">
                    <Label text="Hadoop 配置" styleClass="subsection-title"/>
                    <HBox>
                        <RadioButton fx:id="hadoopPresetRadio" text="预设版本:"
                                    selected="true" toggleGroup="$hadoopSourceGroup"/>
                        <ComboBox fx:id="hadoopVersionCombo"/>
                    </HBox>
                    <HBox>
                        <RadioButton fx:id="hadoopUploadRadio" text="本地上传:"
                                    toggleGroup="$hadoopSourceGroup"/>
                        <TextField fx:id="hadoopFilePathField" disable="true"/>
                        <Button fx:id="hadoopBrowseBtn" text="浏览"
                               onAction="#handleBrowseHadoop" disable="true"/>
                    </HBox>
                </VBox>

                <!-- 其他配置 -->
                <GridPane>
                    <Label text="HDFS 块大小:"/>
                    <ComboBox fx:id="hdfsBlockSizeCombo"/>
                    <Label text="YARN 总内存:"/>
                    <ComboBox fx:id="yarnMemoryCombo"/>
                </GridPane>
            </VBox>
        </VBox>
    </ScrollPane>

    <!-- 底部按钮 -->
    <HBox>
        <Button fx:id="previousBtn" text="上一步" onAction="#handlePrevious"/>
        <Button fx:id="cancelBtn" text="取消" onAction="#handleCancel"/>
        <Button fx:id="startDeployBtn" text="开始部署" onAction="#handleStartDeploy"/>
    </HBox>
</VBox>
```

### 2. CSS 样式增强

**新增样式类：**

```css
/* 子标题样式 */
.subsection-title {
  -fx-text-fill: #34495e;
  -fx-font-weight: bold;
  -fx-font-size: 12px;
}

/* 单选按钮样式 */
.config-radio {
  -fx-text-fill: #2c3e50;
  -fx-font-size: 12px;
}

.config-radio:selected .radio {
  -fx-background-color: #3498db;
}

/* 文件路径输入框样式 */
.file-path-field {
  -fx-background-color: #ecf0f1;
  -fx-border-color: #bdc3c7;
  -fx-border-width: 1;
  -fx-border-radius: 3;
  -fx-padding: 5 8;
  -fx-font-size: 11px;
}

.file-path-field:focused {
  -fx-border-color: #3498db;
  -fx-border-width: 1.5;
}

/* 浏览按钮样式 */
.browse-btn {
  -fx-background-color: #95a5a6;
  -fx-text-fill: white;
  -fx-font-size: 11px;
  -fx-font-weight: bold;
  -fx-cursor: hand;
}

.browse-btn:hover {
  -fx-background-color: #7f8c8d;
}

.browse-btn:disabled {
  -fx-background-color: #bdc3c7;
  -fx-opacity: 0.6;
}
```

## 错误处理

### 1. 配置验证错误

**场景：** 用户配置不完整或不正确

**处理策略：**

- 在开始部署前进行完整性验证
- 显示具体的错误提示信息
- 阻止进入部署流程

**验证项：**

```java
// 主机名验证
if (hostname == null || hostname.trim().isEmpty()) {
    throw new ValidationException("主机名不能为空");
}

// 本地文件验证
if (sourceType == SourceType.LOCAL_FILE) {
    File file = new File(localFilePath);
    if (!file.exists()) {
        throw new ValidationException("文件不存在: " + localFilePath);
    }
    if (!file.canRead()) {
        throw new ValidationException("文件不可读: " + localFilePath);
    }
}
```

### 2. SSH 连接错误

**场景：** 部署过程中 SSH 连接断开

**处理策略：**

- 自动重连（最多 3 次）
- 记录详细错误日志
- 提示用户检查网络和虚拟机状态

**错误类型：**

- `ConnectionException` - 连接失败
- `AuthenticationException` - 认证失败
- `TimeoutException` - 连接超时

### 3. 文件传输错误

**场景：** 文件上传失败

**处理策略：**

- 支持断点续传
- 验证文件完整性（MD5 校验）
- 提供重试机制

**错误处理：**

```java
try {
    uploadFile(session, localFile, remoteDir);
} catch (IOException e) {
    logger.error("文件上传失败: {}", e.getMessage());

    // 尝试重新上传
    if (retryCount < MAX_RETRY) {
        retryCount++;
        uploadFile(session, localFile, remoteDir);
    } else {
        throw new DeploymentException("文件上传失败，已达到最大重试次数");
    }
}
```

### 4. 命令执行错误

**场景：** SSH 命令执行失败

**处理策略：**

- 记录命令输出和错误信息
- 根据退出码判断失败原因
- 提供回滚机制（可选）

**错误处理：**

```java
CommandResult result = executeCommand(session, command);
if (result.getExitCode() != 0) {
    logger.error("命令执行失败: {}", command);
    logger.error("错误输出: {}", result.getError());

    throw new DeploymentException(
        "命令执行失败: " + command + "\n" +
        "错误信息: " + result.getError()
    );
}
```

### 5. 部署中断处理

**场景：** 用户取消部署或程序异常退出

**处理策略：**

- 保存部署进度状态
- 清理临时文件
- 关闭所有 SSH 连接
- 提供恢复部署选项（可选）

## 测试策略

### 1. 单元测试

**测试范围：**

- 数据模型的 getter/setter
- 配置验证逻辑
- 文件路径解析
- 命令构建逻辑

**测试工具：** JUnit 5

**示例测试：**

```java
@Test
public void testJDKConfigValidation() {
    JDKConfig config = new JDKConfig();
    config.setSourceType(SourceType.LOCAL_FILE);
    config.setLocalFilePath("/path/to/jdk.tar.gz");

    ValidationService validator = new ValidationService();
    assertDoesNotThrow(() -> validator.validateJDKConfig(config));
}

@Test
public void testInvalidFilePath() {
    JDKConfig config = new JDKConfig();
    config.setSourceType(SourceType.LOCAL_FILE);
    config.setLocalFilePath("/nonexistent/file.tar.gz");

    ValidationService validator = new ValidationService();
    assertThrows(ValidationException.class,
                () -> validator.validateJDKConfig(config));
}
```

### 2. 集成测试

**测试范围：**

- SSH 连接建立和命令执行
- 文件上传功能
- 配置文件生成
- 端到端部署流程

**测试环境：**

- 使用 Docker 容器模拟 3 台 CentOS 虚拟机
- 配置 SSH 服务
- 准备测试数据

**示例测试：**

```java
@Test
public void testFileUpload() {
    // 准备测试环境
    VMConnectionConfig config = createTestConfig();
    Session session = sshService.getSession(config);

    // 执行上传
    String localFile = "test-data/jdk-8u212.tar.gz";
    String remoteDir = "/tmp/test";

    boolean success = fileTransferService.uploadFile(
        session, localFile, remoteDir, null
    );

    assertTrue(success);

    // 验证文件存在
    boolean exists = fileTransferService.verifyRemoteFile(
        session, remoteDir + "/jdk-8u212.tar.gz"
    );

    assertTrue(exists);
}
```

### 3. 界面测试

**测试范围：**

- 控件状态切换
- 数据绑定
- 事件处理
- 界面跳转

**测试工具：** TestFX

**示例测试：**

```java
@Test
public void testJDKSourceToggle(FxRobot robot) {
    // 点击"本地上传"单选按钮
    robot.clickOn("#jdkUploadRadio");

    // 验证控件状态
    ComboBox<?> combo = robot.lookup("#jdkVersionCombo").query();
    TextField field = robot.lookup("#jdkFilePathField").query();
    Button button = robot.lookup("#jdkBrowseBtn").query();

    assertTrue(combo.isDisabled());
    assertFalse(field.isDisabled());
    assertFalse(button.isDisabled());
}
```

### 4. 性能测试

**测试指标：**

- 文件上传速度（MB/s）
- SSH 命令执行延迟
- 完整部署时间
- 内存占用

**测试方法：**

- 使用 JMH 进行基准测试
- 监控系统资源使用
- 记录关键操作耗时

### 5. 手动测试清单

**测试场景：**

- [ ] 从连接配置界面正常跳转，IP 自动填充
- [ ] 主机名显示为首字母大写
- [ ] 选择预设 JDK 版本，下拉框可用
- [ ] 选择本地上传 JDK，文件选择器打开
- [ ] 选择本地上传 Hadoop，文件选择器打开
- [ ] 未选择文件时点击部署，显示错误提示
- [ ] 开始部署后，日志显示真实 SSH 命令输出
- [ ] 部署过程中显示实时进度
- [ ] 部署完成后，虚拟机上文件和配置正确
- [ ] 取消部署，SSH 连接正确关闭

## 技术实现细节

### 1. 连接信息传递机制

**实现方式：** 通过 ConfigService 持久化 + Controller 直接传递

**流程：**

```java
// Step 1: ConnectionController保存配置
@FXML
private void handleNext() {
    List<VMConnectionConfig> configs = buildConfigList();
    configService.saveConfig(configs);  // 持久化到文件
    // 跳转到下一个界面
}

// Step 2: DeployModeController读取并传递
@FXML
private void handleNext() {
    List<VMConnectionConfig> configs = configService.loadConfig();

    FXMLLoader loader = new FXMLLoader(...);
    Parent root = loader.load();
    ClusterConfigController controller = loader.getController();

    // 传递连接信息
    controller.setVmConnectionInfo(
        configs.get(0).getIp(), configs.get(1).getIp(), configs.get(2).getIp(),
        configs.get(0).getUsername(), configs.get(1).getUsername(), configs.get(2).getUsername(),
        configs.get(0).getPassword(), configs.get(1).getPassword(), configs.get(2).getPassword()
    );
}

// Step 3: ClusterConfigController接收并存储
public void setVmConnectionInfo(...) {
    // 填充IP字段
    vm1IpField.setText(vm1Ip);
    vm2IpField.setText(vm2Ip);
    vm3IpField.setText(vm3Ip);

    // 存储到内存
    vmIps[0] = vm1Ip;
    vmIps[1] = vm2Ip;
    vmIps[2] = vm3Ip;
    vmUsernames[0] = vm1User;
    vmUsernames[1] = vm2User;
    vmUsernames[2] = vm3User;
    vmPasswords[0] = vm1Pass;
    vmPasswords[1] = vm2Pass;
    vmPasswords[2] = vm3Pass;
}
```

### 2. 文件上传实现

**使用 JSch 的 SFTP 通道：**

```java
public boolean uploadFile(Session session, String localFilePath,
                         String remoteDir, ProgressCallback callback) {
    ChannelSftp sftpChannel = null;
    try {
        // 打开SFTP通道
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        // 创建远程目录
        createRemoteDir(sftpChannel, remoteDir);

        // 上传文件
        File localFile = new File(localFilePath);
        String remoteFilePath = remoteDir + "/" + localFile.getName();

        // 设置进度监控
        SftpProgressMonitor monitor = new SftpProgressMonitor() {
            private long transferred = 0;
            private long total = localFile.length();

            @Override
            public void init(int op, String src, String dest, long max) {
                callback.onStart(src, dest, max);
            }

            @Override
            public boolean count(long count) {
                transferred += count;
                int percent = (int) ((transferred * 100) / total);
                callback.onProgress(transferred, total, percent);
                return true;
            }

            @Override
            public void end() {
                callback.onComplete();
            }
        };

        // 执行上传
        sftpChannel.put(localFilePath, remoteFilePath, monitor,
                       ChannelSftp.OVERWRITE);

        logger.info("文件上传成功: {} -> {}", localFilePath, remoteFilePath);
        return true;

    } catch (JSchException | SftpException e) {
        logger.error("文件上传失败", e);
        callback.onError(e.getMessage());
        return false;
    } finally {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
    }
}
```

### 3. SSH 命令执行与日志输出

**实时输出命令执行日志：**

```java
public CommandResult executeCommandWithLog(Session session, String command,
                                          LogCallback logCallback) {
    ChannelExec execChannel = null;
    try {
        execChannel = (ChannelExec) session.openChannel("exec");
        execChannel.setCommand(command);

        // 获取输出流
        InputStream in = execChannel.getInputStream();
        InputStream err = execChannel.getErrStream();

        execChannel.connect();

        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        // 实时读取输出
        byte[] buffer = new byte[1024];
        while (true) {
            // 读取标准输出
            while (in.available() > 0) {
                int len = in.read(buffer);
                if (len < 0) break;
                String line = new String(buffer, 0, len);
                output.append(line);
                logCallback.onLog(line);  // 实时回调
            }

            // 读取错误输出
            while (err.available() > 0) {
                int len = err.read(buffer);
                if (len < 0) break;
                String line = new String(buffer, 0, len);
                error.append(line);
                logCallback.onError(line);  // 实时回调
            }

            // 检查命令是否执行完成
            if (execChannel.isClosed()) {
                if (in.available() > 0 || err.available() > 0) {
                    continue;
                }
                break;
            }

            Thread.sleep(100);
        }

        int exitCode = execChannel.getExitStatus();

        return new CommandResult(
            command,
            exitCode,
            output.toString(),
            error.toString(),
            System.currentTimeMillis(),
            exitCode == 0
        );

    } catch (Exception e) {
        logger.error("命令执行失败", e);
        logCallback.onError("命令执行异常: " + e.getMessage());
        return CommandResult.failure(command, e.getMessage());
    } finally {
        if (execChannel != null && execChannel.isConnected()) {
            execChannel.disconnect();
        }
    }
}
```

### 4. 部署进度实时更新

**使用 JavaFX 的 Platform.runLater：**

```java
public class DeploymentService {

    public void deploy(DeploymentConfig config,
                      DeploymentProgressListener listener) {
        // 在后台线程执行部署
        Task<Void> deployTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 步骤1: 环境预处理
                updateProgress("环境预处理", 0, 10);
                configureEnvironment(config, listener);

                // 步骤2: 软件安装
                updateProgress("安装JDK", 10, 10);
                installJDK(config, listener);

                updateProgress("安装Hadoop", 20, 10);
                installHadoop(config, listener);

                // 步骤3: 配置文件生成
                updateProgress("生成配置文件", 40, 10);
                generateConfigs(config, listener);

                // 步骤4: 集群初始化
                updateProgress("初始化HDFS", 60, 10);
                initializeHDFS(config, listener);

                // 步骤5: 启动服务
                updateProgress("启动集群服务", 80, 10);
                startServices(config, listener);

                updateProgress("部署完成", 100, 10);
                return null;
            }

            private void updateProgress(String step, int progress, int total) {
                Platform.runLater(() -> {
                    listener.onStepChange(step);
                    listener.onProgressChange(progress, total);
                });
            }
        };

        Thread deployThread = new Thread(deployTask);
        deployThread.setDaemon(true);
        deployThread.start();
    }
}
```

### 5. 配置文件生成（FreeMarker）

**模板示例 (core-site.xml.ftl)：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://${nameNodeHost}:${nameNodePort}</value>
    </property>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>${hdfsTempDir}</value>
    </property>
    <property>
        <name>hadoop.http.staticuser.user</name>
        <value>${hadoopUser}</value>
    </property>
</configuration>
```

**生成代码：**

```java
public String generateCoreConfig(ClusterConfig config) {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
    cfg.setClassForTemplateLoading(this.getClass(), "/templates");

    Template template = cfg.getTemplate("core-site.xml.ftl");

    Map<String, Object> data = new HashMap<>();
    data.put("nameNodeHost", config.getNameNodeHost());
    data.put("nameNodePort", config.getNameNodePort());
    data.put("hdfsTempDir", config.getHdfsTempDir());
    data.put("hadoopUser", "hadoop");

    StringWriter writer = new StringWriter();
    template.process(data, writer);

    return writer.toString();
}
```

## 性能优化

### 1. 并行上传

**策略：** 使用线程池并行上传文件到多台虚拟机

```java
ExecutorService executor = Executors.newFixedThreadPool(3);
List<Future<UploadResult>> futures = new ArrayList<>();

for (VMConnectionConfig config : configs) {
    Future<UploadResult> future = executor.submit(() -> {
        Session session = sshService.getSession(config);
        return uploadFile(session, localFile, remoteDir, callback);
    });
    futures.add(future);
}

// 等待所有上传完成
for (Future<UploadResult> future : futures) {
    UploadResult result = future.get();
    // 处理结果
}

executor.shutdown();
```

### 2. 连接复用

**策略：** 缓存 SSH 会话，避免重复建立连接

```java
// 在SSHConnectionService中维护会话缓存
private final Map<String, Session> sessionCache = new ConcurrentHashMap<>();

public Session getSession(VMConnectionConfig config) {
    String key = config.getIp();
    Session session = sessionCache.get(key);

    if (session != null && session.isConnected()) {
        return session;  // 复用现有连接
    }

    // 创建新连接
    session = createNewSession(config);
    sessionCache.put(key, session);
    return session;
}
```

### 3. 日志异步写入

**策略：** 使用 log4j2 的异步 Appender

```xml
<Appenders>
    <Async name="AsyncFile">
        <AppenderRef ref="RollingFile"/>
    </Async>
</Appenders>
```

## 安全考虑

### 1. 密码加密存储

**策略：** 使用 AES 加密存储密码到配置文件

```java
public class PasswordEncryptor {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "your-secret-key";

    public static String encrypt(String password) {
        // AES加密实现
    }

    public static String decrypt(String encryptedPassword) {
        // AES解密实现
    }
}
```

### 2. SSH 密钥认证（可选）

**策略：** 支持使用 SSH 密钥代替密码

```java
session.setConfig("PreferredAuthentications", "publickey,password");
session.setIdentity("/path/to/private/key");
```

### 3. 输入验证

**策略：** 严格验证所有用户输入

```java
// 防止命令注入
public void validateCommand(String command) {
    if (command.contains(";") || command.contains("|") ||
        command.contains("&") || command.contains(">")) {
        throw new ValidationException("命令包含非法字符");
    }
}
```
