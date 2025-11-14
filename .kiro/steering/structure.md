---
inclusion: always
---

# 项目结构

## 当前组织结构

```
/
├── .kiro/              # Kiro配置和steering规则
│   └── steering/       # AI助手指导文档
└── 需求分析.txt        # 需求分析文档（中文）
```

## 推荐项目结构

基于JavaFX + Maven项目的标准结构：

```
/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hads/
│   │   │       ├── Main.java                    # 应用程序入口
│   │   │       ├── controller/                  # JavaFX控制器
│   │   │       │   ├── WizardController.java    # 部署向导控制器
│   │   │       │   ├── ConnectionController.java # 远程连接配置控制器
│   │   │       │   ├── DeployModeController.java # 部署模式选择控制器
│   │   │       │   ├── ConfigController.java    # 集群参数配置控制器
│   │   │       │   └── ManagementController.java # 集群管理控制器
│   │   │       ├── service/                     # 核心服务层
│   │   │       │   ├── SSHService.java          # SSH远程连接服务（JSch）
│   │   │       │   ├── ConfigGeneratorService.java # 配置生成服务（FreeMarker）
│   │   │       │   ├── DeploymentService.java   # 部署服务
│   │   │       │   ├── ValidationService.java   # 验证服务
│   │   │       │   └── MonitorService.java      # 监控服务
│   │   │       ├── model/                       # 数据模型
│   │   │       │   ├── VMConfig.java            # 虚拟机配置
│   │   │       │   ├── ClusterConfig.java       # 集群配置
│   │   │       │   ├── DeploymentProgress.java  # 部署进度
│   │   │       │   └── NodeStatus.java          # 节点状态
│   │   │       ├── util/                        # 工具类
│   │   │       │   ├── SSHUtil.java             # SSH工具
│   │   │       │   ├── FileUtil.java            # 文件操作工具
│   │   │       │   ├── NetworkUtil.java         # 网络检测工具
│   │   │       │   └── LogUtil.java             # 日志工具
│   │   │       └── exception/                   # 自定义异常
│   │   │           ├── ConnectionException.java
│   │   │           ├── DeploymentException.java
│   │   │           └── ValidationException.java
│   │   └── resources/
│   │       ├── fxml/                            # JavaFX界面文件
│   │       │   ├── wizard.fxml
│   │       │   ├── connection.fxml
│   │       │   ├── deploy-mode.fxml
│   │       │   ├── config.fxml
│   │       │   └── management.fxml
│   │       ├── templates/                       # FreeMarker模板
│   │       │   ├── hadoop-env.sh.ftl
│   │       │   ├── core-site.xml.ftl
│   │       │   ├── hdfs-site.xml.ftl
│   │       │   ├── mapred-site.xml.ftl
│   │       │   ├── yarn-site.xml.ftl
│   │       │   ├── workers.ftl
│   │       │   ├── xsync.sh.ftl
│   │       │   ├── myhadoop.sh.ftl
│   │       │   └── jpsall.sh.ftl
│   │       ├── css/                             # 样式文件
│   │       │   └── style.css
│   │       ├── images/                          # 图标和图片资源
│   │       └── log4j2.xml                       # 日志配置
│   └── test/
│       └── java/
│           └── com/hads/
│               ├── service/                     # 服务层测试
│               └── util/                        # 工具类测试
├── docs/                                        # 文档
│   └── 需求分析.txt
├── scripts/                                     # 辅助脚本
├── pom.xml                                      # Maven配置文件
└── README.md                                    # 项目说明
```

## 代码组织约定

### 包命名规范
- 基础包：`com.hads`
- 控制器：`com.hads.controller`
- 服务层：`com.hads.service`
- 数据模型：`com.hads.model`
- 工具类：`com.hads.util`
- 异常类：`com.hads.exception`

### 文件命名规范
- JavaFX控制器：`XxxController.java`
- 服务类：`XxxService.java`
- 工具类：`XxxUtil.java`
- FXML文件：小写连字符命名，如`deploy-mode.fxml`
- 模板文件：`.ftl`后缀，如`core-site.xml.ftl`

### 资源文件路径
- 用户配置：`~/.hads/config.json`
- 日志文件：`~/.hads/logs/`
- 缓存目录：`~/.hads/cache/`

## 模块职责划分

### Controller层
- 处理用户交互
- 调用Service层执行业务逻辑
- 更新界面显示

### Service层
- 实现核心业务逻辑
- SSH远程操作
- 配置文件生成
- 部署流程控制
- 集群监控

### Model层
- 数据封装
- 配置信息存储
- 状态管理

### Util层
- 通用工具方法
- 网络检测
- 文件操作
- 日志记录
