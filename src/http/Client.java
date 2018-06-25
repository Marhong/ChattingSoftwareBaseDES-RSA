package http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;

import utils.DESUtils;
import utils.MyKeyPair;
import utils.MyPrivateKey;
import utils.MyPublicKey;
import utils.RSAHelper;
import utils.RSASignature;

/**
 * 客户端
 * 
 * @author Wangbin
 * @time 2018年6月23日 下午5:19:29
 */
public class Client extends Socket {

	private static final String SERVER_IP = "127.0.0.1"; // 服务端IP
	private static final int SERVER_PORT = 8899; // 服务端端口
	private static final String END_MARK = "quit"; // 退出聊天室标识
	private static String myName = "";
	private int choices;
	private int selectedChoice = 0;
	private String frientName = "";
	private boolean isChatting = false;
	private String desKey = "";
	private String myPrivateKey = "";
	private String myPublicKey = "";
	private String friendPublicKey = "";
	private MyPublicKey publicKey;
	private MyPrivateKey privateKey;
	private MyPublicKey friendPublicKeys;
	private String publickKeyString ="";
	private String friendPublicKeyString ="";
	private MyKeyPair keyPair;
	private ArrayList<String> userList = new ArrayList<String>();
	private Socket client;

	private Writer writer;

	// 发送消息输入流
	private BufferedReader in;

	/**
	 * 构造函数<br/>
	 * 与服务器建立连接
	 * 
	 * @throws Exception
	 */
	public Client() throws Exception {
		super(SERVER_IP, SERVER_PORT);
		this.client = this;
		myName = String.valueOf(client.getLocalPort());
		// 生成自己的公钥和私钥
		keyPair = RSAHelper.generatorKey(1024);
		privateKey = keyPair.getPrivateKey();
		publicKey = keyPair.getPublicKey();
		publickKeyString = publicKey.getN().toString()+"_"+publicKey.getB().toString();
		desKey = DESUtils.generateRandomKey();
		System.out.println("Cliect[port:" + client.getLocalPort() + "] has connected to the server!");
		System.out.println("My DESKey is：" + desKey);
	}

	/**
	 * 启动监听收取消息，循环可以不停的输入消息，将消息发送出去
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {

		this.writer = new OutputStreamWriter(this.getOutputStream(), "UTF-8");
		// 将自己的公钥发送给服务器
		
		writer.write("publickey:" + myName + ":" + publickKeyString);
		writer.write("\n");
		writer.flush(); // 写完后要记得flush
		// 获取当前连接服务器的用户列表
		System.out.println("select a client to start chatting");
		System.out.println(" ======== Online member list ======== ");
		writer.write("viewuser");
		writer.write("\n");
		writer.flush(); // 写完后要记得flush
		new Thread(new ReceiveMsgTask()).start(); // 启动监听

		while (true) {
			in = new BufferedReader(new InputStreamReader(System.in));
			String inputMsg = in.readLine();
			if (inputMsg.equals("quit") || inputMsg.equals("viewuser")) {
				System.out.println("select a client to start chatting");
				System.out.println(" ======== Online member list ======== ");
				writer.write(inputMsg);
				writer.write("\n");
				writer.flush(); // 写完后要记得flush
			} else {
				if (!isChatting) {
					selectedChoice = Integer.parseInt(inputMsg);
					if (selectedChoice <= choices && selectedChoice > 0) {
						isChatting = true;
						frientName = userList.get(selectedChoice - 1);
						// 向服务器请求获取对话对象的公钥
						writer.write("getpublickey:" + frientName);
						writer.write("\n");
						writer.flush(); // 写完后要记得flush
						System.out.println("The participant is:" + frientName);
					}
				} else {
					String msg = "";
					if (inputMsg.equals("sendMyDESKey")) {
						String encryptDESKey = RSAHelper.encrypt(desKey, friendPublicKeys,"UTF-8");
						//String signEncryptDESKey = RSASignature.md5RsaSign(encryptDESKey, myPrivateKey);
						msg = "DESKey_" + encryptDESKey + "_" + encryptDESKey + "_" + frientName;
						System.out.println("The DESKey is " + ":" + desKey);
						System.out.println("The RSA encryptDESKey is " + ":" + encryptDESKey);
						//System.out.println("The RSA signEncryptDESKey is " + ":" + signEncryptDESKey);
						// 01:DESKey_encryptDESKey_signEncryptDESKey_friendName
					} else {
						String encryptInputMsg = Base64.encodeBase64String((DESUtils.encodeDES(desKey.getBytes(), inputMsg.getBytes())));
						msg = encryptInputMsg + "_" + frientName;
						System.out.println(myName + ":The DES encrypted message is " + encryptInputMsg);
					}
					writer.write(msg);
					writer.write("\n");
					writer.flush(); // 写完后要记得flush
					System.out.println(myName + ":The raw message is " + inputMsg);
					System.out.println("-----------------------------------------------------------------");
				}
			}

		}
	}

	/**
	 * 监听服务器发来的消息线程类
	 */
	class ReceiveMsgTask implements Runnable {

