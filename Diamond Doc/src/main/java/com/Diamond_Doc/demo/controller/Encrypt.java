package com.Diamond_Doc.demo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


@Service
public class Encrypt {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";//默认的加密算法

    public static byte[] initSecretKey() {

        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
        //初始化此密钥生成器，使其具有确定的密钥大小
        //AES 要求密钥长度为 128
        kg.init(128);
        //生成一个密钥
        SecretKey  secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    static Key toKey(byte[] key){
        //生成密钥
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    public static byte[] encrypt(byte[] data,Key key) throws Exception{
        return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] encrypt(byte[] data,byte[] key) throws Exception{
        return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] encrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
        //还原密钥
        Key k = toKey(key);
        return encrypt(data, k, cipherAlgorithm);
    }

    public static byte[] encrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{
        //实例化
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        //使用密钥初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, key);
        //执行操作
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
        return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] decrypt(byte[] data,Key key) throws Exception{
        return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] decrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
        //还原密钥
        Key k = toKey(key);
        return decrypt(data, k, cipherAlgorithm);
    }

    public static byte[] decrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{
        //实例化
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        //使用密钥初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, key);
        //执行操作
        return cipher.doFinal(data);
    }

    private static String  showByteArray(byte[] data){
        if(null == data){
            return null;
        }
        StringBuilder sb = new StringBuilder("{");
        for(byte b:data){
            sb.append(b).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        byte[] key = initSecretKey();
        System.out.println("key："+showByteArray(key));
        Key k = toKey(key); //生成秘钥
        String data ="111111";
        System.out.println("加密前数据: string:"+data);
        System.out.println("加密前数据: byte[]:"+showByteArray(data.getBytes()));
        System.out.println();
        byte[] encryptData = encrypt(data.getBytes(), k);//数据加密
        System.out.println("加密后数据: byte[]:"+showByteArray(encryptData));
//       System.out.println("加密后数据: hexStr:"+Hex.encodeHexStr(encryptData));
        System.out.println();
        byte[] decryptData = decrypt(encryptData, k);//数据解密
        System.out.println("解密后数据: byte[]:"+showByteArray(decryptData));
        System.out.println("解密后数据: string:"+new String(decryptData));
        System.out.println(encrypt(data,k));
        System.out.println(decrypt(encrypt(data,k),k));
    }
    public static String encrypt(String data,Key k)throws Exception{
        byte[] encryptData = encrypt(data.getBytes(), k);
        return showByteArray(encryptData);
    }
    public static String decrypt(String encryptdata,Key k)throws Exception{
        encryptdata=encryptdata.substring(1,encryptdata.length()-1);
        String[] tmp=encryptdata.split(",");
        byte[] data=new byte[tmp.length];
        int i=0;
        for( String item:tmp){
            data[i++]= (byte) Integer.parseInt(item);
        }
        byte[] decryptData = decrypt(data, k);
        return new String(decryptData);
    }

    //Token
    //密钥文本，密钥不要泄漏
    private static String secret = "test-jwt";

    /**
     * 创建一个带有uid字段的token，过期时间为30分钟
     * @param id
     * @return
     */
    public static String create(int id,String name,String email,String avatar) {
        //JWT默认头部alg=HS256，typ=JWT，如果不更换加密方式可以不设置头部
        Map<String, Object> header = new HashMap();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        String token = JWT.create() //创建一个jwt对象
                .withHeader(header) //设置Header（头部）
                .withExpiresAt(new Date(System.currentTimeMillis() + 360L * 60 * 1000))  //设置过期时间为30分钟后，其他官方字段，后续追加即可
                .withClaim("id", id)  //设置自己的字段，字段名为uid。多个字段在后面继续使用withClaim()方法即可
                .withClaim("name", name)
                .withClaim("email", email)
                .withClaim("avatar", avatar)
                .sign(Algorithm.HMAC256(secret));   //设置签名，签名算法为头部的HS256，密钥为secret变量的值
        return token;
    }

    /**
     * 解密token
     * @param token
     * @return
     */
    public static DecodedJWT decoded(String token){
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();//创建一个加密算法为HS256的校验器
        DecodedJWT decoded = verifier.verify(token); //校验传入的token，生成一个解密的JWT
        return decoded;
    }
}
