package com.lyq.exception;

/**
 * 验证异常类
 * 用于封装输入验证过程中发生的异常情况
 * 
 * 需求: 需求 2（IP 地址格式验证）
 */
public class ValidationException extends Exception {
    
    /**
     * 字段名称
     */
    private final String fieldName;
    
    /**
     * 无效的值
     */
    private final String invalidValue;
    
    /**
     * 构造函数 - 基本构造
     * 
     * @param message 异常消息
     * @param fieldName 字段名称
     * @param invalidValue 无效的值
     */
    public ValidationException(String message, String fieldName, String invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    /**
     * 构造函数 - 包含原因
     * 
     * @param message 异常消息
     * @param cause 异常原因
     * @param fieldName 字段名称
     * @param invalidValue 无效的值
     */
    public ValidationException(String message, Throwable cause, String fieldName, String invalidValue) {
        super(message, cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    /**
     * 获取字段名称
     * 
     * @return 字段名称
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * 获取无效的值
     * 
     * @return 无效的值
     */
    public String getInvalidValue() {
        return invalidValue;
    }
    
    /**
     * 获取详细的错误信息
     * 包含字段名称和无效值
     * 
     * @return 详细错误信息
     */
    public String getDetailedMessage() {
        return String.format("字段 '%s' 验证失败: %s (输入值: '%s')", 
            fieldName, 
            getMessage(), 
            invalidValue);
    }
    
    @Override
    public String toString() {
        return String.format("ValidationException{fieldName='%s', invalidValue='%s', message='%s'}", 
            fieldName, invalidValue, getMessage());
    }
}
