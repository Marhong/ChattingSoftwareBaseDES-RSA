package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.commons.codec.binary.Base64;

import utils.DESUtils;
import utils.MyKeyPair;
import utils.MyPrivateKey;
import utils.MyPublicKey;
import utils.RSAHelper;

public class Client extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	// north
	JMenuBar bar = new JMenuBar();
	JMenu m1 = new JMenu("连接服务器");
	JMenu m2 = new JMenu("关于");
	JMenuItem conServer = new JMenuItem("连接服务器");
	JMenuItem disServer = new JMenuItem("断开服务器");
	JMenuItem exit = new JMenuItem("退出");
	JMenuItem about = new JMenuItem("关于本软件");
	ImageIcon img = new ImageIcon(Client.class.getResource("top.jpg"));
	JLabel top = new JLabel(img);
	JPanel north = new JPanel();
	// west
	JPanel west = new JPanel();
	ImageIcon img2 = new ImageIcon(Client.class.getResource("left.jpg"));
	JLabel left = new JLabel(img2);
	DefaultListModel dlm = new DefaultListModel();
	JList userList = new JList(dlm);
	JScrollPane listPane = new JScrollPane(userList);
	// center
	JPanel center = new JPanel();
	JTextArea showMsg = new JTextArea(10, 20);
	JScrollPane showPane = new JScrollPane(showMsg);
	JPanel operPane = new JPanel();
	JLabel input = new JLabel("请输入:");
	JTextField msgInput = new JTextField(24);
	JButton send = new JButton("send");
	// net
	static Socket socket;
	static String name;
	static OutputStreamWriter oos;
	static List<PersonalFrame> personalFrames = new ArrayList<PersonalFrame>();
	// 数据加密部分属性
	
	private MyPublicKey publicKey;
	private MyPrivateKey privateKey;
	private MyPublicKey friendPublicKeys;
	private String publickKeyString = "";
	private MyKeyPair keyPair;
	private String tempParticipant = "";
	private String tempParticipantKey = "";
	private String desKey = "";
	public Client() {
		init();
		addEvent();
		initFrame();
	}

	/**
	 * 初始化主界面
	 * 
	 * @author Wangbin
	 * @time 2018年6月24日 下午2:38:22
	 */
	public void init() {
		// 初始化PublicKey PrivateKey DES key
		keyPair = RSAHelper.generatorKey(1024);
		privateKey = keyPair.getPrivateKey();
		publicKey = keyPair.getPublicKey();
		publickKeyString = publicKey.getN().toString() + "_" + publicKey.getB().toString();
		desKey = DESUtils.generateRandomKey();
		// north
		m1.add(conServer);
		m1.add(disServer);
		m1.addSeparator();
		m1.add(exit);
		m2.add(about);
		bar.add(m1);
		bar.add(m2);
		BorderLayout bl = new BorderLayout();
		north.setLayout(bl);
		north.add(bar, BorderLayout.NORTH);
		// north.add(top,BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);
		// west
		Dimension dim = new Dimension(100, 150);
		west.setPreferredSize(dim);
		Dimension dim2 = new Dimension(100, 300);
		listPane.setPreferredSize(dim2);
		BorderLayout bl2 = new BorderLayout();
		west.setLayout(bl2);
		// west.add(left,BorderLayout.NORTH);
		west.add(listPane, BorderLayout.CENTER);
		add(west, BorderLayout.EAST);
		userList.setFont(new Font("隶书", Font.BOLD, 18));
		// center
		// showMsg.setFont(new Font("宋体",Font.BOLD,28));
		msgInput.setActionCommand("enterKey");
		showMsg.setEditable(false);
		BorderLayout bl3 = new BorderLayout();
		center.setLayout(bl3);
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		operPane.setLayout(fl);
		operPane.add(input);
		operPane.add(msgInput);
		operPane.add(send);
		center.add(showPane, BorderLayout.CENTER);
		center.add(operPane, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);

	}

	/**
	 * 给空间设置监听器
	 * 
	 * @author Wangbin
	 * @time 2018年6月24日 下午2:38:37
	 */
	public void addEvent() {
		// 事件的绑定
		send.addActionListener(this);
		msgInput.addActionListener(this);
		conServer.addActionListener(this);
		disServer.addActionListener(this);
		exit.addActionListener(this);
		about.addActionListener(this);
		SymMouse picker = new SymMouse();
		userList.addMouseListener(picker);
	}

	public void initFrame() {
		setResizable(false);
		setTitle("即时通讯系统客户端");
		setSize(500, 400);
		setLocation(200, 50);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void dispose() {
		try {
			ExitProgram();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 发送编辑框内的内容
	 * 
	 * @author Wangbin
	 * @time 2018年6月24日 下午3:10:20
	 */
	public void SendMessage() throws Exception {
		// 判断是否连接了服务器
		if (socket == null) {
			JOptionPane.showMessageDialog(this, "没有连接服务器或已经断开服务器");
			return;
		}
		// 发送消息
		String content = msgInput.getText();
		if (content == null || content.trim().equals("")) {
			JOptionPane.showMessageDialog(this, "不能发送空字符串");
		} else {
			// 清空文本输入框
			msgInput.setText("");
		}
		Message msg = new Message(name, content, MessageType.Chat, ChatState.Group, null);
		//oos.writeObject(msg);
		oos.write(name+"_"+content+"_1"+"_1");
		oos.flush();
	}

	/**
	 * 发送自定义内容
	 * 
	 * @author Wangbin
	 * @time 2018年6月24日 下午3:10:49
	 */
	public void SendAnyMessage(String content) throws Exception {
		// 判断是否连接了服务器
		if (socket == null) {
			JOptionPane.showMessageDialog(this, "没有连接服务器或已经断开服务器");
			return;
		}
		// 发送消息
		Message msg = new Message(name, content, MessageType.Chat, ChatState.Personal, null);
		//oos.writeObject(msg);
		oos.write(name+"_"+content+"_1"+"_0");
		oos.flush();
	}

	public void ConnectServer() throws Exception {
		// 弹出输入框，提示用户输入服务器IP地址
		int cnt = 0;
		do {
			if (++cnt == 4) {
				JOptionPane.showMessageDialog(this, "对不起，您的输入已经超出3次\n程序将自动退出！");
				System.exit(0);
			}
			String ip = JOptionPane.showInputDialog("请您输入服务器IP:");
			String portStr = JOptionPane.showInputDialog("请您输入服务器PORT:");
			System.out.println("程序接收到的信息有：" + ip + " " + portStr);
			socket = new Socket("127.0.0.1", Integer.parseInt(portStr));
			name = JOptionPane.showInputDialog("请输入您的昵称:");
			if (name == null || name.trim().equals("")) {
				name = "游客";
			}
			//OutputStream os = socket.getOutputStream();
			oos = new OutputStreamWriter(socket.getOutputStream(),
					"UTF-8");
			Message msg = new Message(name, publickKeyString, MessageType.Login);
			oos.write(name+"_"+publickKeyString+"_0");
			oos.flush();

		} while (socket == null);
		// 成功创建连接后启动一个新线程接收消息
		new Thread() {
			@Override
			public void run() {
				try {
					boolean flag = true;
					while (flag) {
						InputStream is = socket.getInputStream();
						ObjectInputStream ois = new ObjectInputStream(is);
						Message msg = (Message) (ois.readObject());
						MessageType type = msg.getMsgType();
						// System.out.println("msgType:"+type);
						if (type.equals(MessageType.Login)) {
							String content = msg.getContent();
							String old = showMsg.getText();
							if (old == null || old.trim().equals("")) {
								showMsg.setText(content);
							} else {
								String temp = old + "\n" + content;
								showMsg.setText(temp);
							}
							showMsg.setCaretPosition(showMsg.getText().length());
							if (dlm.size() == 0) {
								List<String> names = msg.getNames();
								for (String s : names) {

									if (!s.equals(name)) {
										dlm.addElement(s);
										System.out.println(s + "添加成功");
									}
								}
							} else {
								if (!msg.getFrom().equals(name))
									dlm.addElement(msg.getFrom());
							}
						} else if (type.equals(MessageType.Chat)) {
							String content2 = msg.getContent();
							String[] keyInfo = content2.split(":");
							if(keyInfo.length == 3 && keyInfo[0].equals("publickey")) {
								if(tempParticipant.equals(keyInfo[1])) {
									tempParticipantKey = keyInfo[2];
									String[] nAndE = tempParticipantKey.split("_");
									friendPublicKeys = new MyPublicKey(new BigInteger(nAndE[0]), new BigInteger(nAndE[1]));
								}
							}else {
								if (msg.getChatState().equals(ChatState.Personal)) {
									boolean FindIt = false;
									for (PersonalFrame pf : personalFrames) {
										if (pf.getName().equals(msg.getFrom()) || pf.getName().equals(msg.getTo())) {
											String content = msg.getContent();
											String[] infParts = content.split(":");
											if(infParts.length == 3&&infParts[1].equals("sendDESKeys")){
												// 如果接受的是DES key，则需要将数据用RSA进行解密
												try {
													desKey = RSAHelper.decrypt(infParts[2], privateKey, "UTF-8");
												} catch (UnsupportedEncodingException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
												content = infParts[0]+": common DESKey is "+desKey;
												msg.setContent(content);
												pf.showMsgAndChangeDES(msg, desKey);
											}else {
												// 如果接受的是一般数据，则需要将数据用DES进行解密
												try {
													content = new String(DESUtils.decodeDES(desKey.getBytes(), Base64.decodeBase64(infParts[1])));
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
												msg.setContent(content);
												pf.showMsg(msg);
											}
											
											System.out.println("成功找到！");
											FindIt = true;
											break;
										}
									}
									if (FindIt == false) {
										PersonalFrame myFrame = new PersonalFrame(name, msg.getFrom(),friendPublicKeys,desKey);
										personalFrames.add(myFrame);
										System.out.println("成功创建！");
										myFrame.showMsg(msg);
									}
								} else {
									String content = msg.getContent();
									String old = showMsg.getText();
									if (old == null || old.trim().equals("")) {
										showMsg.setText(content);
									} else {
										String temp = old + "\n" + content;
										showMsg.setText(temp);
									}
									showMsg.setCaretPosition(showMsg.getText().length());
								}
							}
							
						} else if (type.equals(MessageType.Logout)) {
							String content = msg.getContent();
							String old = showMsg.getText();
							if (old == null || old.trim().equals("")) {
								showMsg.setText(content);
							} else {
								String temp = old + "\n" + content;
								showMsg.setText(temp);
							}
							showMsg.setCaretPosition(showMsg.getText().length());
							if (!msg.getFrom().equals(name))
								dlm.removeElement(msg.getFrom());
							if (msg.getFrom().equals(name))
								flag = false;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();
		setTitle("即时通讯系统客户端---" + name);
	}

	public void BreakServer() throws Exception {
		if (socket != null) {
			Message msg = new Message(name, null, MessageType.Logout);
			//oos.writeObject(msg);
			oos.write(name+"_2");
			oos.flush();
			dlm.removeAllElements();
			socket = null;
			setTitle("即时通讯系统客户端---" + name + "已离线");
		}
	}

	public void ExitProgram() throws Exception {
		int flag;
		if (socket != null) {
			flag = JOptionPane.showConfirmDialog(this, "还没有注销，直接退出将自动注销连接！\n确定要退出吗？");
		} else {
			flag = 0;
		}
		// System.out.println("flag : "+flag);
		if (flag == 0) {
			if (socket != null) {
				Message msg = new Message(name, null, MessageType.Logout);
				//oos.writeObject(msg);
				oos.write(name+"_2");
				oos.flush();
				socket = null;
				dlm.removeAllElements();
			}
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String comm = e.getActionCommand();
			System.out.println(comm);
			if (comm.equals("send") || comm.equals("enterKey") || comm.equals("sendPersonalMessage")) {
				SendMessage();
			} else if (comm.equals("连接服务器")) {
				ConnectServer();
			} else if (comm.equals("断开服务器")) {
				BreakServer();
			} else if (comm.equals("退出")) {
				ExitProgram();
			} else if (comm.equals("关于本软件")) {
				JOptionPane.showMessageDialog(this, "Tony Support");
			} else {
				System.out.println("不识别的事件");
			}
		} catch (Exception e1) {
			// 不作处理
		}
	}

	// 内部类，用于响应双击JList中item的事件
	class SymMouse extends java.awt.event.MouseAdapter {
		public void mouseClicked(java.awt.event.MouseEvent e) {
			Object object = e.getSource();
			if (object == userList)
				userList_mouseClicked(e);
		}
	}

	void userList_mouseClicked(java.awt.event.MouseEvent event) {
		try {

			if (event.getModifiers() == MouseEvent.BUTTON1_MASK && event.getClickCount() == 2) {
				// JOptionPane.showMessageDialog(this, userList.getSelectedValue().toString(),
				// "警告 ",JOptionPane.WARNING_MESSAGE);
				String participant = userList.getSelectedValue().toString();
				tempParticipant = participant;
				SendAnyMessage("publickey:" + participant);
				Thread.sleep(100);
				PersonalFrame myFrame = new PersonalFrame(name, participant,friendPublicKeys,desKey);
				personalFrames.add(myFrame);
				tempParticipant = "";
				tempParticipantKey ="";
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void main(String[] args) {
		new Client();
	}
}
