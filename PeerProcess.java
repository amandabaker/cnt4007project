import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.math.BigInteger;
import java.time.LocalTime;
import java.nio.file.*;

public class PeerProcess implements Runnable {

/* ---------- Initilize Variables ---------- */
	/* Message types */
	final 	int 		CHOKE 			= 0; 
	final 	int 		UNCHOKE 		= 1;
	final 	int 		INTERESTED 		= 2;
	final 	int 		NOT_INTERESTED 	= 3;
	final 	int 		HAVE 			= 4;
	final 	int 		BITFIELD 		= 5;
	final 	int 		REQUEST 		= 6;
	final 	int 		PIECE 			= 7;

	/* common properties from Common.cfg */
	private int 		numberOfPerferredNeighbors;
	private int 		unchokingInterval;
	private int 		optimisticUnchokingInterval;
	private String 		fileName;
	private int 		fileSize;
	private int 		pieceSize;
	private int 		nPieces;

	/* unique peer properites */
	private int 		port;
	private int 		nPeers;
	private String 		host;
	private boolean 	gotFile;
	private BitSet 		bitfield;
	private byte [] 	fileData;
	private PeerInfo []	peers;
	private Path 		logPath;
	private Path 		filePath;
	private Path 		directoryPath;

	private int 		peerID;				//id for this peer
	private byte [] 	MESSAGE;
	private String 		message;
	private Socket 		requestSocket;      //socket to connect to the server
	
	public ServerSocket listener;			//will fix privacy later
	ObjectOutputStream 	out;         		//stream write to the socket
	ObjectInputStream 	in;          		//stream read from the socket

/* ---------- End Initialize Variables ---------- */

	/* Constructor */
	public PeerProcess(int peerID) {
		this.peerID = peerID;
		configure();
	}
    
	/* Method run by each thread */
	public void run() {	
		
		//initialize me captain

		startupPeer(peerID); 
		//System.out.println("test1");
		//System.out.println("PeerID is: " + peerID);
		//configure();
		//initialize me captain
		//startupPeer(peerID);
		 /*
		try {
			configure();
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
			handshake(1002);	//placeholder peer id
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

		} catch (Exception e) {
			
			//ostrich algorithm approach to exceptions at its finest
			System.out.println("whoops");
		
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
		} */
	}

	/* Startup as server or client */
	public void startupPeer(int peerID) {
		// Hardcoding first peer to start for now
		if (peerID == 1001) {
		
			System.out.println("Server peer " + peerID + " started.");
			
			//spin up a server socket, port hardcoded for now
			spinServer(8000);

		} else {
			
			//lets make some friends
			openConnection();
			
			System.out.println("After openConnection: " + peerID);
			//create a socket to connect to the server
			//requestSocket = new Socket("localhost", 8000);//("66.231.144.240", 8080);//

			System.out.println("CONNECTING with " + peerID);
			for(int i = 0; i < nPeers; i++) {
				if(peers[i].getPeerId() == peerID) {
					//set the socket
					peers[i].setSocket(requestSocket);
					System.out.println("Peer " + peerID + " is done handshaking.");
					//if loop reaches this peer, break out of loop
					break;
				}
				else {
					//otherwise, connect with and handshake peer
					System.out.println("Connecting with peer " + peers[i].getPeerId() + "!");
					handshake(peers[i].getSocket());
				}
			}
			//TODO:Add arguments to openConnection
			//openConnection("localhost", 8000);
			
		}
	}

/* ---------- Configuration ---------- */

	/* Configures everything, including the 3 following functions */
	void configure () {
		String nameLogFile 	= "log_peer_" + peerID + ".log";
		String nameDirectory= "peer_" + peerID;
		
		logPath			= Paths.get (nameLogFile);
		directoryPath	= Paths.get (nameDirectory);

		try {
			Files.createFile (logPath);
		} catch (IOException f) {
			System.out.println("The log file for " + peerID + " already exists.");
		} catch (Exception e) {
			System.out.println(e);
		}

		// Create storage directory
		try {
			Files.createDirectory (directoryPath);
		} catch (IOException f) {
			System.out.println("The log directory for " + peerID + " already exists.");
		} catch (Exception e) {
			System.out.println(e);
		}

		configureGeneral();
		configurePeer();
	}

