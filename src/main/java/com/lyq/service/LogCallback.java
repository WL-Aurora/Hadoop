package com.lyq.service;

/**
 * 日志回调接口
 * 用于实时接收SSH命令执行的日志输出
 */
public interface LogCallback {
    /**
     * 标准输出日志
     * 
     * @param log 日志内容
     */
    void onLog(String log);

    /**
     * 错误输出日志
     * 
     * @param error 错误内容
     */
    void onError(String error);

    /**
     * 命令执行完成
     */
    void onComplete();
}
