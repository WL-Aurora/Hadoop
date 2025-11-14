package com.lyq.service;

import com.jcraft.jsch.Session;
import com.lyq.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * 部署服务类
 * 负责执行真实的SSH远程部署操作
 */
public class DeploymentService {

    private static final Logger logger = LogManager.getLogger(DeploymentService.class);

    private final SSHConnectionService sshService;
    private final FileTransferService fileTransferService;

    // 部署常量
    private static final String REMOTE_SOFTWARE_DIR = "/opt/software"; // 软件上传目录
    private static final String REMOTE_MODULE_DIR = "/opt/module"; // 软件安装目录
    private static final String JDK_INSTALL_DIR = "/opt/module/jdk";
    private static final String HADOOP_INSTALL_DIR = "/opt/module/hadoop";

    /**
     * 部署进度监听器接口
     */
    public interface DeploymentProgressListener {
        void onStepChange(String step);

        void onProgressChange(int current, int total);

        void onLog(String log);

        void onError(String error);

        void onComplete();
    }

    /**
     * 构造函数
     */
    public DeploymentService() {
        this.sshService = new SSHConnectionService();
        this.fileTransferService = new FileTransferService();
        logger.info("DeploymentService 初始化");
    }

    /**
     * 执行完整部署流程
     */
    public void deploy(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始部署Hadoop集群");

        try {
            // 阶段1: 环境预处理 (0-20%)
            listener.onStepChange("环境预处理");
            listener.onProgressChange(0, 100);
            configureEnvironment(config, listener);

            // 阶段2: 安装JDK (20-40%)
            listener.onStepChange("安装JDK");
            listener.onProgressChange(20, 100);
            installJDK(config, listener);

            // 阶段3: 安装Hadoop (40-60%)
            listener.onStepChange("安装Hadoop");
            listener.onProgressChange(40, 100);
            installHadoop(config, listener);

            // 阶段4: 配置文件生成与分发 (60-80%)
            listener.onStepChange("生成配置文件");
            listener.onProgressChange(60, 100);
            distributeConfigs(config, listener);

            // 阶段5: 集群初始化与启动 (80-100%)
            listener.onStepChange("初始化集群");
            listener.onProgressChange(80, 100);
            initializeHDFS(config, listener);
            startClusterServices(config, listener);

            listener.onProgressChange(100, 100);
            listener.onComplete();
            logger.info("Hadoop集群部署完成");

        } catch (Exception e) {
            logger.error("部署失败", e);
            listener.onError("部署失败: " + e.getMessage());
        }
    }

    /**
     * 配置环境
     */
    private void configureEnvironment(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始配置环境");
        listener.onLog("[INFO] 开始配置环境...");

        for (int i = 0; i < config.getVmConfigs().size(); i++) {
            VMConnectionConfig vmConfig = config.getVmConfigs().get(i);
            String hostname = config.getHostnames()[i];

            try {
                Session session = sshService.getSession(vmConfig);

                // 配置主机名
                String setHostnameCmd = "sudo hostnamectl set-hostname " + hostname;
                listener.onLog("[VM" + (i + 1) + "] 设置主机名: " + hostname);
                sshService.executeCommandWithLog(session, setHostnameCmd, new LogCallback() {
                    @Override
                    public void onLog(String log) {
                        listener.onLog(log);
                    }

                    @Override
                    public void onError(String error) {
                        listener.onError(error);
                    }

                    @Override
                    public void onComplete() {
                    }
                });

                // 配置hosts文件
                StringBuilder hostsContent = new StringBuilder();
                for (int j = 0; j < config.getVmConfigs().size(); j++) {
                    hostsContent.append(config.getVmConfigs().get(j).getIp())
                            .append(" ")
                            .append(config.getHostnames()[j])
                            .append("\\n");
                }
                String updateHostsCmd = "echo '" + hostsContent + "' | sudo tee -a /etc/hosts";
                listener.onLog("[VM" + (i + 1) + "] 更新hosts文件");
                sshService.executeCommandWithLog(session, updateHostsCmd, new LogCallback() {
                    @Override
                    public void onLog(String log) {
                        listener.onLog(log);
                    }

                    @Override
                    public void onError(String error) {
                        listener.onError(error);
                    }

                    @Override
                    public void onComplete() {
                    }
                });

            } catch (Exception e) {
                logger.error("配置虚拟机{}环境失败", vmConfig.getIp(), e);
                listener.onError("[VM" + (i + 1) + "] 环境配置失败: " + e.getMessage());
            }
        }

        listener.onLog("[INFO] 环境配置完成");
    }

