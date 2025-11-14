# Hadoop 分布式集群一键部署系统（HADS）

## 项目简介

Hadoop 分布式集群一键部署系统（Hadoop Auto Deployment System，简称 HADS）是一个基于 JavaFX 的图形化工具，旨在简化 Hadoop 分布式集群的部署过程。通过友好的向导式界面，用户可以在 30 分钟内完成 Hadoop 集群的自动化部署。

## 核心特性

- 🎯 **图形化向导**：替代复杂的命令行操作，降低技术门槛
- ⚡ **快速部署**：30 分钟内完成集群部署，效率提升 10 倍
- 🔧 **标准化配置**：固化最佳实践，避免人工配置差异
- ✅ **自动验证**：自动执行服务状态检查，确保部署即可用
- 🔄 **灵活模式**：支持一键部署和自定义部署两种模式

## 功能模块

### 已完成功能

#### 1. 虚拟机远程连接配置 ✅

- SSH 连接配置和测试
- 批量连接测试（3 台虚拟机）
- 智能故障诊断（网络、SSH 服务、认证等）
- 配置持久化（密码加密存储）
- 实时输入验证
- SSH 会话管理和自动重连

### 开发中功能

#### 2. 部署模式选择 🚧

- 一键部署模式（自动分配角色）
- 自定义部署模式（用户配置角色）

#### 3. 集群参数配置 🚧

- 网络配置
- 系统参数配置
- Hadoop 组件配置

#### 4. 自动化部署 🚧

- 环境预处理
- 组件安装
- 配置文件生成与分发
- 集群初始化

#### 5. 集群管理 🚧

- 实时监控节点状态
- 服务管理（启动/停止/重启）
- WebUI 快捷访问

#### 6. 部署验证 🚧

- 服务状态验证
- 验收报告生成

## 技术栈

### 前端技术

- **JavaFX 17+**：图形界面开发框架
- **Scene Builder**：界面设计工具
- **MVC 模式**：分离视图与业务逻辑

### 核心依赖

- **JSch 0.1.55**：SSH 远程连接与命令执行
- **Gson 2.10.1**：JSON 配置文件处理
- **Log4j2 2.20.0**：日志管理
- **Hadoop Client 3.1.3**：Hadoop 客户端库

### 目标环境

- **虚拟机操作系统**：CentOS 7 64 位
- **VMware 版本**：VMware Workstation Pro 17.x
- **Hadoop 版本**：3.1.3（默认）
- **JDK 版本**：1.8.0_212（默认）

## 系统要求

### 宿主机环境

- **操作系统**：Windows 10/11（64 位）或 CentOS 7（64 位）
- **JDK**：JDK 1.8 或更高版本
- **JavaFX**：JavaFX 17+
- **Maven**：Maven 3.6+
- **内存**：建议 8GB 以上
- **磁盘空间**：建议 50GB 以上

### 虚拟机环境

- **操作系统**：CentOS 7 64 位
- **虚拟机数量**：3 台
- **每台配置**：
  - CPU：2 核心
  - 内存：4GB
  - 磁盘：50GB
- **网络**：静态 IP 地址，与宿主机互通
- **SSH 服务**：已安装并启动

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/Hadoop.git
cd Hadoop
```

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 打包项目

```bash
mvn package
```

### 4. 运行应用

```bash
# 方式1：使用 Maven 运行
mvn javafx:run

