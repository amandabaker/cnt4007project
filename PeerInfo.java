import java.util.*;
import java.lang.*;
import java.net.*;
import java.nio.*;

public class PeerInfo {

	private final int theirPeerID;
	private BitSet theirBitField;
	private final String theirHost;
	private final Socket theirSocket; //where we interact with this peer
	private final int theirPort;
	private boolean theirInterestInMe;
	private boolean myInterestInThem;
	private boolean theirChoked;	//haha bad grammar/code theme pun
	private boolean theirOptimistic;	//lol same as above, got em again!


	//only values that will be read from PeerConfig need to be in constructor
	public PeerInfo(int id, String host, Socket socket, int port) {
		this.theirPeerID = id;
		this.theirHost = host;
		this.theirSocket = socket;
		this.theirPort = port;
	}

	/* Getters */
	public int getPeerId() {
		return theirPeerID;
	}

	public BitSet getBitField() {
		return theirBitField;
	}

	public String getHost() {
		return theirHost;
	}

	public Socket getSocket() {
		return theirSocket;
	}

	public int getPort() {
		return theirPort;
	}

	public boolean getTheirInterest() {
		return theirInterestInMe;
	}
	
	public boolean getMyInterest() {
		return myInterestInThem;
	}
	public boolean getChoked() {
		return theirChoked;
	}
	public boolean getOptimistic() {
		return theirOptimistic;
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