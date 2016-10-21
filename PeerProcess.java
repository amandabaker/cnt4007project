import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.math.BigInteger;

public class PeerProcess extends Thread {

	final int CHOKE 			= 0; 
	final int UNCHOKE 			= 1;
	final int INTERESTED 		= 2;
	final int NOT_INTERESTED 	= 3;
	final int HAVE 				= 4;
	final int BITFIELD 			= 5;
	final int REQUEST 			= 6;
	final int PIECE 			= 7;
	
	private int peerID;						//id for this peer
	private Socket requestSocket;           //socket to connect to the server
	public ServerSocket listener;			//will fix privacy later

	public PeerProcess(int peerID) {
		
		this.peerID = peerID;
	}

	public void Run() {
		
		try {
			//initialize me captain
			startupPeer(peerID);

		} catch(Exception e) {
			
			//ostrich algorithm approach to exceptions at its finest
			System.out.println("woops");
		
		}
	}

	private void startupPeer(int peerID) {
		
		// Hardcoding first peer to start for now
		if (peerID == 1001) {
			
			//spin up a server socket, port hardcoded for now
			spinServer(8080);

		} else {
			
			//lets make some friends
			openConnection();
			//TODO:Add arguments to openConnection
			//openConnection("localhost", 8000);
			
		}
	}

	private void spinServer(int port) {


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

	private void openConnection() { // TODO: String dest, int port) {

		//create a socket to connect to the server

		/*
		TODO
		requestSocket = new Socket(dest, port);
		System.out.println("Connected to " + dest + " in port " + port);
		*/

		//requestSocket = new Socket("localhost", 8000);
		try {
			requestSocket = new Socket("66.231.144.240", 8080);
		} 
		catch(Exception e) {
			System.out.println("exception handling coming in v0.2!!!");
		}
	}

	// Check the type of the recieved message and act accordingly
	void messageType(Message msg) 
	{
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

	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}

}





