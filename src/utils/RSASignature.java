package utils;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;

/**
 * 
 */

/**
 * RSA工具类
 * 
 * @author Wangbin
 * @time 2018年6月17日 上午9:09:33
 */
public class RSASignature {

	public static String publicKeyString;
	public static String privateKeyString;
	public static PublicKey publicKey;
	public static PrivateKey privateKey;


	/**
	 * 生成公钥和私钥
	 * @author Wangbin
	 * @time 2018年6月24日 上午12:42:40
	 */
	public static void generatePublicAndPrivateKey() {
		try {

			// 1.初始化密钥
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			// 设置KEY的长度
			keyPairGenerator.initialize(512);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			// 得到公钥
			publicKey = keyPair.getPublic();
			// 得到私钥
			privateKey = keyPair.getPrivate();
			// 展示数据:
			final byte[] privateKeyEncoded = privateKey.getEncoded();
			final byte[] publicKeyEncoded = publicKey.getEncoded();
			// 将密钥存为String
			publicKeyString = Base64.encodeBase64String(publicKeyEncoded);
			privateKeyString = Base64.encodeBase64String(privateKeyEncoded);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 加密数据
	 * 
	 * @author Wangbin
	 * @time 2018年6月24日 上午1:05:26
	 */
	public static String encryptData(String data,String privatekeyString) {
		String encryptResult = "";
		byte[] decode = Base64.decodeBase64(privatekeyString);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			// 私钥字节数组转换成私钥
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decode);
			PrivateKey privateKey1 = keyFactory.generatePrivate(keySpec);
			encryptResult = encrypt(data, privateKey1, 0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return encryptResult;
	}

	/**
	 * 解密数据
	 * @author Wangbin
	 * @time 2018年6月24日 上午1:07:48
	 */
	public static String decryptData(String data,String publickeyString) {
		String decryptResult = "";
		byte[] decode1 = Base64.decodeBase64(publickeyString);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode1);
			PublicKey publicKey1 = keyFactory.generatePublic(keySpec);
			decryptResult = encrypt(data, publicKey1, 1);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return decryptResult;
	}
	
	/**
	 * 对加密后的数据进行签名
	 * @author Wangbin
	 * @time 2018年6月24日 上午1:10:57
	 */
	public static String md5RsaSign(String data,String privatekeyString) {
		String signResult = "";
		byte[] decode = Base64.decodeBase64(privatekeyString);
		 try {
             Signature signature = Signature.getInstance("MD5withRSA");
             // 使用私钥进行数字签名
             KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 			// 私钥字节数组转换成私钥
 			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decode);
 			PrivateKey privateKey1 = keyFactory.generatePrivate(keySpec);
             signature.initSign(privateKey1);
             signature.update(data.getBytes());
             byte[] sign = signature.sign();//签名的数据
             //将源文件和签名的数据一块发送给合作伙伴
             signResult = Base64.encodeBase64String(sign);
		 }catch (Exception e) {
			// TODO: handle exception
			 e.printStackTrace();
		}
		return signResult;
	}
	/**
	 * 验证签名是否正确
	 * @author Wangbin
	 * @time 2018年6月24日 上午1:13:57
	 */
	public static boolean verifySignature(String data,String signResult,String publickeyString) {
		boolean mSignResult = false;
		byte[] decode1 = Base64.decodeBase64(publickeyString);
		try {
            Signature signature = Signature.getInstance("MD5withRSA");
            //数字验证，使用公钥
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode1);
			PublicKey publicKey1 = keyFactory.generatePublic(keySpec);
            signature.initVerify(publicKey1);
            //update,传递过来的源文件
            signature.update(data.getBytes());

            //对签名进行验证 mSignResult
            mSignResult = signature.verify(Base64.decodeBase64(signResult));
           
        } catch (Exception e) {
            e.printStackTrace();
        }
		return mSignResult;
	}
	/**
	 * 根据不同模式，生成加密或解密结果
	 * @author Wangbin
	 * @time 2018年6月24日 上午1:24:15
	 */
	public static String encrypt(String data, Key key, int type) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			// 设置密码
			switch (type) {
			case 0:
				cipher.init(Cipher.ENCRYPT_MODE, key);
				break;
			case 1:
				cipher.init(Cipher.DECRYPT_MODE, key);
				break;
			}
			// 区分加密解密:
			switch (type) {
			case 0:
				byte[] bytes = cipher.doFinal(data.getBytes());
				byte[] encode = Base64.encodeBase64(bytes);
				String result = new String(encode);
				return result;
			case 1:
				byte[] decode = Base64.decodeBase64(data);
				byte[] doFinal = cipher.doFinal(decode);
				String dResult = new String(doFinal);
				return dResult;
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return data;
	}

}
