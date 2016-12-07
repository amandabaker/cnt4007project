import java.util.*;
import java.lang.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.io.*;

public class PeerInfo {

	private final 	int 	theirPeerID;
	private 		BitSet 	theirBitField;
	private final 	String 	theirHost;
	private 		Socket 	theirSocket; //where we interact with this peer
	private final 	int 	theirPort;
	private 		boolean theirInterestInMe;
	private 		boolean myInterestInThem;
	private 		boolean theirChoked;	//haha bad grammar/code theme pun
	private 		boolean theirOptimistic;	//lol same as above, got em again!
	private static 	Path 	logFile;
	private static  Path 	directory;


	//only values that will be read from PeerConfig need to be in constructor
	public PeerInfo(int id, String host, int port, boolean hasFile, int nPieces) {
		this.theirPeerID 		= id;
		this.theirHost 			= host;
		this.theirPort 			= port;
		this.theirInterestInMe 	= false;
		this.myInterestInThem 	= false;
		this.theirChoked 		= false;
		this.theirOptimistic 	= false;
		
		if (hasFile) {
			this.theirBitField 	= new BitSet(nPieces);
			this.theirBitField.set(0, nPieces);
		}

		String nameLogFile 	= "log_peer_" + Integer.toString(theirPeerID) + ".log";
		String nameDirectory= "peer_" + Integer.toString(theirPeerID);
		
		this.logFile 	= Paths.get (nameLogFile);
		this.directory 	= Paths.get (nameDirectory);

		// Create log file
		try {
			Files.createFile (logFile);
		} catch (IOException f) {
			System.out.println("The log file for " + Integer.toString(theirPeerID) + " already exists. \n" +
							   "The old file will be appended with new data.");
		} catch (Exception e) {
			System.out.println(e);
		}

		// Create storage directory
		try {
			Files.createDirectory (directory);
		} catch (IOException f) {
			System.out.println("The log directory for " + Integer.toString(theirPeerID) + " already exists. \n" + 
							   "The old directory will be kept and any new files added here.");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/* Getters */
	public int getPeerId () {
		return theirPeerID;
	}

	public BitSet getBitField () {
		return theirBitField;
	}

	public String getHost () {
		return theirHost;
	}

	public Socket getSocket () {
		return theirSocket;
	}

	public int getPort () {
		return theirPort;
	}

	public boolean getTheirInterest () {
		return theirInterestInMe;
	}
	
	public boolean getMyInterest () {
		return myInterestInThem;
	}
	public boolean getChoked () {
		return theirChoked;
	}

	public boolean getOptimistic () {
		return theirOptimistic;
	}

	public Path getLogFile () {
		return logFile;
	}

	public Path getDirectory () {
		return directory;
	}


	/* Setters */

	public void setTheirBitField(BitSet a) {
		theirBitField = a;
	}

	public void setTheirInterest(boolean a) {
		theirInterestInMe = a;
	}

	public void setMyInterest(boolean a) {
		myInterestInThem = a;
	}

	public void setChoked(boolean a) {
		theirChoked = a;
	}

	public void setOptimistic(boolean a) {
		theirOptimistic = a;
	}	
}