# 方式2：直接运行 JAR 包
java -jar target/Hadoop-1.0-SNAPSHOT.jar
```

### 5. 配置虚拟机连接

1. 启动应用程序
2. 输入 3 台虚拟机的 IP 地址
3. 输入 SSH 登录凭证（用户名和密码）
4. 点击"测试连接"按钮
5. 等待连接测试完成
6. 连接成功后点击"下一步"

详细步骤请参考：[虚拟机连接功能使用说明](docs/虚拟机连接功能使用说明.md)

## 项目结构

```
Hadoop/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/lyq/
│   │   │       ├── Main.java                    # 应用程序入口
│   │   │       ├── controller/                  # JavaFX 控制器
│   │   │       │   └── ConnectionController.java
│   │   │       ├── service/                     # 核心服务层
│   │   │       │   ├── SSHConnectionService.java
│   │   │       │   ├── ConfigService.java
│   │   │       │   └── ValidationService.java
│   │   │       ├── model/                       # 数据模型
│   │   │       │   ├── VMConnectionConfig.java
│   │   │       │   ├── ConnectionResult.java
│   │   │       │   └── ConnectionStatus.java
│   │   │       ├── util/                        # 工具类
│   │   │       │   ├── SSHUtil.java
│   │   │       │   ├── NetworkUtil.java
│   │   │       │   └── EncryptionUtil.java
│   │   │       └── exception/                   # 自定义异常
│   │   │           ├── ConnectionException.java
│   │   │           └── ValidationException.java
│   │   └── resources/
│   │       ├── fxml/                            # JavaFX 界面文件
│   │       │   └── connection.fxml
│   │       ├── css/                             # 样式文件
│   │       │   └── connection.css
│   │       └── log4j2.xml                       # 日志配置
│   └── test/
│       └── java/                                # 测试代码
├── docs/                                        # 文档
│   ├── 虚拟机连接功能使用说明.md
│   └── 测试验证清单.md
├── .kiro/                                       # Kiro 配置
│   ├── specs/                                   # 功能规格说明
│   └── steering/                                # AI 助手指导文档
├── pom.xml                                      # Maven 配置
└── README.md                                    # 项目说明
```

## 配置文件

### 用户配置目录

- **路径**：`~/.hads/`
- **配置文件**：`~/.hads/config.json`
- **日志目录**：`~/.hads/logs/`
- **密钥目录**：`~/.hads/keys/`
- **缓存目录**：`~/.hads/cache/`

### 日志配置

- **日志级别**：INFO（默认）
- **滚动策略**：单文件最大 100MB，保留 10 个文件
- **日志格式**：`[时间] [级别] [类名] - 消息`

## 开发指南

### 代码规范

- **包命名**：`com.lyq.*`
- **类命名**：大驼峰命名法（PascalCase）
- **方法命名**：小驼峰命名法（camelCase）
- **注释**：所有公共类和方法必须有 JavaDoc 注释
- **语言**：代码注释和文档使用中文

### 设计模式

- **MVC 模式**：分离视图、控制器和模型
- **服务层模式**：封装业务逻辑
- **工厂模式**：创建对象实例
- **观察者模式**：实时更新部署进度

### 测试规范

- **单元测试**：使用 JUnit 4
- **测试覆盖率**：核心功能 > 80%
- **集成测试**：测试完整业务流程

### 提交规范

- **提交信息格式**：`[类型] 简短描述`
- **类型**：
  - `feat`：新功能
  - `fix`：Bug 修复
  - `docs`：文档更新
  - `style`：代码格式调整
  - `refactor`：代码重构
  - `test`：测试相关
  - `chore`：构建/工具相关

## 常见问题

### Q: 应用程序无法启动？

A: 请检查 JDK 版本（需要 1.8+）和 JavaFX 是否正确安装。

### Q: 连接测试失败？

A: 请检查：

1. 虚拟机是否启动
2. IP 地址是否正确
3. SSH 服务是否运行
4. 网络是否互通
5. 用户名和密码是否正确

详细排查步骤请参考：[虚拟机连接功能使用说明](docs/虚拟机连接功能使用说明.md)

### Q: 配置文件在哪里？

A: 配置文件保存在 `~/.hads/config.json`

### Q: 如何查看日志？

A: 日志文件保存在 `~/.hads/logs/` 目录

## 贡献指南

欢迎贡献代码、报告问题或提出建议！

### 贡献流程

1. Fork 本项目
2. 创建特性分支（`git checkout -b feature/AmazingFeature`）
3. 提交更改（`git commit -m '[feat] Add some AmazingFeature'`）
4. 推送到分支（`git push origin feature/AmazingFeature`）
5. 提交 Pull Request

### 报告问题

- 使用 GitHub Issues 报告问题
- 提供详细的问题描述和复现步骤
- 附上相关日志和截图

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- **项目主页**：https://github.com/your-repo/Hadoop
- **问题反馈**：https://github.com/your-repo/Hadoop/issues
- **邮箱**：your-email@example.com

## 致谢

感谢所有为本项目做出贡献的开发者！

---

**版本**：1.0.0  
**更新日期**：2024-01-01  
**状态**：开发中
