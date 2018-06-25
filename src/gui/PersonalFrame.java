package gui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;

import utils.DESUtils;
import utils.MyKeyPair;
import utils.MyPrivateKey;
import utils.MyPublicKey;
import utils.RSAHelper;


public class PersonalFrame extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	//Frame Name
	private String From;
	private String name;
	//PersonalFrame
	private JPanel PCenter = new JPanel();
	private JTextArea PShowMsg = new JTextArea(10,20);
	private JScrollPane PShowPane = new JScrollPane(PShowMsg);
	private JPanel PoperPane = new JPanel();
	private JLabel Pinput = new JLabel("请输入:");
	private JTextField PmsgInput = new JTextField(28);
	private JButton Psend = new JButton("send");	
	// 数据加密部分
	private String desKey = "";

	private MyPublicKey friendPublicKeys;
	public PersonalFrame(String From,String name,MyPublicKey friendPublicKeys,String desKey){
		this.desKey = desKey;
		this.friendPublicKeys = friendPublicKeys;
		this.From=From;
		this.name = name;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initPersonalFrame();
		initEvent();
		//Pinput.setText(name+":public key ="+friendPublicKeys.getN()+"\n"+friendPublicKeys.getB());
		System.out.println(name+":public key ="+friendPublicKeys.getN()+"\n"+friendPublicKeys.getB());
	}
	private void initPersonalFrame(){
		PmsgInput.setActionCommand("send");
		PShowMsg.setText("My DES key is: "+desKey);
		PShowMsg.setEditable(false);
		BorderLayout bl3 = new BorderLayout();
		PCenter.setLayout(bl3);
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		PoperPane.setLayout(fl);
		PoperPane.add(Pinput);
		PoperPane.add(PmsgInput);
		PoperPane.add(Psend);
		PCenter.add(PShowPane,BorderLayout.CENTER);
		PCenter.add(PoperPane,BorderLayout.SOUTH);
		add(PCenter,BorderLayout.CENTER);
		add(PCenter);
		setSize(450,300);
		setLocation(300,200);
		setResizable(false);
		setTitle("与"+name+"单独聊天中.....");
		setVisible(true);
	}
	private void initEvent(){
		 Psend.addActionListener(this);
		 PmsgInput.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		try{
			String cmd=e.getActionCommand();
			if(cmd.equals("send")){
				SendMessage();
			}
		}catch(Exception ex)
		{}
		
	}
	/**
	 * 发送数据到服务器
	 * @author Wangbin
	 * @time 2018年6月24日 下午4:27:01
	 */
	private void SendMessage()throws Exception{
		//判断是否连接了服务器
		if(Client.socket == null){
			JOptionPane.showMessageDialog(this, "没有连接服务器或已经断开服务器");
			return;
		}
		//发送消息
		String content = PmsgInput.getText();
		if(content == null || content.trim().equals("")){
			JOptionPane.showMessageDialog(this, "不能发送空字符串");
			return;
		}else{
			//清空文本输入框
			PmsgInput.setText("");
		}
		if(content.equals("sendDESKeys")) {
			// 如果是发送DES key，则用RSA加密，然后再发送
			desKey = RSAHelper.encrypt(desKey, friendPublicKeys, "UTF-8");
			content +=":"+desKey;
		
		}else {
			// 如果是其他一般数据，则用DES加密，然后再发送
			content = Base64.encodeBase64String((DESUtils.encodeDES(desKey.getBytes(), content.getBytes())));
		}
		Message msg = new Message(From, content, MessageType.Chat, ChatState.Personal, name);
		Client.oos.write(From+"_"+content+"_1"+"_0"+"_"+name);
		Client.oos.flush();
	}
	/**
	 * 展示从服务器接受到的数据
	 * @author Wangbin
	 * @time 2018年6月24日 下午4:26:47
	 */
	public void showMsg(Message msg){
		String content = msg.getContent();				
		String old = PShowMsg.getText();
		
		if(old == null || old.trim().equals("")){
			PShowMsg.setText(content);
		}else{
			String temp = old+"\n"+content;
			PShowMsg.setText(temp);
		}
		PShowMsg.setCaretPosition(PShowMsg.getText().length());
	}
	
	public void showMsgAndChangeDES(Message msg,String des){
		this.desKey = des;
		String content = msg.getContent();				
		String old = PShowMsg.getText();
		if(old == null || old.trim().equals("")){
			PShowMsg.setText(content);
		}else{
			String temp = old+"\n"+content;
			PShowMsg.setText(temp);
		}
		PShowMsg.setCaretPosition(PShowMsg.getText().length());
	}
	public String getName(){
		return name;
	}
	@Override
	public void dispose(){
		Client.personalFrames.remove(this);
		super.dispose();
	}
}
