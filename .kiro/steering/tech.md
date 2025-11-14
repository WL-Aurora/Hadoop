---
inclusion: always
---

# 技术栈

## 前端技术
- **JavaFX 17+**：图形界面开发框架
- **Scene Builder**：界面设计工具
- **设计模式**：MVC模式分离视图与业务逻辑

## 核心依赖库
- **JSch**：SSH远程连接与命令执行、文件传输
- **FreeMarker**：配置文件模板引擎，动态生成Hadoop配置
- **log4j2**：日志管理（滚动日志，单文件最大100MB，保留10个文件）
- **JSON**：配置文件存储格式

## 目标部署环境
- **虚拟机操作系统**：CentOS 7 64位
- **VMware版本**：VMware Workstation Pro 17.x
- **Hadoop版本**：3.1.3（默认），支持2.7.7、3.3.4
- **JDK版本**：1.8.0_212（默认），支持1.8.x系列
- **浏览器**：Chrome 80+、Firefox 75+、Edge 80+（WebUI访问）

## 宿主机兼容性
- **Windows**：Windows 10/11（64位）
- **Linux**：CentOS 7（64位）

## 项目结构约定
- **配置文件路径**：`~/.hads/config.json`
- **日志文件路径**：`~/.hads/logs/`
- **缓存路径**：`~/.hads/cache/`（安装包、离线资源）

## 构建与运行
```bash
# 编译项目（Maven）
mvn clean compile

# 打包
mvn package

# 运行
java -jar target/hads.jar
```

## 关键技术点
- **远程执行**：通过JSch执行SSH命令，实现免密登录配置、软件安装、服务启动
- **文件分发**：xsync脚本实现配置文件和软件包的集群同步
- **配置生成**：FreeMarker模板动态生成hadoop-env.sh、core-site.xml、hdfs-site.xml等配置
- **进度管理**：观察者模式实时更新部署进度
- **异常处理**：自定义异常类，分类处理连接异常、执行异常，支持断点续传
