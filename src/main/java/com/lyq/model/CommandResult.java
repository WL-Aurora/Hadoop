package com.lyq.model;

/**
 * 命令执行结果模型
 * 封装SSH命令执行的结果信息
 */
public class CommandResult {
    /**
     * 执行的命令
     */
    private String command;

    /**
     * 退出码
     */
    private int exitCode;

    /**
     * 标准输出
     */
    private String output;

    /**
     * 错误输出
     */
    private String error;

    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 默认构造函数
     */
    public CommandResult() {
    }

    /**
     * 完整构造函数
     */
    public CommandResult(String command, int exitCode, String output,
            String error, long executionTime, boolean success) {
        this.command = command;
        this.exitCode = exitCode;
        this.output = output;
        this.error = error;
        this.executionTime = executionTime;
        this.success = success;
    }

    /**
     * 创建成功的命令结果
     */
    public static CommandResult success(String command, String output, long executionTime) {
        return new CommandResult(command, 0, output, "", executionTime, true);
    }

    /**
     * 创建失败的命令结果
     */
    public static CommandResult failure(String command, String errorMessage) {
        return new CommandResult(command, -1, "", errorMessage, 0, false);
    }

    // Getter和Setter方法

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 重写toString方法
     * 
     * @return 结果信息的字符串表示
     */
    @Override
    public String toString() {
        return "CommandResult{" +
                "command='" + command + '\'' +
                ", exitCode=" + exitCode +
                ", output='" + output + '\'' +
                ", error='" + error + '\'' +
                ", executionTime=" + executionTime +
                ", success=" + success +
                '}';
    }
}