		private BufferedReader buff;

		@Override
		public void run() {
			try {
				this.buff = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
				while (true) {
					String result = buff.readLine();
					if (END_MARK.equals(result)) { // 遇到退出标识时表示服务端返回确认退出
						System.out.println("Cliect[port:" + client.getLocalPort() + "] has disconneted the connection");
						break;
					} else { // 输出服务端回复的消息
						String[] parts = result.split(":");
						if (parts.length == 3) {
							// 如果服务器返回的是用户列表
							if (parts[0].equals("memberlist")) {
								userList.clear();
								// 存储将当前所有用户名
								String[] users = parts[2].split("_");
								for (int i = 0; i < users.length; i++) {
									System.out.println(users[i]);
									userList.add(users[i].split(" ")[1]);
								}

								choices = Integer.parseInt(parts[1]);
							}
							// 如果服务器返回的是对话对象的公钥
							if (parts[0].equals("friendPublicKey") && parts[1].equals(myName)) {
								String[] keyInfo = parts[2].split("_");
								friendPublicKeys = new MyPublicKey(new BigInteger(keyInfo[0]), new BigInteger(keyInfo[1]));
								//friendPublicKey = parts[2];
								//System.out.println("The public key of " + frientName+":" + " n = " +keyInfo[0]+" b = "+keyInfo[1] );
								System.out.println("-----------------------------------------------------------------");
							}
						} else if (isChatting && parts.length == 2) {
							// 01:DESKey_encryptDESKey_signEncryptDESKey_00
							String[] deskInfo = parts[1].split("_");
							if (deskInfo.length == 4 && deskInfo[0].equals("DESKey")) {
								String init = parts[0];
								String rec = deskInfo[3];
								if (rec.equals(myName) && init.equals(frientName)) {
									desKey = RSAHelper.decrypt(deskInfo[1], privateKey, "UTF-8");
//									if (RSASignature.verifySignature(deskInfo[1], deskInfo[2], friendPublicKey)) {
//										desKey = RSASignature.decryptData(deskInfo[1], friendPublicKey);
//										System.out.println(init+":The RSA signEncryptDESKey is " + ":" + deskInfo[2]);
//										System.out.println(init+":The RSA encryptDESKey is " + ":" + deskInfo[1]);
//										System.out.println(init+":The raw DESKey is " + ":" + desKey);
//										System.out.println("-----------------------------------------------------------------");
//									} else {
//										System.out.println("the signature is wrong!!");
//										System.exit(0);
//									} 15988450E51E934A 5988450E51E934A
									System.out.println(init+":The RSA encryptDESKey is " + ":" + deskInfo[1]);
									System.out.println(init+":The raw DESKey is " + ":" + desKey);
									System.out.println("-----------------------------------------------------------------");
								}
								// INIT:ENCRYPTEDMSG_FRIENDNAME
							}else if(deskInfo.length == 2 && deskInfo[1].equals(myName)&&parts[0].equals(frientName)) {
								//System.out.println("我接收到了消息");
								String message = new String(DESUtils.decodeDES(desKey.getBytes(), Base64.decodeBase64(deskInfo[0])));
								System.out.println(parts[0]+":the DES encrypted message is "+deskInfo[0]);
								System.out.println(parts[0]+":the raw message is "+message);
								System.out.println("-----------------------------------------------------------------");
							}
							
						}

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					buff.close();
					// 关闭连接
					writer.close();
					client.close();
					in.close();
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * 入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Client client = new Client(); // 启动客户端
			client.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}