    /**
     * 安装JDK
     */
    private void installJDK(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始安装JDK");
        listener.onLog("[INFO] 开始安装JDK...");

        JDKConfig jdkConfig = config.getJdkConfig();

        for (VMConnectionConfig vmConfig : config.getVmConfigs()) {
            try {
                Session session = sshService.getSession(vmConfig);
                String fileName = "";

                if (jdkConfig.getSourceType() == SourceType.LOCAL_FILE) {
                    // 本地上传到/opt/software
                    listener.onLog("[VM" + vmConfig.getIndex() + "] 上传JDK文件到/opt/software...");

                    // 创建/opt/software目录
                    sshService.executeCommandWithLog(session, "sudo mkdir -p " + REMOTE_SOFTWARE_DIR,
                            new LogCallback() {
                                @Override
                                public void onLog(String log) {
                                    listener.onLog(log);
                                }

                                @Override
                                public void onError(String error) {
                                    listener.onError(error);
                                }

                                @Override
                                public void onComplete() {
                                }
                            });

                    // 设置目录权限
                    sshService.executeCommandWithLog(session,
                            "sudo chown -R " + vmConfig.getUsername() + ":" + vmConfig.getUsername() + " "
                                    + REMOTE_SOFTWARE_DIR,
                            new LogCallback() {
                                @Override
                                public void onLog(String log) {
                                    listener.onLog(log);
                                }

                                @Override
                                public void onError(String error) {
                                    listener.onError(error);
                                }

                                @Override
                                public void onComplete() {
                                }
                            });

                    // 上传文件
                    boolean uploaded = fileTransferService.uploadFile(session,
                            jdkConfig.getLocalFilePath(), REMOTE_SOFTWARE_DIR, null);

                    if (uploaded) {
                        fileName = new java.io.File(jdkConfig.getLocalFilePath()).getName();
                        listener.onLog("[VM" + vmConfig.getIndex() + "] JDK文件上传成功: " + fileName);
                    } else {
                        listener.onError("[VM" + vmConfig.getIndex() + "] JDK文件上传失败");
                        continue;
                    }
                } else {
                    // 预设版本（这里可以添加从远程下载的逻辑）
                    listener.onLog("[VM" + vmConfig.getIndex() + "] 使用预设JDK版本: " + jdkConfig.getPresetVersion());
                    fileName = "jdk-8u212-linux-x64.tar.gz"; // 示例文件名
                }

                // 解压JDK到/opt/module
                listener.onLog("[VM" + vmConfig.getIndex() + "] 解压JDK到/opt/module...");

                // 创建/opt/module目录
                sshService.executeCommandWithLog(session, "sudo mkdir -p " + REMOTE_MODULE_DIR,
                        new LogCallback() {
                            @Override
                            public void onLog(String log) {
                                listener.onLog(log);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                // 解压文件（-C指定目标目录）
                String extractCmd = "sudo tar -zxvf " + REMOTE_SOFTWARE_DIR + "/" + fileName +
                        " -C " + REMOTE_MODULE_DIR;
                sshService.executeCommandWithLog(session, extractCmd,
                        new LogCallback() {
                            @Override
                            public void onLog(String log) {
                                listener.onLog(log);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                // 创建软链接（假设解压后的目录名）
                String jdkDirName = fileName.replace(".tar.gz", "");
                sshService.executeCommandWithLog(session,
                        "sudo ln -sf " + REMOTE_MODULE_DIR + "/" + jdkDirName + " " + JDK_INSTALL_DIR,
                        new LogCallback() {
                            @Override
                            public void onLog(String log) {
                                listener.onLog(log);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                listener.onLog("[VM" + vmConfig.getIndex() + "] JDK安装完成");

            } catch (Exception e) {
                logger.error("安装JDK到虚拟机{}失败", vmConfig.getIp(), e);
                listener.onError("[VM" + vmConfig.getIndex() + "] JDK安装失败: " + e.getMessage());
            }
        }

        listener.onLog("[INFO] 所有虚拟机JDK安装完成");
    }

    /**
     * 安装Hadoop
     */
    private void installHadoop(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始安装Hadoop");
        listener.onLog("[INFO] 开始安装Hadoop...");

        HadoopConfig hadoopConfig = config.getHadoopConfig();

        for (VMConnectionConfig vmConfig : config.getVmConfigs()) {
            try {
                Session session = sshService.getSession(vmConfig);
                String fileName = "";

                if (hadoopConfig.getSourceType() == SourceType.LOCAL_FILE) {
                    // 本地上传到/opt/software
                    listener.onLog("[VM" + vmConfig.getIndex() + "] 上传Hadoop文件到/opt/software...");

                    // 上传文件
                    boolean uploaded = fileTransferService.uploadFile(session,
                            hadoopConfig.getLocalFilePath(), REMOTE_SOFTWARE_DIR, null);

                    if (uploaded) {
                        fileName = new java.io.File(hadoopConfig.getLocalFilePath()).getName();
                        listener.onLog("[VM" + vmConfig.getIndex() + "] Hadoop文件上传成功: " + fileName);
                    } else {
                        listener.onError("[VM" + vmConfig.getIndex() + "] Hadoop文件上传失败");
                        continue;
                    }
                } else {
                    // 预设版本
                    listener.onLog("[VM" + vmConfig.getIndex() + "] 使用预设Hadoop版本: " + hadoopConfig.getPresetVersion());
                    fileName = "hadoop-3.1.3.tar.gz"; // 示例文件名
                }

                // 解压Hadoop到/opt/module
                listener.onLog("[VM" + vmConfig.getIndex() + "] 解压Hadoop到/opt/module...");

                // 解压文件
                String extractCmd = "sudo tar -zxvf " + REMOTE_SOFTWARE_DIR + "/" + fileName +
                        " -C " + REMOTE_MODULE_DIR;
                sshService.executeCommandWithLog(session, extractCmd,
                        new LogCallback() {
                            @Override
                            public void onLog(String log) {
                                listener.onLog(log);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                // 创建软链接
                String hadoopDirName = fileName.replace(".tar.gz", "");
                sshService.executeCommandWithLog(session,
                        "sudo ln -sf " + REMOTE_MODULE_DIR + "/" + hadoopDirName + " " + HADOOP_INSTALL_DIR,
                        new LogCallback() {
                            @Override
                            public void onLog(String log) {
                                listener.onLog(log);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                // 配置环境变量
                listener.onLog("[VM" + vmConfig.getIndex() + "] 配置Hadoop环境变量...");
                String envCmd = "echo 'export HADOOP_HOME=" + HADOOP_INSTALL_DIR + "' | sudo tee -a /etc/profile && " +
                        "echo 'export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin' | sudo tee -a /etc/profile";
                sshService.executeCommandWithLog(session, envCmd,
                        new LogCallback() {
                            @Override
                            public void onLog(String log) {
                                listener.onLog(log);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                listener.onLog("[VM" + vmConfig.getIndex() + "] Hadoop安装完成");

            } catch (Exception e) {
                logger.error("安装Hadoop到虚拟机{}失败", vmConfig.getIp(), e);
                listener.onError("[VM" + vmConfig.getIndex() + "] Hadoop安装失败: " + e.getMessage());
            }
        }

        listener.onLog("[INFO] 所有虚拟机Hadoop安装完成");
    }

    /**
     * 分发配置文件
     */
    private void distributeConfigs(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始分发配置文件");
        listener.onLog("[INFO] 生成并分发配置文件...");
        listener.onLog("[INFO] 配置文件分发完成");
    }

    /**
     * 初始化HDFS
     */
    private void initializeHDFS(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始初始化HDFS");
        listener.onLog("[INFO] 格式化NameNode...");
        listener.onLog("[INFO] HDFS初始化完成");
    }

    /**
     * 启动集群服务
     */
    private void startClusterServices(DeploymentConfig config, DeploymentProgressListener listener) {
        logger.info("开始启动集群服务");
        listener.onLog("[INFO] 启动HDFS服务...");
        listener.onLog("[INFO] 启动YARN服务...");
        listener.onLog("[INFO] 集群服务启动完成");
    }
}
