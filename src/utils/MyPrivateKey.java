/**
 * 
 */
package utils;

import java.math.BigInteger;
import java.security.PrivateKey;
/**
 * 自定义的私钥
 * @author Wangbin
 * @time 2018年6月24日 上午8:48:56
 */
public class MyPrivateKey implements PrivateKey{

	private static final long serialVersionUID = 1L;
	public BigInteger N;
	public BigInteger A;
	/**
	 * @param n
	 * @param a
	 */
	public MyPrivateKey(BigInteger n, BigInteger a) {
		super();
		N = n;
		A = a;
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
	 * @return the a
	 */
	public BigInteger getA() {
		return A;
	}
	/**
	 * @param a the a to set
	 */
	public void setA(BigInteger a) {
		A = a;
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