	/* Read Common.cfg */
	void configureGeneral () {
		try {
			FileReader fileReader = new FileReader("Common.cfg");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringTokenizer tokens;
			String line;

			line = bufferedReader.readLine();
			tokens = (line != null) ? new StringTokenizer(line) : null;
			numberOfPerferredNeighbors = (tokens != null && tokens.nextToken() != "") ? Integer.parseInt(tokens.nextToken()) : null;

			line = bufferedReader.readLine();
			tokens = (line != null) ? new StringTokenizer(line) : null;
			unchokingInterval = (tokens != null && tokens.nextToken() != "") ? Integer.parseInt(tokens.nextToken()) : null;

			line = bufferedReader.readLine();
			tokens = (line != null) ? new StringTokenizer(line) : null;
			optimisticUnchokingInterval = (tokens != null && tokens.nextToken() != "") ? Integer.parseInt(tokens.nextToken()) : null;

			line = bufferedReader.readLine();
			tokens = (line != null) ? new StringTokenizer(line) : null;
			fileName = (tokens != null && tokens.nextToken() != "") ? tokens.nextToken() : null;	

			line = bufferedReader.readLine();
			tokens = (line != null) ? new StringTokenizer(line) : null;
			fileSize = (tokens != null && tokens.nextToken() != "") ? Integer.parseInt(tokens.nextToken()) : null;

			line = bufferedReader.readLine();
			tokens = (line != null) ? new StringTokenizer(line) : null;
			pieceSize = (tokens != null && tokens.nextToken() != "") ? Integer.parseInt(tokens.nextToken()) : null;
		} catch (Exception e) {
			System.out.println(e);
		}
		// NOTE: THIS MIGHT NOT WORK
		filePath = Paths.get(fileName);
	}

