/**
 * 
 */
package utils;

/**
 * 自定义的密钥对
 * @author Wangbin
 * @time 2018年6月24日 上午8:49:43
 */
public class MyKeyPair {
	public MyPublicKey publicKey;
	public MyPrivateKey privateKey;
	
	/**
	 * @param publicKey
	 * @param privateKey
	 */
	public MyKeyPair(MyPublicKey publicKey, MyPrivateKey privateKey) {
		super();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	/**
	 * @return the publicKey
	 */
	public MyPublicKey getPublicKey() {
		return publicKey;
	}
	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(MyPublicKey publicKey) {
		this.publicKey = publicKey;
	}
	/**
	 * @return the privateKey
	 */
	public MyPrivateKey getPrivateKey() {
		return privateKey;
	}
	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(MyPrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
}
