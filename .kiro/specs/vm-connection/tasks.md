# 虚拟机远程连接功能实现任务列表

- [x] 1. 配置项目依赖和基础结构

  - 在 pom.xml 中添加 JSch、JavaFX、Gson、Log4j2 依赖
  - 创建 com.lyq 包下的子包结构（controller、service、model、util、exception）
  - 配置 log4j2.xml 日志配置文件
  - _需求: 需求 6（SSH 会话管理）、需求 8（安全性保障）_

- [x] 2. 实现数据模型层

  - [x] 2.1 创建 ConnectionStatus 枚举类

    - 定义 8 种连接状态（NOT_TESTED、TESTING、SUCCESS、NETWORK_UNREACHABLE、SSH_SERVICE_DOWN、AUTH_FAILED、TIMEOUT、UNKNOWN_ERROR）
    - 为每种状态添加中文描述
    - _需求: 需求 3（SSH 连接测试）、需求 4（连接失败诊断）_

  - [x] 2.2 创建 VMConnectionConfig 模型类

    - 定义虚拟机连接配置属性（index、ip、hostname、username、password、sshPort、timeout）
    - 实现构造函数、getter/setter 方法
    - 重写 toString 方法，确保密码脱敏显示
    - _需求: 需求 1（虚拟机连接信息输入）、需求 5（连接配置持久化）_

  - [x] 2.3 创建 ConnectionResult 模型类

    - 定义连接测试结果属性（vmIndex、vmIp、status、message、errorDetail、responseTime、testTime）
    - 实现 success 和 failure 工厂方法
    - _需求: 需求 3（SSH 连接测试）、需求 4（连接失败诊断）_

- [x] 3. 实现工具类层

  - [x] 3.1 创建 NetworkUtil 网络工具类

    - 实现 isReachable 方法检查 IP 可达性
    - 实现 isPortOpen 方法检查 SSH 端口（22）是否开放
    - 实现 isValidIpAddress 方法验证 IP 格式
    - _需求: 需求 2（IP 地址格式验证）、需求 3（SSH 连接测试）_

  - [x] 3.2 创建 EncryptionUtil 加密工具类

    - 实现 AES-256 加密算法
    - 实现 encrypt 方法加密密码
    - 实现 decrypt 方法解密密码
    - 实现密钥生成和存储逻辑（存储在~/.hads/keys/目录）
    - _需求: 需求 5（连接配置持久化）、需求 8（安全性保障）_

  - [x] 3.3 创建 SSHUtil SSH 工具类

    - 实现 createSession 方法创建 JSch 会话
    - 实现 executeCommand 方法执行远程命令
    - 实现 isSessionConnected 方法检查会话状态
    - 实现 closeSession 方法安全关闭会话
    - 配置 JSch 属性（禁用 StrictHostKeyChecking 等）
    - _需求: 需求 3（SSH 连接测试）、需求 6（SSH 会话管理）_

- [x] 4. 实现异常处理类

  - [x] 4.1 创建 ConnectionException 连接异常类

    - 定义异常属性（status、vmIp）
    - 实现带原因的构造函数
    - _需求: 需求 4（连接失败诊断）_

  - [x] 4.2 创建 ValidationException 验证异常类

    - 定义异常属性（fieldName、invalidValue）
    - 实现构造函数
    - _需求: 需求 2（IP 地址格式验证）_

- [x] 5. 实现服务层

  - [x] 5.1 创建 ValidationService 验证服务类

    - 实现 validateIpAddress 方法，使用正则表达式验证 IP 格式
    - 实现 validateUsername 方法验证用户名
    - 实现 validateConfig 方法验证完整配置
    - 定义 IP 地址正则表达式常量
    - _需求: 需求 2（IP 地址格式验证）_

  - [x] 5.2 创建 ConfigService 配置管理服务类

    - 实现 saveConfig 方法，将配置序列化为 JSON 并保存到~/.hads/config.json
    - 实现 loadConfig 方法，从配置文件加载并解析 JSON
    - 实现 configExists 方法检查配置文件是否存在
    - 实现 createDefaultConfig 方法创建默认配置
    - 集成 EncryptionUtil 加密/解密密码
    - 处理文件不存在或损坏的异常情况
    - _需求: 需求 5（连接配置持久化）、需求 8（安全性保障）_

  - [x] 5.3 创建 SSHConnectionService SSH 连接服务类

    - 实现 testConnection 方法测试单个虚拟机连接
    - 实现 testAllConnections 方法批量测试所有虚拟机
    - 实现 getSession 方法获取或创建 SSH 会话
    - 实现 closeSession 和 closeAllSessions 方法关闭会话
    - 实现 reconnect 方法支持自动重连（最多 3 次）
    - 维护 sessionCache 缓存已建立的 SSH 会话
    - 集成 NetworkUtil 进行网络检测
    - 集成 SSHUtil 进行 SSH 操作
    - 根据不同异常类型返回对应的 ConnectionStatus
    - _需求: 需求 3（SSH 连接测试）、需求 4（连接失败诊断）、需求 6（SSH 会话管理）_

