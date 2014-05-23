
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * @author bachmaer
 * 
 * Number server for CSA 283 lab one. Uses a ServerSocket to 
 * listen for TCP connection requests from clients. Instantiates
 * a TCPListenThread object to handle each client. 
 *
 * Created on Feb 4, 2004
 */
public class TCPListen
{

	// Socket for listening for clients
	ServerSocket serverSocket = null;

	// Counter for tracking number of clients
	int clientCount = 1;

	// Port number for server to listen on
	final int LISTEN_PORT = 32100;

	// Flag for controlling listening loop
	// (Could be set to false to cause the server to stop
	// listening for clients
	boolean listening = true;


	/*
	 * Constructor for the TCPListen class. It creates a 
	 * ServerSocket to use for listening for clients. Displays
	 * the port number and IP address for the ServerSocket to the
	 * console and then enters an infinite loop to listen for 
	 * clients. Spawns a new TCPListenThread object to handle 
	 * each client.
	 */
	public TCPListen() throws IOException
	{
		try {

			// Instantiate socker to listening
			serverSocket = new ServerSocket(LISTEN_PORT);
		}
		catch (IOException e) {
			
			// Port not available
			System.err.println("Could not listen on port: " 
					+ LISTEN_PORT);
			System.exit(-1);
		}

		// Display socket information.
		displayContactInfo();
		
		// Enter infinite loop to listen for clients
		listenForClients();
		
		// Close the socket (unreachable code)
		serverSocket.close();

	} // end TCPListen constructor
	
	
	/*
	 * Diplays IP address and port number that clients can use to
	 * contact the server to the console.
	 */
	protected void displayContactInfo()
	{
		try {
			// Display contact information.
			System.out.println( 
				"Number Server standing by to accept Clients:"			
				+ "\nIP Address: " + InetAddress.getLocalHost() 
				+ "\nPort: " + serverSocket.getLocalPort() 
				+ "\n\n" );				
			
		} catch (UnknownHostException e) {
			// NS lookup for host IP failed? 
			// This should only happen if the host machine does 
			// not have an IP address.
			e.printStackTrace();
		}
	
	} // end displayContactInfo
	
	
	/*
	 * Infinite loop for listing for new clients. Spawns a new
	 * TCPListenThread object to handle each client.
	 */
	protected void listenForClients() throws IOException
	{
		while (listening) {
		
			// Create new thread to hand each client.
			// Pass the Socket object returned by the accept 
			// method to the thread.
			new TCPListenThread(serverSocket.accept(), clientCount ).start();
				
			clientCount++;
		}
				
	} // end listenForClients

	
	/*
	 * Instantiates a Server the listens for clients that are
	 * going to send a series of numbers.
	 */
	public static void main(String[] args) throws IOException 
	{
		// Instantiate Server
		new TCPListen();

	} // end main


} // end TCPListen class


/*
 * Class that defines a runnable object (extends Thread) that can
 * handle a single client. Expects to receive eight number of
 * various types in the following order:
 * 		- two ints
 * 		- two floats
 * 		- two doubles
 * 		- two longs
 */
class TCPListenThread extends Thread
{
	// Handle for the Socket used for communicating with a 
	// client.
	protected Socket socket = null;
	
	// Integer ID for the client
	protected int clientNumber;
	
	
	/*
	 * Sets up class data members. Diplays comuunication info.
	 */
	public TCPListenThread(Socket socket, int clientNumber )
	{
		// Call the super class (Thread) constructor.
		super("TCPListenThread_" + clientNumber);

		// Save a reference to the Socket connection 
		// to the client.
		this.socket = socket;
		
		// Save the ID for the client
		this.clientNumber = clientNumber;

		displayClientInfo();

	} // end TCPListenThreadconstructor
	
	
	/* Displays IP address and port number information 
	 * associated with a particular client.
	 */
	protected void displayClientInfo()
	{	
		// Display IP address and port number client is using.
		System.out.println( "Client " + clientNumber 
				+ " IP address: " + socket.getInetAddress()
				+ "\nClient Port number: " + socket.getPort() );
		
	} // end displayClientInfo


	/**
	 * Overides run method of the Thread class. Create 
	 * appropriate stream objects for receiving numeric data
	 * from a client. Closes out the Socket connection after
	 * all data has been received.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		try {

			// Instantiate a DataInputStream object around the
			// inputStream associated with the Socket object
			// being used to communicate with the client.
			DataInputStream inFromClient = 
				new DataInputStream( socket.getInputStream() );

			// Read in the numbers being sent by the client and
			// display them on the console.
			
			// Read two ints
			System.out.println("int 1 = " 
					+ inFromClient.readInt() );
			System.out.println("int 2 = " 
					+ inFromClient.readInt() );

			// Read two floats
			System.out.println("float 1 = " 
					+ inFromClient.readFloat() );
			System.out.println("float 2 = " 
					+ inFromClient.readFloat() );

			// Read two doubles
			System.out.println("double 1 = " 
					+ inFromClient.readDouble() );
			System.out.println("double 2 = " 
					+ inFromClient.readDouble() );
			
			// Read two longs
			System.out.println("long 1 = " 
					+ inFromClient.readLong() );
			System.out.println("long 2 = " 
					+ inFromClient.readLong() );
			
			// Display closing message.
			System.out.println("All data received.\n"
					+ "Closing connection. Bye\n\n");		

			// Close the Socket connection with the client.
			socket.close();

		}
		catch (IOException e) {
			System.err.println( 
					"Error in communication with client " 
					+ clientNumber );
			e.printStackTrace();
		}

	}// end run		


} // end TCPListenThread class
