package com.changtian.factor.common;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加解密工具类
 */
@Slf4j
public class EncipherUtil {
    private static final String ENCRYPT_KEY = "123456";

    /*
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
    public static String AESEncode(String content){
        try {


            SecretKey key=getSecretKey();
            Cipher cipher=Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte [] byte_encode=content.getBytes(StandardCharsets.UTF_8);
            //9.根据密码器的初始化方式--加密：将数据加密
            byte [] byte_AES=cipher.doFinal(byte_encode);
            //10.将加密后的数据转换为字符串
            //11.将字符串返回
            return new String(Base64.getEncoder().encode(byte_AES));
        } catch (Exception e) {
            log.info("AESEncode Exception",e);

        }

        //如果有错就返加nulll
        return "";
    }
    /*
     * 解密
     * 解密过程：
     * 1.同加密1-4步
     * 2.将加密后的字符串反纺成byte[]数组
     * 3.将加密内容解密
     */
    public static String AESDecode(String content){
        try {

            SecretKey key=getSecretKey();
            Cipher cipher=Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.DECRYPT_MODE, key);
            //8.将加密并编码后的内容解码成字节数组
            byte [] byte_content= Base64.getDecoder().decode(content);
            /*
             * 解密
             */
            byte [] byte_decode=cipher.doFinal(byte_content);
            return new String(byte_decode, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.info("AESDncode Exception",e);
        }

        //如果有错就返加空
        return "";
    }

    public static void main(String[] args) {
        /*
         * 加密
         */

        log.info("请输入要加密的内容:");
        String content = "nbcb,4286";
        log.info("加密后的密文是:"+ AESEncode(content));

        /*
         * 解密
         */
        log.info("使用AES对称解密，请输入加密的规则：(须与加密相同)");

        content =  "nj3gRcSTAD3QgXRk/tvTYQ==";
        log.info("解密后的明文是:"+ AESDecode(content));
    }

    /**
     * 获取加密key
     */
    public static SecretKey getSecretKey() throws  Exception{
        //1.构造密钥生成器，指定为AES算法,不区分大小写
        //            KeyGenerator keygen=KeyGenerator.getInstance("AES");
        //2.根据ecnodeRules规则初始化密钥生成器
        //生成一个128位的随机源,根据传入的字节数组

        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(ENCRYPT_KEY.getBytes());
        keygen.init(128, random);
        //3.产生原始对称密钥
        SecretKey original_key=keygen.generateKey();
        //4.获得原始对称密钥的字节数组
        byte [] raw=original_key.getEncoded();
        //5.根据字节数组生成AES密钥
        return new SecretKeySpec(raw, "AES");
    }
}
