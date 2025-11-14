package com.lyq.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.lyq.model.UploadResult;
import com.lyq.model.VMConnectionConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 文件传输服务类
 * 负责处理本地文件上传到虚拟机的操作
 */
public class FileTransferService {

    private static final Logger logger = LogManager.getLogger(FileTransferService.class);

    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onStart(String source, String dest, long totalSize);

        void onProgress(long transferred, long total, int percent);

        void onComplete();

        void onError(String errorMessage);
    }

    /**
     * 构造函数
     */
    public FileTransferService() {
        logger.info("FileTransferService 初始化");
    }

    /**
     * 上传文件到单台虚拟机
     */
    public boolean uploadFile(Session session, String localFilePath, 
                             String remoteDir, ProgressCallback progressCallback) {
        logger.info("开始上传文件: {} -> {}", localFilePath, remoteDir);
        
        ChannelSftp sftpChannel = null;
        long startTime = System.currentTimeMillis();
        
        try {
            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                String error = "本地文件不存在: " + localFilePath;
                logger.error(error);
                if (progressCallback != null) {
                    progressCallback.onError(error);
                }
                return false;
            }
            
            if (!localFile.canRead()) {
                String error = "本地文件不可读: " + localFilePath;
                logger.error(error);
                if (progressCallback != null) {
                    progressCallback.onError(error);
                }
                return false;
            }
            
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            logger.debug("SFTP通道已连接");
            
            createRemoteDir(sftpChannel, remoteDir);
            
            String remoteFilePath = remoteDir + "/" + localFile.getName();
            
            SftpProgressMonitor monitor = null;
            if (progressCallback != null) {
                final long totalSize = localFile.length();
                monitor = new SftpProgressMonitor() {
                    private long transferred = 0;
                    
                    @Override
                    public void init(int op, String src, String dest, long max) {
                        progressCallback.onStart(src, dest, max);
                        logger.debug("开始传输: {} -> {}, 大小: {} bytes", src, dest, max);
                    }
                    
                    @Override
                    public boolean count(long count) {
                        transferred += count;
                        int percent = (int) ((transferred * 100) / totalSize);
                        progressCallback.onProgress(transferred, totalSize, percent);
                        return true;
                    }
                    
                    @Override
                    public void end() {
                        progressCallback.onComplete();
                        logger.debug("传输完成");
                    }
                };
            }
            
            if (monitor != null) {
                sftpChannel.put(localFilePath, remoteFilePath, monitor, ChannelSftp.OVERWRITE);
            } else {
                sftpChannel.put(localFilePath, remoteFilePath, ChannelSftp.OVERWRITE);
            }
            
            long uploadTime = System.currentTimeMillis() - startTime;
            logger.info("文件上传成功: {} -> {}, 耗时: {}ms", localFilePath, remoteFilePath, uploadTime);
            
            return true;
            
        } catch (JSchException e) {
            String error = "SFTP通道创建失败: " + e.getMessage();
            logger.error(error, e);
            if (progressCallback != null) {
                progressCallback.onError(error);
            }
            return false;
            
        } catch (SftpException e) {
            String error = "文件上传失败: " + e.getMessage();
            logger.error(error, e);
            if (progressCallback != null) {
                progressCallback.onError(error);
            }
            return false;
            
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
                logger.debug("SFTP通道已关闭");
            }
        }
    }

/**
     * 批量上传文件到多台虚拟机
     */
    public List<UploadResult> uploadToAllVMs(List<VMConnectionConfig> configs,
                                            String localFilePath,
                                            String remoteDir,
                                            ProgressCallback progressCallback) {
        logger.info("开始批量上传文件到{}台虚拟机", configs.size());
        
        List<UploadResult> results = new ArrayList<>();
        File localFile = new File(localFilePath);
        long fileSize = localFile.length();
        
        ExecutorService executor = Executors.newFixedThreadPool(configs.size());
        List<Future<UploadResult>> futures = new ArrayList<>();
        
        SSHConnectionService sshService = new SSHConnectionService();
        
        for (VMConnectionConfig config : configs) {
            Future<UploadResult> future = executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                
                try {
                    Session session = sshService.getSession(config);
                    boolean success = uploadFile(session, localFilePath, remoteDir, null);
                    
                    long uploadTime = System.currentTimeMillis() - startTime;
                    String remoteFilePath = remoteDir + "/" + localFile.getName();
                    
                    if (success) {
                        return UploadResult.success(config.getIndex(), config.getIp(),
                                localFilePath, remoteFilePath, fileSize, uploadTime);
                    } else {
                        return UploadResult.failure(config.getIndex(), config.getIp(),
                                localFilePath, "上传失败");
                    }
                    
                } catch (Exception e) {
                    logger.error("上传到虚拟机{}失败", config.getIp(), e);
                    return UploadResult.failure(config.getIndex(), config.getIp(),
                            localFilePath, e.getMessage());
                }
            });
            
            futures.add(future);
        }
        
        for (Future<UploadResult> future : futures) {
            try {
                UploadResult result = future.get();
                results.add(result);
                
                if (result.isSuccess()) {
                    logger.info("虚拟机{}上传成功，耗时: {}ms", 
                               result.getVmIp(), result.getUploadTime());
                } else {
                    logger.error("虚拟机{}上传失败: {}", 
                                result.getVmIp(), result.getErrorMessage());
                }
                
            } catch (Exception e) {
                logger.error("获取上传结果失败", e);
            }
        }
        
        executor.shutdown();
        
        long successCount = results.stream().filter(UploadResult::isSuccess).count();
        logger.info("批量上传完成，成功: {}/{}", successCount, configs.size());
        
        return results;
    }
    
    /**
     * 验证远程文件是否存在
     */
    public boolean verifyRemoteFile(Session session, String remoteFilePath) {
        logger.debug("验证远程文件是否存在: {}", remoteFilePath);
        
        ChannelSftp sftpChannel = null;
        
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            sftpChannel.lstat(remoteFilePath);
            
            logger.debug("远程文件存在: {}", remoteFilePath);
            return true;
            
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                logger.debug("远程文件不存在: {}", remoteFilePath);
            } else {
                logger.error("验证远程文件失败: {}", remoteFilePath, e);
            }
            return false;
            
        } catch (JSchException e) {
            logger.error("SFTP通道创建失败", e);
            return false;
            
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
        }
    }
    
    /**
     * 创建远程目录（支持递归创建）
     */
    private void createRemoteDir(ChannelSftp sftpChannel, String remoteDir) 
            throws SftpException {
        logger.debug("创建远程目录: {}", remoteDir);
        
        try {
            sftpChannel.cd(remoteDir);
            logger.debug("远程目录已存在: {}", remoteDir);
            return;
        } catch (SftpException e) {
            // 目录不存在，需要创建
        }
        
        String[] dirs = remoteDir.split("/");
        StringBuilder currentPath = new StringBuilder();
        
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            
            currentPath.append("/").append(dir);
            String path = currentPath.toString();
            
            try {
                sftpChannel.cd(path);
            } catch (SftpException e) {
                try {
                    sftpChannel.mkdir(path);
                    logger.debug("创建目录: {}", path);
                } catch (SftpException ex) {
                    logger.error("创建目录失败: {}", path, ex);
                    throw ex;
                }
            }
        }
        
        logger.info("远程目录创建成功: {}", remoteDir);
    }
}
