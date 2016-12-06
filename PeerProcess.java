import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.math.BigInteger;

public class PeerProcess implements Runnable {

	/* Message types */
	final int CHOKE 			= 0; 
	final int UNCHOKE 			= 1;
	final int INTERESTED 		= 2;
	final int NOT_INTERESTED 	= 3;
	final int HAVE 				= 4;
	final int BITFIELD 			= 5;
	final int REQUEST 			= 6;
	final int PIECE 			= 7;

	/* common properties from Common.cfg */
	private int fileSize;
	private int numberOfPerferredNeighbors;
	private int optimisticUnchokingInterval;
	private int pieceSize;
	private String fileName;

	/* unique peer properites */
	//todo: peers
	private int port;
	private String host;
	private boolean gotFile;
	//private Bitfield bitfield

	private int peerID;						//id for this peer
	private byte [] MESSAGE;
	private String message;
	private Socket requestSocket;           //socket to connect to the server
	public ServerSocket listener;			//will fix privacy later
	ObjectOutputStream out;         		//stream write to the socket
	ObjectInputStream in;          			//stream read from the socket

	/* Constructor */
	public PeerProcess(int peerID) {
		this.peerID = peerID;
	}
    
	/* Method run by each thread */
	public void run() {	
		System.out.println("test1");
		try {
			//initialize me captain
			startupPeer(peerID);
			System.out.println("test2");
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);//("66.231.144.240", 8080);//
			System.out.println("test3");
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("test4");
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			//first, send a handshake with the peer ID
			handshake(1010);	//placeholder peer id
			//Receive the handshake from the server
			MESSAGE = (byte [])in.readObject();
			//check whether the handshake is valid and if not, no further connection
			boolean hc = handshakeCheck(MESSAGE);
			System.out.println("handshake check: " + hc);
			//show the message to the user
			System.out.println("Handshake message: " + MESSAGE);
			//while handshake is valid, send messages
			while(hc)
			{
				System.out.print("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				//translate to bytes --temporary

				//translate from string to byte
				byte [] bmessage = message.getBytes();
				
				//Send the sentence to the server
				sendMessage(bmessage);
				//Receive the upperCase sentence from the server
				MESSAGE = (byte[])in.readObject();
				//show the message to the user
				//translate from byte to string
				String bytestring = new String(MESSAGE, StandardCharsets.UTF_8);
				
				System.out.println("Receive message: " + bytestring);
			}

		} catch(Exception e) {
			
			//ostrich algorithm approach to exceptions at its finest
			System.out.println("woops");
		
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

	/* Startup as server or client */
	public void startupPeer(int peerID) {
		
		// Hardcoding first peer to start for now
		if (peerID == 1001) {
		
			//spin up a server socket, port hardcoded for now
			spinServer(8000);

		} else {
			
			//lets make some friends
			openConnection();
			//TODO:Add arguments to openConnection
			//openConnection("localhost", 8000);
			
		}
	}

	/* Spin up server */
	void spinServer(int port) {

		int serverPort = port;

		try {
			//start listening at that port
			listener = new ServerSocket(serverPort);
		
		} catch(Exception e) {
			
			System.out.println("We'll do the error handling later");

		}

		//Using example code for now, will refactor to make it better
		int clientNum = 1;
    	try {
        		while(true) {
        			try {
        				new Handler(listener.accept(),clientNum).start();
        			} catch(Exception e) {
        				System.out.println("Error handling (and more!) to come");
        			}
            		
					System.out.println("Client "  + clientNum + " is connected!");
					clientNum++;
    			}
    	} finally {
    		try {
        		listener.close();
    		} catch(Exception e) {
    			System.out.println("Add this in later too");
    		} 
    	}
	
	}

	/* Spin up client? */
	void openConnection() { // TODO: String dest, int port) {

		//create a socket to connect to the server

		/*
		TODO
		requestSocket = new Socket(dest, port);
		System.out.println("Connected to " + dest + " in port " + port);
		*/

		//requestSocket = new Socket("localhost", 8000);
		try {
			//requestSocket = new Socket("66.231.144.240", 8080);
			requestSocket = new Socket("localhost", 8000);
		} 
		catch(Exception e) {
			System.out.println("exception handling coming in v0.2!!!");
		}
	}

	/* Send message to OutputStream */
	void sendMessage(byte[] msg) {
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	/* Perform handshake */
	void handshake(int peerid) {
		//convert integer peerID (can change to byte[] if more convenient) to string
		String pid = Integer.toString(peerid);
		//add peer id to end of handshake message
		String hmsg = "P2PFILESHARINGPROJ0000000000" + peerid;
		
		//for debugging purposes
		//System.out.println("peerid: " + peerid);
		//System.out.println("hmsg: " + hmsg);
		//hmsg = "12";
		//translate from string to byte
		byte [] b = hmsg.getBytes();
		
		//System.out.println("header bytes: " + b);
		
		//System.out.println("header bytes: " + Integer.toBinaryString(b));
		
		//translate from byte to string
		String bytestring = new String(b, StandardCharsets.UTF_8);
		System.out.println("stringfrombytes: " + bytestring);

		//send handshake message
		String msg = hmsg;
		try{
			//stream write the message
			out.writeObject(b);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	/* Validate handshake */
	boolean handshakeCheck(byte[] hcheck) {
		//check whether handshake was successful and connected peer is valid
		//proper header string
		String head = "P2PFILESHARINGPROJ0000000000";
		//transform to bytes
		byte [] phb = head.getBytes();
		//byte array for header and zero bits of handshake
		byte[] header = new byte[28]; 
		for(int i = 0; i < 28; i++)
		{
			header[i] = hcheck[i];
		}
		//for debugging	
		//translate from byte to string
		String headstring = new String(header, StandardCharsets.UTF_8);
		System.out.println("header byte2string: " + headstring);
		//byte array for peer id bytes of handshake
		byte[] peerID = new byte[4]; 
		for(int i = 28; i < 32; i++)
		{
			peerID[i-28] = hcheck[i];
		}
		//convert peerID to string
		String mid = new String(peerID, StandardCharsets.UTF_8);
		//convert string to integer for easy checking
		int intpid = Integer.parseInt(mid);
		//pretty roundabout, may change later
		//for debugging peer id
		System.out.println("peerid byte2int: " + intpid);
		//check if it is equal to 'P2PFILESHARINGPROJ0000000000'
		//byte[] hzero = {}; //insert correct things here
		if (!head.equals(headstring)) 
		{
			//terminate connection

			System.out.println("Handshake header invalid!");
			return false;
		}
		//else check to see if peerID is expected
		else if (intpid < 0)	//temp
		{
			//otherwise terminate connection
			System.out.println("Peer ID invalid!");
			return false;
		}
		//passing all checks returns true so connection can continue
		else
			return true;
		
	}

	/* Check the type of the recieved message and act accordingly */
	void messageType(Message msg) {
		//check message type bit
		int type = msg.getType();

		if (type == CHOKE) 
		{
			// choke
		}
		else if (type == UNCHOKE)
		{
			// unchoke
			// send requests!
		}
		else if (type == INTERESTED) 
		{
			//interested
		}
		else if (type == NOT_INTERESTED) 
		{
			//not interested
		}
		else if (type == HAVE) 
		{
			//have
		}
		else if (type == BITFIELD) 
		{
			//bitfield
		}
		else if (type == REQUEST) 
		{
			//request
		}
		else if (type == PIECE)
		{
			//piece
		}
	}

	/* Send choke message */
	void sendChoke () {
		Message msg = new Message();
		msg.setLength(4);
		msg.setType(CHOKE);
		msg.setPayload(null);
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	/* Send unchoke message */
	void sendUnchoke () {
		Message msg = new Message();
		msg.setLength(4);
		msg.setType(UNCHOKE);
		msg.setPayload(null);
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send interested message */
	void sendInterested () {
		Message msg = new Message();
		msg.setLength(4);
		msg.setType(INTERESTED);
		msg.setPayload(null);
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send not interested message */
	void sendNotInterested () {
		Message msg = new Message();
		msg.setLength(4);
		msg.setType(NOT_INTERESTED);
		msg.setPayload(null);
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send have message */
	void sendHave (int pieceIndex) {
		byte[] bPieceIndex = new byte [4];
		// Convert pieceIndex from int to byte[]
		for (int i=bPieceIndex.length-1; i>=0; i--) {
			bPieceIndex[i] = (byte)(pieceIndex % 12);
			pieceIndex /= 12;
		}

		Message msg = new Message();
		msg.setLength(8);
		msg.setType(HAVE);
		msg.setPayload(bPieceIndex);
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send bitfield message */
	void sendBitfield () {
		// has payload: complicated
		Message msg = new Message();
		msg.setLength(0);			// MAKE LENGTH MAKE SENSE
		msg.setType(BITFIELD);
		msg.setPayload(null);		// ADD BITFIELD CONTENT
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send request message */
	void sendRequest (int pieceIndex) {
		byte[] bPieceIndex = new byte [4];
		// Convert pieceIndex from int to byte[]
		for (int i=bPieceIndex.length-1; i>=0; i--) {
			bPieceIndex[i] = (byte)(pieceIndex % 12);
			pieceIndex /= 12;
		}

		Message msg = new Message();
		msg.setLength(8);
		msg.setType(REQUEST);
		msg.setPayload(bPieceIndex);
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send piece message */
	void sendPiece (int pieceIndex) {
		// ADD PIECE CONTENT
		byte[] bPieceIndex = new byte [4];
		// Convert pieceIndex from int to byte[]
		for (int i=bPieceIndex.length-1; i>=0; i--) {
			bPieceIndex[i] = (byte)(pieceIndex % 12);
			pieceIndex /= 12;
		}

		Message msg = new Message();
		msg.setLength(4);				// SET LENGTH APPROPRIATELY 
		msg.setType(PIECE);
		msg.setPayload(bPieceIndex); 	// AND PIECE CONTENT!
		try {
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Get message length */
	int messageLength(byte[] ml) {
		byte[] meslen = new byte[4];
		for (int i = 0; i<5; i++) {
			meslen[i] = ml[i]; 
		}
		//returns integer value of first four bits of byte message
		return new BigInteger(meslen).intValue();
		
	}

	/* Main Method */
	public static void main(String args[]) {
		
		PeerProcess peer = new PeerProcess(Integer.parseInt(args[0]));
		//spin a thread and start the peer
		Thread peer_thread = new Thread(peer);
		peer_thread.start();

		//I left this in case anyone was still using client, but it works all inside PeerProcess now
		//Client client = new Client();
		//client.run();
	}

	// Reading from files (need to create the file structure too, peer_[id])
	/*
	// ----- TODO: Read Common.cfg

	// ----- TODO: Read PeerInfo.cfg
	FileReader fileReader = new FileReader("PeerInfo.cfg");
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	
	String line = null;
	String peerID = "";
	String hostname = "";
	String port = "";
	String hasFile = "";
	//int sPort = 8000;
	int i = 0;
	
	while ((line = bufferedReader.readLine()) != null) {
		System.out.println(line + "\n");
		listenerArr[i] = new ServerSocket(sPort++
		);
		StringTokenizer tokens = new StringTokenizer(line);
		if (tokens.countTokens() < 4) {
			// throw too few tokens
		} else if (tokens.countTokens() > 4) {
			// throw too many tokens
		}
		peerID = tokens.nextToken();
		hostname = tokens.nextToken();
		port = tokens.nextToken();
		hasFile = tokens.nextToken();
		System.out.println("almost new\n");
		while (true) {
			new Handler (listenerArr[i++].accept(),peerID, hostname).start();
			System.out.println("end\n");
		}
	}*/

}





