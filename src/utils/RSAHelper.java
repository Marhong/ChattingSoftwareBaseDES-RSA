/**
 * 
 */
package utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
/**
 * RSA辅助类
 * @author Wangbin
 * @time 2018年6月24日 上午8:56:47
 */
public class RSAHelper {
    
    private static BigInteger x; //存储临时的位置变量x，y 用于递归
    
    private static BigInteger y;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	       //生成KeyPair
        MyKeyPair keyPair=generatorKey(1024);
        // 元数据
        String source = new String("哈哈哈哈哈~~~嘿嘿嘿嘿嘿~~---呵呵呵呵呵ASDFASD！");

        System.out.println("加密前:"+source);
        //使用公钥加密 
        String cryptdata=encrypt(source, keyPair.getPublicKey(),"UTF-8");
        System.out.println("加密后:"+cryptdata);
        String signResult = md5RsaSign(cryptdata, keyPair.getPrivateKey().getA().toString());
        System.out.println("签名后:"+signResult);
        // 先验证签名是否正确
        // 再使用私钥解密
        try {
        	if(verifySignature(cryptdata, signResult, keyPair.getPublicKey().getB().toString())) {
                String result=decrypt(cryptdata, keyPair.getPrivateKey(),"UTF-8");
                System.out.println("解密后:"+result);
               
        	}else {
        		System.out.println("签名错误");
        	}

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

   
   
    /**
     * 欧几里得扩展算法
     * @author Wangbin
     * @time 2018年6月24日 上午9:06:34
     */
    public static BigInteger ex_gcd(BigInteger a,BigInteger b){
        if(b.intValue()==0){
            x=new BigInteger("1");
            y=new BigInteger("0");
            return a;
        }
        BigInteger ans=ex_gcd(b,a.mod(b));
        BigInteger temp=x;
        x=y;
        y=temp.subtract(a.divide(b).multiply(y));
        return ans;
        
    }
    
    
    /**
     * 计算求出私钥的a值
     * @author Wangbin
     * @time 2018年6月24日 上午9:05:42
     */
    public static BigInteger cal(BigInteger a,BigInteger k){
        BigInteger gcd=ex_gcd(a,k);
        if(BigInteger.ONE.mod(gcd).intValue()!=0){
            return new BigInteger("-1");
        }
        //由于我们只求乘法逆元 所以这里使用BigInteger.One,实际中如果需要更灵活可以多传递一个参数,表示模的值来代替这里
        x=x.multiply(BigInteger.ONE.divide(gcd));
        k=k.abs();
        BigInteger ans=x.mod(k);
        if(ans.compareTo(BigInteger.ZERO)<0) ans=ans.add(k);
        return ans;
        
    }

    /**
     * 生成公钥和私钥
     * @author Wangbin
     * @time 2018年6月24日 上午9:04:58
     */
    public static MyKeyPair generatorKey(int bitlength){
        SecureRandom random=new SecureRandom();
        random.setSeed(new Date().getTime());
        BigInteger bigPrimep,bigPrimeq;
        while(!(bigPrimep=BigInteger.probablePrime(bitlength, random)).isProbablePrime(1)){
            continue;
        }//生成大素数p
        
        while(!(bigPrimeq=BigInteger.probablePrime(bitlength, random)).isProbablePrime(1)){
            continue;
        }//生成大素数q
        
        BigInteger n=bigPrimep.multiply(bigPrimeq);//生成n
        //生成k
        BigInteger k=bigPrimep.subtract(BigInteger.ONE).multiply(bigPrimeq.subtract(BigInteger.ONE));
        //生成一个比k小的b,或者使用65537
        BigInteger b=BigInteger.probablePrime(bitlength-1, random);
        //根据扩展欧几里得算法生成b 
        BigInteger a=cal(b,k);
        //存储入 公钥与私钥中
     
        MyPrivateKey privateKey=new MyPrivateKey(n, a);
        MyPublicKey  publicKey=new MyPublicKey(n, b);
        
        //生成秘钥对 返回密钥对
        return new MyKeyPair(publicKey, privateKey);
    }
    /**c 
     * 用公钥加密数据
     * @author Wangbin
     * @time 2018年6月24日 上午8:58:52
     */
    public static String encrypt(String source,MyPublicKey key,String charset){
        byte[] sourceByte = null;
        try {
            sourceByte = source.getBytes(charset);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BigInteger temp=new BigInteger(1,sourceByte);
        BigInteger encrypted=temp.modPow(key.getB(), key.getN());
        return Base64.encodeBase64String(encrypted.toByteArray());
        }
    
    /**
     * 用私钥解密数据
     * @author Wangbin
     * @time 2018年6月24日 上午8:59:00
     */
    public static String decrypt(String cryptdata,MyPrivateKey key,String charset) throws UnsupportedEncodingException{
        byte[] byteTmp=Base64.decodeBase64(cryptdata);
        BigInteger cryptedBig=new BigInteger(byteTmp);
        byte[] cryptedData=cryptedBig.modPow(key.getA(), key.getN()).toByteArray();
       // cryptedData=Arrays.copyOfRange(cryptedData, 1, cryptedData.length);//去除符号位的字节
        return new String(cryptedData,charset);
        
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
}
