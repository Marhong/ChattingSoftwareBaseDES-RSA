/**
 * 
 */
package utils;

import java.math.BigInteger;
import java.security.PublicKey;

/**
 * 自定义的公钥
 * @author Wangbin
 * @time 2018年6月24日 上午8:47:28
 */
public class MyPublicKey implements PublicKey{

	private static final long serialVersionUID = 1L;
	public BigInteger N;
	public BigInteger B;
	
	/**
	 * @param n
	 * @param b
	 */
	public MyPublicKey(BigInteger n, BigInteger b) {
		super();
		N = n;
		B = b;
	}
	/**
	 * @return the n
	 */
	public BigInteger getN() {
		return N;
	}
	/**
	 * @param n the n to set
	 */
	public void setN(BigInteger n) {
		N = n;
	}
	/**
	 * @return the b
	 */
	public BigInteger getB() {
		return B;
	}
	/**
	 * @param b the b to set
	 */
	public void setB(BigInteger b) {
		B = b;
	}
	/* (non-Javadoc)
	 * @see java.security.Key#getAlgorithm()
	 */
	@Override
	public String getAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.security.Key#getEncoded()
	 */
	@Override
	public byte[] getEncoded() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.security.Key#getFormat()
	 */
	@Override
	public String getFormat() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
