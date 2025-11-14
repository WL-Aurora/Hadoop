package com.lyq.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 * 提供AES-256加密解密功能，用于保护敏感信息
 */
public class EncryptionUtil {
    private static final Logger logger = LogManager.getLogger(EncryptionUtil.class);
    
    // 加密算法
    private static final String ALGORITHM = "AES";
    // 加密模式和填充方式
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    // 密钥长度（256位）
    private static final int KEY_SIZE = 256;
    // 初始化向量长度
    private static final int IV_SIZE = 16;
    
    // 密钥存储路径
    private static final String KEY_DIR = System.getProperty("user.home") + File.separator + ".hads" + File.separator + "keys";
    private static final String KEY_FILE = KEY_DIR + File.separator + "secret.key";
    
    // 缓存的密钥
    private static SecretKey cachedKey = null;
    
    /**
     * 加密字符串
     * 
     * @param plainText 明文
     * @return Base64编码的密文，格式为：IV:密文
     * @throws Exception 加密失败时抛出异常
     */
    public static String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            logger.warn("待加密文本为空");
            return plainText;
        }
        
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // 生成随机IV
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 将IV和密文组合，用冒号分隔
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
            
            logger.debug("文本加密成功");
            return ivBase64 + ":" + encryptedBase64;
        } catch (Exception e) {
            logger.error("加密失败: {}", e.getMessage());
            throw new Exception("加密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解密字符串
     * 
     * @param encryptedText 加密文本，格式为：IV:密文
     * @return 明文
     * @throws Exception 解密失败时抛出异常
     */
    public static String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            logger.warn("待解密文本为空");
            return encryptedText;
        }
        
        try {
            // 分离IV和密文
            String[] parts = encryptedText.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("加密文本格式错误");
            }
            
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            
            logger.debug("文本解密成功");
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("解密失败: {}", e.getMessage());
            throw new Exception("解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取或创建密钥
     * 如果密钥文件存在则加载，否则生成新密钥并保存
     * 
     * @return AES密钥
     * @throws Exception 密钥操作失败时抛出异常
     */
    private static SecretKey getOrCreateKey() throws Exception {
        // 如果已缓存密钥，直接返回
        if (cachedKey != null) {
            return cachedKey;
        }
        
        File keyFile = new File(KEY_FILE);
        
        if (keyFile.exists()) {
            // 加载现有密钥
            cachedKey = loadKey();
            logger.debug("从文件加载密钥");
        } else {
            // 生成新密钥
            cachedKey = generateKey();
            saveKey(cachedKey);
            logger.info("生成新密钥并保存到: {}", KEY_FILE);
        }
        
        return cachedKey;
    }
    
    /**
     * 生成AES密钥
     * 
     * @return 生成的密钥
     * @throws Exception 生成失败时抛出异常
     */
    private static SecretKey generateKey() throws Exception {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            return keyGenerator.generateKey();
        } catch (Exception e) {
            logger.error("生成密钥失败: {}", e.getMessage());
            throw new Exception("生成密钥失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存密钥到文件
     * 
     * @param key 要保存的密钥
     * @throws Exception 保存失败时抛出异常
     */
    private static void saveKey(SecretKey key) throws Exception {
        try {
            // 创建密钥目录
            File keyDir = new File(KEY_DIR);
            if (!keyDir.exists()) {
                if (!keyDir.mkdirs()) {
                    throw new Exception("创建密钥目录失败: " + KEY_DIR);
                }
            }
            
            // 保存密钥
            byte[] keyBytes = key.getEncoded();
            try (FileOutputStream fos = new FileOutputStream(KEY_FILE)) {
                fos.write(keyBytes);
            }
            
            // 设置文件权限（仅当前用户可读写）
            File keyFile = new File(KEY_FILE);
            keyFile.setReadable(false, false);
            keyFile.setReadable(true, true);
            keyFile.setWritable(false, false);
            keyFile.setWritable(true, true);
            
            logger.debug("密钥已保存到: {}", KEY_FILE);
        } catch (Exception e) {
            logger.error("保存密钥失败: {}", e.getMessage());
            throw new Exception("保存密钥失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从文件加载密钥
     * 
     * @return 加载的密钥
     * @throws Exception 加载失败时抛出异常
     */
    private static SecretKey loadKey() throws Exception {
        try {
            File keyFile = new File(KEY_FILE);
            byte[] keyBytes = new byte[(int) keyFile.length()];
            
            try (FileInputStream fis = new FileInputStream(keyFile)) {
                int bytesRead = fis.read(keyBytes);
                if (bytesRead != keyBytes.length) {
                    throw new Exception("密钥文件读取不完整");
                }
            }
            
            logger.debug("密钥已从文件加载: {}", KEY_FILE);
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            logger.error("加载密钥失败: {}", e.getMessage());
            throw new Exception("加载密钥失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清除缓存的密钥（用于安全清理）
     */
    public static void clearCachedKey() {
        cachedKey = null;
        logger.debug("已清除缓存的密钥");
    }
}