	/* Read PeerProcess.cfg */
	void configurePeer () {
		// initialize variables
		nPieces 	= (int)Math.ceil(fileSize/pieceSize);
		bitfield 	= new BitSet(nPieces);
		fileData 	= new byte[fileSize];

		String 	line		= "";
		String 	hostname	= "";
		int 	port 		= 0;
		boolean hasFile 	= false;
		String 	peerIDstr 	= "";
		int 	peerId		= 0;
		try {
			nPeers 	= getNumPeers();
			peers 	= new PeerInfo[nPeers];

			FileReader fileReader 			= new FileReader("PeerInfo.cfg");
			BufferedReader bufferedReader 	= new BufferedReader(fileReader);

			System.out.println("created peers");

			int i 	= 0;
			
			peerIDstr 	= Integer.toString(peerID);
			while ((line = bufferedReader.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(line);
				System.out.println(tokens.countTokens());

				if (tokens.countTokens() == 4) {
					peerId 		= Integer.parseInt(tokens.nextToken());
					hostname 	= tokens.nextToken();
					port 		= Integer.parseInt(tokens.nextToken());
					hasFile 	= tokens.nextToken().charAt(0) == '1' ? true : false;
					// create new peer and add to array
					peers[i++] 	= new PeerInfo(peerId, hostname, port, hasFile, nPieces);
					if (peerId == peerID) {
						System.out.println(hasFile);
						if (hasFile) {
							// set bitfield to all ones
							bitfield.set(0,nPieces-1);

							// store data
							String file = "./peer_" + peerIDstr + "/" + fileName;
							try {
								FileInputStream fileInput = new FileInputStream(file);
								fileInput.read(fileData);
								System.out.println(fileData.toString());
							} catch (Exception e) {	 // if file doesn't exist
								System.out.println(e);
							}
						} 
					}
				} else {
					System.out.println("Bad input in PeerInfo.cfg");
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}		
	}

	/* Count number of peers in PeerInfo.cfg */
	int getNumPeers () {
		int i = 0;
		try {
			FileReader fileReader 			= new FileReader("PeerInfo.cfg");
			BufferedReader bufferedReader 	= new BufferedReader(fileReader);
			
			while (bufferedReader.readLine() != null) i++;
			// Reset reader to top of file
			bufferedReader.reset();

			bufferedReader.close();
			fileReader.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return i;
	}

/* ---------- End Configuration ---------- */

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
			System.out.println("test3");
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("test4");
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

/* ---------- Message Handlers and Helpers ---------- */

	/* Perform handshake */
	void handshake(Socket s) {
		//convert integer peerID (can change to byte[] if more convenient) to string
		String pid = Integer.toString(peerID);
		//add peer id to end of handshake message
		String hmsg = "P2PFILESHARINGPROJ0000000000" + pid;
		
		//for debugging purposes
		//System.out.println("peerid: " + peerid);
		//System.out.println("hmsg: " + hmsg);
		//hmsg = "12";
		//translate from string to byte
		byte [] b = hmsg.getBytes();
		//create message
		Message msg = new Message();
		msg.setPayload(b);
		//send message		
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		/*
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
		*/
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

	void messageType(Message msg) {
		//check message type bit
		int type = msg.getType();
		int sender = msg.getPeerID();
		int senderIndex = 0;

		if (type == BITFIELD) 
		{
			//add bitfield to datastructure that tracks each peer's bitfield
			//compare to this peer's bitfield to see if there is any interest
			//send interested/not interested
		
			//update sender's bitfield

			peers[senderIndex].setTheirBitField(extractBitfield(msg.getPayload())); //peers[] is an array of PeerInfo instances, one for each neighbor
			boolean check = checkInterest(senderIndex);
			if (check) {
				sendInterested(peers[senderIndex].getSocket()); 
			} else {
				sendNotInterested(peers[senderIndex].getSocket()); //assuming we send to socket, idk
			}
		
		}
		else if (type == INTERESTED) 
		{
			//update this peer's info about sender to include that sender is interested in this peer's data
			peers[senderIndex].setTheirInterest(true);

		}
		else if (type == NOT_INTERESTED) 
		{
			//set sender's interest in this peer to false
			peers[senderIndex].setTheirInterest(false);
		}
		else if (type == CHOKE) 
		{
			//stop sending requests?
			//log and exit/go back to waiting for messages I guess
		}
		else if (type == UNCHOKE)
		{
			// check if still interested, then send requests
			
			ArrayList<Integer> want = findWantedPieces(senderIndex);
			//if there are no wanted pieces, send an uninterested message
			//else send a request for a random piece that this peer doesn't have
			if (want.isEmpty()) {
				sendNotInterested(peers[senderIndex].getSocket());
			} else {
				sendRequest(peers[senderIndex].getSocket(), getRandomIndex(senderIndex));
			}
		
		}
		else if (type == REQUEST) 
		{
			//send piece message back with requested piece
			sendPiece( peers[senderIndex].getSocket(), extractIndex(msg.getPayload()) );
		
		}
		else if (type == PIECE)
		{
			//update bitfield, data
			//send have to everyone with updated bitfield
			//check if still interested in current peer, if yes request
			//else send not interested
			//finally check to see if file is complete, if yes enter random selected neighbors mode
			
			//Add piece to data, broadcast updated bitfield
			
			

			byte[] temp = msg.getPayload();
			byte[] temp2 = new byte[temp.length-4];
			for (int i=0; i < temp.length-4; i++) {
				temp2[i] = temp[i+4];
			}
			writePiece(temp2, extractIndex(temp));

			for(int i = 0; i < (nPeers - 1); i++) {
				sendHave(peers[i].getSocket(), extractIndex(msg.getPayload()));
			}
				
			// check if still interested, then send requests
			ArrayList<Integer> want = findWantedPieces(senderIndex);
			//if there are no wanted pieces, send an uninterested message
			//else send a request for a random piece that this peer doesn't have
			if (want.isEmpty()) {
				sendNotInterested(peers[senderIndex].getSocket());
			} else {
				sendRequest(peers[senderIndex].getSocket(), getRandomIndex(senderIndex));
			}
		}
		else if (type == HAVE) 
		{
			//update tracked bitfield for sender
			//reevaluate interest/non-interest
		
			//update sender's bitfield
			peers[senderIndex].setTheirBitField(extractBitfield(msg.getPayload())); //peers[] is an array of PeerInfo instances, one for each neighbor
			boolean check = checkInterest(senderIndex);
			if (check) {
				sendInterested(peers[senderIndex].getSocket());
			} else {
				sendNotInterested(peers[senderIndex].getSocket()); //assuming we send to socket
			}
		}
		
	}

	int getRandomIndex(int peerIndex) {
		while (true) {
			int i =(int)(Math.random() * bitfield.length());
			if (bitfield.get(i) == false && peers[peerIndex].getBitField().get(i) == true) {
				return i;
			}
		}
	}

	int extractIndex (byte[] payload) {
		int index = 0;
		for (int i=0; i<4; i++) {
			index *= 16;
			index += Byte.valueOf(payload[i]).intValue();
		}
		return index;
	}

	boolean checkInterest(int index) {
		BitSet bf = peers[index].getBitField();
		for (int i = 0; i < bf.length(); i++) {
			if ((bitfield.get(i) == bf.get(i)) && (bf.get(i) == true)) {
				return true;	//there's at least one piece this peer is interested in
			}
		}
		return false;	//there's nothing the sender has that this peer wants
	}
	ArrayList<Integer> findWantedPieces(int index) {
		BitSet bf = peers[index].getBitField();
		ArrayList<Integer> want = new ArrayList<Integer>();
		for (int i = 0; i < bf.length(); i++) {
			if ((bitfield.get(i) != bf.get(i)) && (bf.get(i) == true)) {
				want.add(i);	//append the index of the piece they have that we want
			}
		}
		return want;
	}

	//converts a bitfield message payload into a bitset
	BitSet extractBitfield(byte[] payload) {
		BitSet bf = new BitSet();
		for(int i = 0; i < payload.length; i++) {
			if (payload[i] == 1) {
				bf.set(i);
			}
		}
		return bf;
	}

/* ---------- Message Handlers and Helpers ---------- */

/* ---------- Send messages ---------- */

	/* Send choke message */
	void sendChoke (Socket s) {
		Message msg = new Message();
		msg.setLength(1);
		msg.setType(CHOKE);
		msg.setPayload(null);
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	/* Send unchoke message */
	void sendUnchoke (Socket s) {
		Message msg = new Message();
		msg.setLength(1);
		msg.setType(UNCHOKE);
		msg.setPayload(null);
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send interested message */
	void sendInterested (Socket s) {
		Message msg = new Message();
		msg.setLength(1);
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
	void sendNotInterested (Socket s) {
		Message msg = new Message();
		msg.setLength(1);
		msg.setType(NOT_INTERESTED);
		msg.setPayload(null);
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send have message */
	void sendHave (Socket s, int pieceIndex) {
		byte[] bPieceIndex = new byte [4];
		// Convert pieceIndex from int to byte[]
		for (int i=bPieceIndex.length-1; i>=0; i--) {
			bPieceIndex[i] = (byte)(pieceIndex % 16);
			pieceIndex /= 16;
		}

		Message msg = new Message();
		msg.setLength(5);
		msg.setType(HAVE);
		msg.setPayload(bPieceIndex);
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send bitfield message */
	void sendBitfield (Socket s) {
		byte[] bitField = bitfield.toByteArray();
		Message msg = new Message();
		msg.setLength(1 + bitField.length);	
		msg.setType(BITFIELD);
		msg.setPayload(bitField);		
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send request message */
	void sendRequest (Socket s, int pieceIndex) {
		byte[] bPieceIndex = new byte [4];
		// Convert pieceIndex from int to byte[]
		for (int i=bPieceIndex.length-1; i>=0; i--) {
			bPieceIndex[i] = (byte)(pieceIndex % 16);
			pieceIndex /= 16;
		}

		Message msg = new Message();
		msg.setLength(5);
		msg.setType(REQUEST);
		msg.setPayload(bPieceIndex);
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

	/* Send piece message */
	void sendPiece (Socket s, int pieceIndex) {
		byte[] data = new byte [4 + pieceSize];
		
		// Convert pieceIndex from int to byte[]
		for (int i=3; i>=0; i--) {
			data[i] = (byte)(pieceIndex % 16);
			pieceIndex /= 16;
		}
		for (int i=0; i<pieceSize; i++) {
			data[i+4] = fileData[pieceIndex*pieceSize + i]; 
		}

		//byte[] piece = pieces[pieceIndex];
	 
		Message msg = new Message();
		msg.setLength(data.length);	// SET LENGTH APPROPRIATELY 
		msg.setType(PIECE);
		msg.setPayload(data); 		// AND PIECE CONTENT!
		try {
			msg.send(s);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}	
	}

/* ---------- End send messages ---------- */

	/* Get message length */
	int messageLength(byte[] ml) {
		byte[] meslen = new byte[4];
		for (int i = 0; i<5; i++) {
			meslen[i] = ml[i]; 
		}
		//returns integer value of first four bits of byte message
		return new BigInteger(meslen).intValue();
		
	}

/* ---------- Write Logs ---------- */

	/* Writer for all cases */
	void writeLog (Path logPath, String logData) {
		try {
			Files.write(logPath, logData.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException f) {
			System.out.println("Cannot print \"" + logData + "\" to " + logPath);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	void logTCP (LocalTime time, int theirPeerID) {
		String logData = time.toString() + ": Peer " + peerID + " makes a connection to Peer " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logChangeOfPreferredNeighbors (LocalTime time, int [] preferredPeerIDs) {
		String logData = time.toString() + ": Peer " + peerID + " has the preferred neighbors";
		if (preferredPeerIDs.length > 0) {
			logData += " " + preferredPeerIDs[0];
		}
		for (int i=1; i<preferredPeerIDs.length; i++) {
			logData += ", " + preferredPeerIDs[i];
		}
		logData += ".";
		writeLog(logPath, logData);
	}

	void logChangeOfOptimisticallyUnchokedNeighbor (LocalTime time, int theirPeerID) {
		String logData = time.toString() + ": Peer " + peerID + " has the optimistically unchoked neighbor " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logUnchoking (LocalTime time, int theirPeerID) {

		String logData = time.toString() + ": Peer " + peerID + " is unchoked by " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logChoking (LocalTime time, int theirPeerID) {

		String logData = time.toString() + ": Peer " + peerID + " is choked by " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logHave (LocalTime time, int theirPeerID, int pieceIndex) {
		String logData = time.toString() + ": Peer " + peerID + " received the 'have' message from " + theirPeerID + " for the piece " + pieceIndex + ".";
		writeLog(logPath, logData);
	}

	void logInterested (LocalTime time, int theirPeerID) {
		String logData = time.toString() + ": Peer " + peerID + " received the 'interested' message from " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logNotInterested (LocalTime time, int theirPeerID) {
		String logData = time.toString() + ": Peer " + peerID + " received the 'not interested' message from " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logDownloadingPiece (LocalTime time, int theirPeerID, int pieceIndex) {
		String logData = time.toString() + ": Peer " + peerID + " has downloaded the piece " + pieceIndex + " from " + theirPeerID + ".";
		writeLog(logPath, logData);
	}

	void logCompletionOfDownload (LocalTime time, int theirPeerID) {
		String logData = time.toString() + ": Peer " + peerID + " has downloaded the complete file.";
		writeLog(logPath, logData);
	}

/* ---------- End Write Logs ---------- */
	
/* ---------- Write Data to File ---------- */

	void writePiece (byte[] data, int pieceIndex) {
		for (int i=0; i<pieceSize; i++) {
			fileData[pieceIndex*pieceSize + i] = data[i];
		}
	}

	void setBitInBitfield (int pieceIndex) {
		bitfield.set(pieceIndex);
		bitfield.flip(0,bitfield.length());
		if (bitfield.isEmpty()) {
			// write piece to file
		}
		bitfield.flip(0,bitfield.length());
	}

	void writeToFile (byte[] data) {
		// check if file exists
		try{
			Files.createFile(filePath);
		} catch (IOException f) {
			// it might already exist so it's totes fine
		}
		// append file
		try {
			Files.write(filePath, data);
		} catch (IOException f) {
			System.out.println(f);
		}
	}

/* ---------- End Write Data to File ---------- */

	/* Main Method */
	public static void main(String args[]) {
		
		PeerProcess peer = new PeerProcess(Integer.parseInt(args[0]));
		//spin a thread and start the peer
		// TODO: start each peer
		Thread peer_thread = new Thread(peer);
		peer_thread.start();
	}

}





