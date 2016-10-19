import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {

	private static final int sPort = 8000;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		String clientNum = "1";
        try {

			// ----- TODO: Read Common.cfg

			// ----- TODO: Read PeerInfo.cfg
			FileReader fileReader = new FileReader("PeerInfo.cfg");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String line = null;
			String peerID = "";
			String hostname = "";
			String port = "";
			String hasFile = "";

			while ((line = bufferedReader.readLine()) != null) {
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
			}
			
            while(true) {
                new Handler(listener.accept(),clientNum, "localhost").start();
				System.out.println("Client "  + clientNum + " is connected!");
    		}
        } finally {
    		listener.close();
    	} 
 
    }

	/**
	* A handler thread class.  Handlers are spawned from the listening
	* loop and are responsible for dealing with a single client's requests.
	*/
	private static class Handler extends Thread {
		private String message;    			//message received from the client
		private String MESSAGE;    			//uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in;		//stream read from the socket
		private ObjectOutputStream out;    	//stream write to the socket
		private String peerID;				//The index number of the client
		private String hostname;

		public Handler(Socket connection, String peerID, String hostname) {
			this.connection = connection;
			this.peerID = peerID;
			this.hostname = hostname;
		}

		public void run() {
			try{

				/* PROJECT STUFF */
				String peerProcessName = "java peerProcess";
				String peerProcessArguments = peerID;
				String workingDir = System.getProperty("user.dir");
				Runtime.getRuntime().exec("ssh " + hostname + " cd " + workingDir + " ; " + peerProcessName + " " + peerProcessArguments);

				/* SAMPLE CODE STUFF */
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try{
					while(true) {
						//receive the message sent from the client
						message = (String)in.readObject();
						//show the message to the user
						System.out.println("Receive message: " + message + " from client " + peerID);
						//Capitalize all letters in the message
						MESSAGE = message.toUpperCase();
						//send MESSAGE back to the client
						sendMessage(MESSAGE);
					}
				}
				catch(ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + peerID);
			}
			finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + peerID);
				}
			}
		}

		//send a message to the output stream
		public void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg + " to Client " + peerID);
			}
			catch (IOException ioException) {
				ioException.printStackTrace ();
			}
		}

	}

}
