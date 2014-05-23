import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

	ServerSocket serverSocket = null;
	Socket clientSocket = null;
	DataOutputStream dos = null;
	final int LISTEN_PORT = 31200;
	private boolean listening = true;
	public ArrayList<ChatThread> clientConnections = new ArrayList<ChatThread>();
	ConcurrentHashMap<String, InetSocketAddress> clientMap = new ConcurrentHashMap<String, InetSocketAddress>();
	DataInputStream inFromClient;
	String username;
	byte[] ip;
	int portNumber;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new Server();

	}

	/**
	 * @throws IOException
	 * This create the initial server.
	 */
	public Server() throws IOException {
		serverSocket = new ServerSocket(LISTEN_PORT);
		// Enter infinite loop to listen for clients
		listenForClients();

		// Close the socket (unreachable code)
		// serverSocket.close();
	}

	/**
	 * This is listening for any connection
	 */
	private void listenForClients() {
		// TODO Auto-generated method stub
		while (listening) {

			// Create new thread to hand each client.
			// Pass the Socket object returned by the accept
			// method to the thread.
			try {
				new TCPListenThread(serverSocket.accept()).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Display socket information.
			// displayContactInfo();
		}

	}

	/**
	 * This display the client that has been connected.
	 */
	protected void displayContactInfo() {
		try {
			// Display contact information.
			System.out.println("Number Server standing by to accept Clients:"
					+ "\nIP Address: " + InetAddress.getLocalHost()
					+ "\nPort: " + serverSocket.getLocalPort() + "\n\n");

		} catch (UnknownHostException e) {
			// NS lookup for host IP failed?
			// This should only happen if the host machine does
			// not have an IP address.
			e.printStackTrace();
		}

	} // end displayContactInfo

	class TCPListenThread extends Thread
	// Written by Dr. Bachman
	// Edited by Pob V.
	{
		// Handle for the Socket used for communicating with a
		// client.
		protected Socket socket = null;

		// Integer ID for the client
		protected int clientNumber;

		private DataInputStream inFromClient;

		/*
		 * Sets up class data members. Diplays comuunication info.
		 */
		public TCPListenThread(Socket socket) {
			// Call the super class (Thread) constructor.
			super("TCPListenThread_");

			// Save a reference to the Socket connection
			// to the client.
			this.socket = socket;

			// displayClientInfo();

		} // end TCPListenThreadconstructor

		/*
		 * Displays IP address and port number information associated with a
		 * particular client.
		 */
		protected void displayClientInfo() {
			// Display IP address and port number client is using.
			System.out.println("Client " + clientNumber + " IP address: "
					+ socket.getInetAddress() + "\nClient Port number: "
					+ socket.getPort());

		} // end displayClientInfo

		/**
		 * Overides run method of the Thread class. Create appropriate stream
		 * objects for receiving numeric data from a client. Closes out the
		 * Socket connection after all data has been received.
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try {
				inFromClient = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			int number = 0;
			try {
				number = inFromClient.readInt();
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

			// ///////////////Register user
			try {
				if (number == 1) {
					registerUser();
					System.out.println("you got here1");
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// ///////////////Asking for list

			try {
				if (number == 2) {
					System.out.println("you got here2");
					giveList();
				}

			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			try {
				// Close the Socket connection with the client.
				socket.close();

			} catch (IOException e) {
				System.err.println("Error in communication with client "
						+ clientNumber);
				e.printStackTrace();
			}
		}// end run

		/**
		 * @throws IOException
		 * This method give back the list to the user, using a concurrent hashmap.
		 */
		private void giveList() throws IOException {
			int size = clientMap.size();
			dos.writeInt(size);
			for (Enumeration<String> e = clientMap.keys(); e.hasMoreElements();) {
				String sName = e.nextElement();
				dos.writeUTF(sName);
				byte[] add = clientMap.get(sName).getAddress().getAddress();
				dos.write(add);
				dos.writeInt(clientMap.get(sName).getPort());
			}
		}

		/**
		 * @throws IOException
		 * This put the information of the user in the concurrent hash map.
		 */
		private void registerUser() throws IOException {
			username = inFromClient.readUTF();
			ip = new byte[4];
			inFromClient.read(ip);
			portNumber = inFromClient.readInt();
			InetAddress ia = InetAddress.getByAddress(ip);
			clientMap.put(username, new InetSocketAddress(ia, portNumber));
			System.out.println("You added your user!");
			System.out.println("-----------------------");
			System.out.println(clientMap.toString());
		}

	} // end TCPListenThread class

}
