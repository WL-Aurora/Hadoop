# 部署模式选择界面实现任务

## 任务列表

- [x] 1. 创建数据模型和枚举类



  - 创建 DeployMode 枚举（QUICK, CUSTOM）
  - 创建 NodeRole 枚举（NAMENODE, RESOURCEMANAGER, SECONDARYNAMENODE, DATANODE）
  - 创建 DeployModeConfig 数据模型类
  - _需求: 1.1, 2.1, 3.1_




- [ ] 2. 扩展 ConfigService 支持部署模式配置

  - 添加 saveDeployModeConfig 方法
  - 添加 loadDeployModeConfig 方法


  - 添加 deployModeConfigExists 方法
  - 实现 JSON 序列化和反序列化
  - _需求: 6.1, 6.2, 6.3_

- [x] 3. 扩展 ValidationService 支持角色配置验证



  - 添加 validateRoleConfiguration 方法
  - 实现必需角色检查（NameNode, ResourceManager, SecondaryNameNode）
  - 实现 DataNode 默认分配验证
  - _需求: 4.1, 4.2, 4.3, 4.4_

- [x] 4. 设计 FXML 界面布局



  - 创建 deploy-mode.fxml 文件
  - 设计顶部标题区域
  - 设计一键部署模式选择区域（单选按钮+角色说明）
  - 设计自定义部署模式选择区域（单选按钮+角色配置表格）
  - 设计底部导航按钮区域（上一步、取消、下一步）
  - _需求: 1.1, 1.2, 2.1, 3.1_



- [ ] 5. 创建 CSS 样式文件

  - 创建 deploy-mode.css 文件
  - 定义模式选择卡片样式
  - 定义角色配置表格样式

  - 定义按钮样式
  - 确保与 connection.css 风格一致
  - _需求: 1.1, 2.5, 3.5_

- [ ] 6. 实现 DeployModeController 基础功能


  - 创建 DeployModeController 类
  - 实现 initialize 方法
  - 注入 FXML 界面元素
  - 实现配置加载逻辑
  - _需求: 1.1, 6.2_

- [ ] 7. 实现一键部署模式功能


  - 实现 handleQuickDeployMode 方法
  - 显示默认角色分配说明
  - 隐藏自定义配置区域
  - _需求: 1.3, 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 8. 实现自定义部署模式功能


  - 实现 handleCustomDeployMode 方法
  - 显示角色配置区域
  - 为每台虚拟机创建角色选择下拉菜单
  - 实现 handleRoleSelection 方法
  - 实时更新角色分配显示
  - _需求: 1.4, 3.1, 3.2, 3.3, 3.4, 3.5_


- [ ] 9. 实现角色配置验证

  - 实现 validateRoleConfiguration 方法
  - 检查必需角色是否已分配
  - 显示验证错误提示
  - 高亮显示问题区域
  - _需求: 4.1, 4.2, 4.3, 4.4_




- [ ] 10. 实现导航功能

  - 实现 handlePrevious 方法（返回虚拟机连接配置界面）
  - 实现 handleNext 方法（进入集群参数配置界面）
  - 实现 handleCancel 方法（显示确认对话框并退出）
  - 保存当前配置状态
  - _需求: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 11. 实现配置持久化

  - 实现 saveConfiguration 方法
  - 在用户点击"下一步"时保存配置
  - 在用户点击"上一步"时保留配置
  - 处理配置保存失败的情况
  - _需求: 6.1, 6.5_

- [ ] 12. 集成到主界面导航流程

  - 修改 ConnectionController 的 handleNext 方法
  - 加载 deploy-mode.fxml 界面
  - 传递虚拟机连接配置数据
  - 确保界面切换流畅
  - _需求: 1.5, 5.1_

- [ ]\* 13. 添加日志记录

  - 记录模式选择操作
  - 记录角色配置变化
  - 记录验证错误
  - 记录配置保存结果
  - _需求: 所有需求_

- [ ]\* 14. 编写单元测试
  - 测试 DeployModeConfig 数据模型
  - 测试 ValidationService 角色验证逻辑
  - 测试 ConfigService 配置读写
  - 测试 DeployModeController 核心方法
  - _需求: 所有需求_

## 实现顺序说明

1. **第 1-3 步**：创建基础数据结构和服务扩展
2. **第 4-5 步**：设计界面布局和样式
3. **第 6 步**：创建控制器基础框架
4. **第 7-8 步**：实现两种部署模式的核心功能
5. **第 9 步**：实现配置验证
6. **第 10-11 步**：实现导航和配置持久化
7. **第 12 步**：集成到主流程
8. **第 13-14 步**：完善日志和测试（可选）

## 注意事项

- 所有界面元素需要设置 focusTraversable="false"避免聚焦问题
- 确保与虚拟机连接配置界面的风格一致
- 角色配置验证需要在用户操作时实时进行
- DataNode 角色默认分配且不可取消
- 配置保存失败不应阻止用户继续操作
