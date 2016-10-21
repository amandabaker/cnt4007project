import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.math.BigInteger;

public class Client {

	final int CHOKE 			= 0; 
	final int UNCHOKE 			= 1;
	final int INTERESTED 		= 2;
	final int NOT_INTERESTED 	= 3;
	final int HAVE 				= 4;
	final int BITFIELD 			= 5;
	final int REQUEST 			= 6;
	final int PIECE 			= 7;

	Socket requestSocket;          	//socket connect to the server
	ObjectOutputStream out;        	//stream write to the socket
	OutputStream outStream;
 	ObjectInputStream in;          	//stream read from the socket
	String message;                	//message send to the server
	byte[] MESSAGE;                	//capitalized message read from the server

	public void Client() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			handshake("1010");
			//Receive the upperCase sentence from the server
			MESSAGE = (byte [])in.readObject();
			//show the message to the user
			System.out.println("Handshake message: " + MESSAGE);
			while(true)
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
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
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

	//send a message to the output stream
	void sendMessage(Message msg)
	{
		try{
			msg.send(requestSocket);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	//handshake method
	void handshake(String peerid)
	{
		try{
			//stream write the message
			outStream.write((byte)"P2PFILESHARINGJPROJ");
			outStream.write((byte)0);
			outStream.write((byte)peerid);
		
			outStream.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	void handshakeCheck(String hcheck) 
	{
		//check whether handshake was successful and connected peer is valid
	}
	
	int messageLength(byte[] ml) 
	{
		byte[] meslen = new byte[4];
		for (int i = 0; i<5; i++) 
		{
			meslen[i] = ml[i]; 
		}
		//returns integer value of first four bits of byte message
		return new BigInteger(meslen).intValue();
		
	}

	void messageType(byte [] message) 
	{
		//check message type bit
		//not tested
		int type = message[4];
		if (type == 0) 
		{
			//choke
		}
		if (type == 1)
		{
			//unchoke
		}
		if (type == 2) 
		{
			//interested
		}
		if (type == 3) 
		{
			//not interested
		}
		if (type == 4) 
		{
			//have
		}
		if (type == 5) 
		{
			//bitfield
		}
		if (type == 6) 
		{
			//request
		}
		if (type == 7)
		{
			//piece
		}
	}

	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}

}
