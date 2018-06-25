package gui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.*;


public class Server extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	ServerSocket ss = null;
	List<UserInfo> users=new ArrayList<UserInfo>(); // 存放所有的客户端信息
	List<String> names = new ArrayList<String>(); // 存放所有的客户端昵称
	private  List<String> pulicKeyList = new ArrayList<String>(); // 存放所有客户端的公钥
	static int count=0;
	private boolean run=true;
	//Design
	private JLabel portLabel=new JLabel("请输入端口号：");
	private JTextField textPort=new JTextField(5);
	private JLabel onlineNum=new JLabel("目前没有用户登录！");
	private DefaultListModel dlm = new DefaultListModel();
	private JList userList=new JList(dlm);
	private JButton Confirm=new JButton("启动");
	private JButton quit =new JButton("停止");

	/**
	 * 初始化服务器端主界面
	 * @author Wangbin
	 * @time 2018年6月24日 下午2:44:35
	 */
	private void InitFrame(){
		this.getContentPane().setLayout(new BorderLayout());
		JPanel pf=new JPanel(new FlowLayout());
		pf.add(portLabel);
		pf.add(textPort);
		JPanel p=new JPanel(new BorderLayout());
		p.add(pf,BorderLayout.NORTH);
		p.add(onlineNum,BorderLayout.SOUTH);
		add(p,BorderLayout.NORTH);
		add(userList,BorderLayout.CENTER);
		JPanel p2=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p2.add(quit);
		p2.add(Confirm);
		add(p2,BorderLayout.SOUTH);		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300,500);
		quit.setEnabled(false);
		setVisible(true);
	}
	private void addEvent(){
		Confirm.addActionListener(this);
	}
	public Server() throws Exception{
		InitFrame();
		addEvent();
	}
	@Override
	public void dispose(){
		try {
			stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
	private  void go() throws Exception{
		System.out.println("server start...");
		ss = new ServerSocket(Integer.parseInt(textPort.getText()));
		while(run){
				Socket socket = ss.accept();
				++count;
				onlineNum.setText("当前在线人数："+count);
				final Socket s = socket;
				new Thread(){
					UserInfo user=null;
					InputStream is=null;
					BufferedReader ois=null;
					@Override
					public void run() {
						try {
							boolean flag=true;
							is = s.getInputStream();
							ois = new BufferedReader(new InputStreamReader(
									socket.getInputStream(), "UTF-8"));
							while(flag){
								//Message msg = (Message)(ois.readObject());
								String msgString = ois.readLine();
								String[] inf = msgString.split("_");
								Message msg = new Message();
								if(inf.length == 5) {
									msg.setFrom(inf[0]);
									msg.setContent(inf[1]);
									if(inf[2].equals("0")) {
										msg.setMsgType(MessageType.Login);
									}else if(inf[2].equals("1")) {
										msg.setMsgType(MessageType.Chat);
									}else {
										msg.setMsgType(MessageType.Logout);
									}
									if(inf[3].equals("0")) {
										msg.setChatState(ChatState.Personal);
									}else {
										msg.setChatState(ChatState.Group);
									}
									msg.setTo(inf[4]);
									
								}else if(inf.length == 4) {
									msg.setFrom(inf[0]);
									msg.setContent(inf[1]);
									if(inf[2].equals("0")) {
										msg.setMsgType(MessageType.Login);
									}else if(inf[2].equals("1")) {
										msg.setMsgType(MessageType.Chat);
									}else {
										msg.setMsgType(MessageType.Logout);
									}
									if(inf[3].equals("0")) {
										msg.setChatState(ChatState.Personal);
									}else {
										msg.setChatState(ChatState.Group);
									}
								}else if(inf.length == 3) {
									msg.setFrom(inf[0]);
									msg.setContent(inf[1]);
									if(inf[2].equals("0")) {
										msg.setMsgType(MessageType.Login);
									}else if(inf[2].equals("1")) {
										msg.setMsgType(MessageType.Chat);
									}else {
										msg.setMsgType(MessageType.Logout);
									}
								}else {
									msg.setFrom(inf[0]);
									if(inf[2].equals("0")) {
										msg.setMsgType(MessageType.Login);
									}else if(inf[2].equals("1")) {
										msg.setMsgType(MessageType.Chat);
									}else {
										msg.setMsgType(MessageType.Logout);
									}
								}
								if(user==null){
									user=new UserInfo(s, msg.getFrom());
									System.out.println(msg.getFrom()+"登陆了！");
									System.out.println(s.getRemoteSocketAddress());
								}
								synchronized (this){
									if(!users.contains(user)){
										users.add(user);
										if(!names.contains(user.getName())){
											names.add(user.getName());
										}
										String publicKey = msg.getContent();
										if(!pulicKeyList.contains(publicKey)) {
											pulicKeyList.add(user.getName()+":"+publicKey);
											System.err.println("The public key of "+user.getName()+": "+publicKey);
										}
									}
								}
								MessageType type = msg.getMsgType();
								System.out.println("Type: "+type);
								if(type.equals(MessageType.Login)){
									//xxx上线啦【2010-6-7 20：10：54】
									String content = "【"+msg.getFrom()+"】  "+"上线啦.【"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"】";
									msg.setContent(content);
									msg.setNames(names);
									dlm.removeAllElements();
									for(String s:names){
										dlm.addElement(s);
									}
									SendMessageToAll(msg);//发送给所有人
								}else if(type.equals(MessageType.Chat)){
									String msgContent = msg.getContent();
									if(msgContent.split(":").length == 2 && msgContent.split(":")[0].equals("publickey")) {
										String participant = msgContent.split(":")[1];
										//String me = user.getName();
										String participantKey = "";
										//String mineKey = "";
										for(String key: pulicKeyList) {
											String[] keyInfo = key.split(":");
//											if(me.equals(keyInfo[0])) {
//												mineKey = keyInfo[1];
//											}
											if(participant.equals(keyInfo[0])) {
												participantKey = keyInfo[1];
											}
										}
										//String mString = "publickey:"+user.getName()+":"+mineKey+":"+participant+":"+participantKey;
										String mString = "publickey:"+participant+":"+participantKey;
										msg.setContent(mString);
										SendToPersonal(msg);
									}else {
										// sendDESKeys:encryptedDESkeys
										String content = msg.getFrom()+":"+msg.getContent();
										// from sd-sd-sd hh mm ss\015\012send
										msg.setContent(content);
										if(msg.getChatState().equals(ChatState.Personal)){
											SendToPersonal(msg);
										}else{
											SendMessageToAll(msg);
										}
									}
										
								}else if(type.equals(MessageType.Logout)){
									synchronized (this) {
										names.remove(user.getName());
										users.remove(user);
										count--;
										onlineNum.setText("当前在线人数："+count);
										dlm.removeAllElements();
										for(String s:names){
											dlm.addElement(s);
										}
										String content = "【"+msg.getFrom()+"】  "+"下线啦.【"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"】";
										msg.setContent(content);
										msg.setNames(names);
										SendMessageToAll(msg);
										flag=false;
									}					
								}
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					
					}

					private synchronized void SendMessageToAll(Message msg) throws Exception{
						for(UserInfo s : users){
								ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
								oos.writeObject(msg);
								oos.flush();
						}
					}
					private synchronized void SendToPersonal(Message msg)throws Exception{
						int times=0;
						for(UserInfo s:users){
							if(s.getName().equals(msg.getTo())||s.getName().equals(msg.getFrom())){
								ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
								oos.writeObject(msg);
								oos.flush();
								times++;
								if(times==2)break;
							}
						}
					}
					
				}.start();
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		try{
			String cmd=e.getActionCommand();
			System.out.println(cmd);
			if(cmd.equals("启动")){
				Confirm.setEnabled(false);
				quit.setEnabled(true);
				go();				
			}else if(cmd.equals("停止")){
				stop();
				quit.setEnabled(false);
				Confirm.setEnabled(true);
			}
		}catch(Exception ex){
			
		}
	}
	private void stop() throws Exception{
		if(ss!=null)
			if(!ss.isClosed())
				ss.close();
		run=false;
		String content = "服务器终止服务了.【"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"】";
		Message stopMsg=new Message(null, content, MessageType.Logout);
		for(UserInfo s:users){
			stopMsg.setFrom(s.getName());
			ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
			oos.writeObject(stopMsg);
			oos.flush();
		}
	}
	public static void main(String[] args) throws Exception {
		new Server();
	}
}