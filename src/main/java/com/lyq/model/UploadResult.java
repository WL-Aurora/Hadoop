package com.lyq.model;

/**
 * 文件上传结果模型
 * 封装文件上传操作的结果信息
 */
public class UploadResult {
    /**
     * 虚拟机编号
     */
    private int vmIndex;

    /**
     * 虚拟机IP地址
     */
    private String vmIp;

    /**
     * 上传是否成功
     */
    private boolean success;

    /**
     * 本地文件路径
     */
    private String localFilePath;

    /**
     * 远程文件路径
     */
    private String remoteFilePath;

    /**
     * 文件大小（字节）
     */
    private long fileSize;

    /**
     * 上传耗时（毫秒）
     */
    private long uploadTime;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 默认构造函数
     */
    public UploadResult() {
    }

    /**
     * 完整构造函数
     */
    public UploadResult(int vmIndex, String vmIp, boolean success,
            String localFilePath, String remoteFilePath,
            long fileSize, long uploadTime, String errorMessage) {
        this.vmIndex = vmIndex;
        this.vmIp = vmIp;
        this.success = success;
        this.localFilePath = localFilePath;
        this.remoteFilePath = remoteFilePath;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
        this.errorMessage = errorMessage;
    }

    /**
     * 创建成功的上传结果
     */
    public static UploadResult success(int vmIndex, String vmIp,
            String localFilePath, String remoteFilePath,
            long fileSize, long uploadTime) {
        return new UploadResult(vmIndex, vmIp, true, localFilePath,
                remoteFilePath, fileSize, uploadTime, null);
    }

    /**
     * 创建失败的上传结果
     */
    public static UploadResult failure(int vmIndex, String vmIp,
            String localFilePath, String errorMessage) {
        return new UploadResult(vmIndex, vmIp, false, localFilePath,
                null, 0, 0, errorMessage);
    }

    // Getter和Setter方法

    public int getVmIndex() {
        return vmIndex;
    }

    public void setVmIndex(int vmIndex) {
        this.vmIndex = vmIndex;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 重写toString方法
     * 
     * @return 结果信息的字符串表示
     */
    @Override
    public String toString() {
        return "UploadResult{" +
                "vmIndex=" + vmIndex +
                ", vmIp='" + vmIp + '\'' +
                ", success=" + success +
                ", localFilePath='" + localFilePath + '\'' +
                ", remoteFilePath='" + remoteFilePath + '\'' +
                ", fileSize=" + fileSize +
                ", uploadTime=" + uploadTime +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