- [x] 6. 创建 JavaFX 界面

  - [x] 6.1 创建 connection.fxml 界面文件

    - 设计主布局（VBox 容器）
    - 添加 3 个虚拟机 IP 输入框（TextField: vm1IpField、vm2IpField、vm3IpField）
    - 添加用户名输入框（TextField: usernameField）
    - 添加密码输入框（PasswordField: passwordField）
    - 添加密码显示切换按钮（Button: passwordToggleBtn）
    - 添加 3 个连接状态标签（Label: vm1StatusLabel、vm2StatusLabel、vm3StatusLabel）
    - 添加 3 个错误信息标签（Label: vm1ErrorLabel、vm2ErrorLabel、vm3ErrorLabel）
    - 添加测试连接按钮（Button: testConnectionBtn）
    - 添加下一步按钮（Button: nextBtn，初始禁用）
    - 添加加载动画（ProgressIndicator: loadingIndicator）
    - 配置样式和布局（间距、对齐、颜色）
    - _需求: 需求 1（虚拟机连接信息输入）、需求 7（界面交互反馈）_

  - [x] 6.2 创建 CSS 样式文件

    - 定义输入框样式（正常、聚焦、错误状态）
    - 定义按钮样式
    - 定义状态标签样式（成功绿色、失败红色）
    - 定义错误提示样式
    - _需求: 需求 7（界面交互反馈）_

- [x] 7. 实现界面控制器

  - [x] 7.1 创建 ConnectionController 控制器类

    - 使用@FXML 注解注入界面元素
    - 实现 initialize 方法初始化界面
    - 在 initialize 中调用 loadSavedConfig 加载保存的配置
    - _需求: 需求 5（连接配置持久化）_

  - [x] 7.2 实现 IP 地址实时验证

    - 实现 handleIpChange 方法监听 IP 输入变化
    - 调用 ValidationService 验证 IP 格式
    - 根据验证结果更新输入框样式和错误提示
    - _需求: 需求 2（IP 地址格式验证）、需求 7（界面交互反馈）_

  - [x] 7.3 实现密码显示切换功能

    - 实现 handlePasswordToggle 方法
    - 在 PasswordField 和 TextField 之间切换
    - 更新按钮图标或文本
    - _需求: 需求 1（虚拟机连接信息输入）_

  - [x] 7.4 实现连接测试功能

    - 实现 handleTestConnection 方法
    - 禁用测试按钮，显示加载动画
    - 使用 JavaFX Task 在后台线程执行连接测试
    - 调用 SSHConnectionService.testAllConnections 批量测试
    - 实现 updateConnectionStatus 方法更新每台虚拟机的连接状态显示
    - 测试完成后启用按钮，隐藏加载动画
    - 如果所有连接成功，启用"下一步"按钮并保存配置
    - _需求: 需求 3（SSH 连接测试）、需求 4（连接失败诊断）、需求 7（界面交互反馈）_

  - [x] 7.5 实现下一步导航功能

    - 实现 handleNext 方法
    - 跳转到下一个部署步骤界面（预留接口）
    - _需求: 需求 7（界面交互反馈）_

  - [x] 7.6 实现配置加载功能

    - 实现 loadSavedConfig 方法
    - 调用 ConfigService.loadConfig 加载配置
    - 将配置数据填充到界面输入框
    - 处理配置不存在或损坏的情况
    - _需求: 需求 5（连接配置持久化）_

- [x] 8. 创建应用程序入口

  - [x] 8.1 创建 Main 主类

    - 继承 JavaFX Application 类
    - 实现 start 方法加载 connection.fxml
    - 配置主窗口（标题、大小、图标）
    - 实现 stop 方法，在应用关闭时清理资源（关闭所有 SSH 会话）
    - _需求: 需求 6（SSH 会话管理）_

- [x] 9. 集成测试和优化

  - [x] 9.1 测试完整连接流程

    - 测试正常连接场景（3 台虚拟机全部成功）
    - 测试网络异常场景（IP 不可达）
    - 测试 SSH 服务异常场景（端口未开放）
    - 测试认证失败场景（错误的用户名或密码）
    - 测试连接超时场景
    - _需求: 需求 3（SSH 连接测试）、需求 4（连接失败诊断）_

  - [x] 9.2 测试配置持久化

    - 测试保存配置功能
    - 测试加载配置功能
    - 验证密码加密存储
    - 测试配置文件损坏的处理
    - _需求: 需求 5（连接配置持久化）、需求 8（安全性保障）_

  - [x] 9.3 测试界面交互

    - 验证输入框实时验证反馈
    - 验证按钮状态正确切换
    - 验证加载动画显示
    - 验证错误提示清晰可见
    - 测量操作响应时间（应 ≤1 秒）
    - _需求: 需求 7（界面交互反馈）_

  - [x] 9.4 性能优化

    - 确认连接测试使用异步执行（JavaFX Task）
    - 确认 3 台虚拟机并行测试
    - 验证 SSH 会话正确缓存和复用
    - 验证超时时间设置为 30 秒
    - _需求: 需求 3（SSH 连接测试）、需求 6（SSH 会话管理）_

  - [x] 9.5 安全性验证

    - 验证密码在内存中加密存储
    - 验证日志中密码脱敏
    - 验证配置文件中密码加密
    - 验证应用退出时清除敏感信息
    - _需求: 需求 8（安全性保障）_

- [x] 10. 编写文档和注释

  - 为所有公共类和方法添加 JavaDoc 注释
  - 编写 README.md 说明虚拟机连接功能的使用方法
  - 记录已知问题和限制
  - _需求: 所有需求_
