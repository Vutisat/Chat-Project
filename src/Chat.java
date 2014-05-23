import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;


public class Chat implements ChatPeerInterfaceListener {

	String username;
	ArrayList<ChatThread> clientConnections;
	public Socket socket;
	public Socket registerSocket;
	public ServerSocket chatServerSocket;
	InetAddress serverIp;
	final int PORT_NUMBER = 31200;
	public DataInputStream disReg;
	public DataOutputStream dosReg;
	public DataInputStream dis;
	public DataOutputStream dos;
	final String homeAddress = "127.0.0.1";
	int myPortNumber;
	ChatPeerInterface cpi;
	int sizeOfList = 1;
	private HashMap<String, InetSocketAddress> ht = new HashMap<String, InetSocketAddress>();
	Socket chatSocket = null;
	ArrayList<ChatThread> chatThreadArr = new ArrayList<ChatThread>();

	/**
	 * @throws IOException This is the constructor that calls everything to run.
	 */
	public Chat() throws IOException {
		askForName();

		serverIp = InetAddress.getByName(homeAddress);
		clientConnections = new ArrayList<ChatThread>();
		createChatSocket();
		
		connectRegisterToServer();
		createRegisterClientStreams();
		registerName();
		
		
		cpi = new ChatPeerInterface(this, username);
		
		do{
			//listenForClients();
			chatThreadArr.add(new ChatThread(username, chatServerSocket.accept()));
		}
		while(true);

	}

	
	/**
	 * @throws IOException this create the streams to update the list of friends
	 */
	private void createUpdateClientStreams() throws IOException {
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}

	// Create a server socket and store the port number
	private void createChatSocket() throws UnknownHostException, IOException {
		chatServerSocket = new ServerSocket(0);
		
		myPortNumber = chatServerSocket.getLocalPort();
	}

	/**
	 * This asks for the screen name of the user.
	 */
	public void askForName() {
		username = JOptionPane.showInputDialog("Please enter your username");
	}

	/**
	 * This connect to the sever to register
	 */
	public void connectRegisterToServer() {
		try {
			registerSocket = new Socket(serverIp, PORT_NUMBER);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Connect to update the list of friend list.
	 */
	public void connectUpdateToServer() {
		try {
			socket = new Socket(serverIp, PORT_NUMBER);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @throws IOException This method create the streams to register the user to the server
	 */
	public void createRegisterClientStreams() throws IOException {
		dosReg = new DataOutputStream(registerSocket.getOutputStream());
		disReg = new DataInputStream(registerSocket.getInputStream());
	}

	/**
	 * This method send all the information of the user to register in the server to be online
	 */
	public void registerName() {
		try {
			dosReg.writeInt(1);
			dosReg.writeUTF(username);
			byte[] addr = (chatServerSocket.getInetAddress().getAddress());
			dosReg.write(addr);
			dosReg.writeInt(myPortNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			dosReg.close();
			disReg.close();
			registerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param Args
	 * @throws IOException
	 * Main method simply create chat object
	 */
	public static void main(String[] Args) throws IOException {
		new Chat();
	}

	/**
	 * This is listening for anyone who's trying connect to chat.
	 */
	public void listenForClients(){
		try {
			chatSocket = chatServerSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	/* (non-Javadoc)
	 * @see ChatPeerInterfaceListener#contactFriend(java.lang.String, int)
	 */
	@Override
	public void contactFriend(String friendName, int friendIndex) {
		
		InetSocketAddress friendToContact = (ht.get(friendName));
		try {
			chatSocket = new Socket(friendToContact.getAddress(), friendToContact.getPort());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		chatThreadArr.add(new ChatThread(username, chatSocket));
	}

	/* (non-Javadoc)
	 * @see ChatPeerInterfaceListener#quit()
	 * This method shuts down the peer server.
	 */
	@Override
	public void quit() {
		try {
			chatSocket.close();
			chatServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see ChatPeerInterfaceListener#updateFriendList()
	 * This method update the list to see who is online. The integer 2 is being sent to let the server know this method is being called.
	 * This method called is automatically during a time interval. 
	 */
	@Override
	public void updateFriendList() {
		cpi.clearList();
		// fht.clear();
		connectUpdateToServer();
		try {
			createUpdateClientStreams();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Getting the list
		try {
			dos.writeInt(2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			sizeOfList = dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("whaddup");

		//This gets the information and put it in the hashMap for access.
		for (int i = 0; i < sizeOfList; i++) {
			byte[] add = new byte[4];
			String sName;
			try {
				sName = dis.readUTF();

				dis.read(add);
				InetAddress buddy = InetAddress.getByAddress(add);
				int port = dis.readInt();
				InetSocketAddress buddyInfo = new InetSocketAddress(buddy, port);
				if (!sName.equals(username)) {
					cpi.addFriendToList(sName, i);
					System.out.println("sName is : " + sName);
					System.out.println("buddyInfo is: " + buddyInfo.toString());
					ht.put(sName, buddyInfo);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Closing all the streams that is no longer in used.
		try {
			dis.close();
		dos.close();
		socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("HashMAp table at the moment: " + ht.toString());
		// cpi.addFriendToList(friendName, friendIndex)

	}
}
