/**
 * 
 */
package utils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 
 * @author Wangbin
 * @time 2018年6月24日 上午9:49:00
 */
public class RSAKeyPair {
	public PublicKey publicKey;
	public PrivateKey privateKey;
	/**
	 * @param publicKey
	 * @param privateKey
	 */
	public RSAKeyPair(PublicKey publicKey, PrivateKey privateKey) {
		super();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	/**
	 * @return the publicKey
	 */
	public PublicKey getPublicKey() {
		
		return publicKey;
	}
	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	/**
	 * @return the privateKey
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
}
