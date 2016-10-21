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
		} catch(Exception e) {
			System.out.println("exception handling coming in v0.2!!!");
		}
			


	}
}





