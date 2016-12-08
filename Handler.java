import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.math.BigInteger;

/* ------------------------- */
/* DO WE EVEN NEED THIS????? */
/* ------------------------- */


public class Handler extends Thread {
        	//private String message;    //message received from the client
    		private byte [] message;
			//private String MESSAGE;    //uppercase message send to the client
    		private byte [] MESSAGE;
			private Socket connection;
        	private ObjectInputStream in;	//stream read from the socket
        	private ObjectOutputStream out;    //stream write to the socket
			private int no;		//The index number of the client

        	public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        	}

	        public void run() {
		 		try{
					//initialize Input and Output streams
					out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					in = new ObjectInputStream(connection.getInputStream());
					try{
						while(true)
						{
							//receive the message sent from the client
							message = (byte[])in.readObject();
							//message = (String)in.readObject();
							//convert message to string
							String bytestring = new String(message, StandardCharsets.UTF_8);
							
							//show the message to the user
							System.out.println("Receive message: " + bytestring + " from client " + no);
							//Capitalize all letters in the message
							//MESSAGE = message.toUpperCase();
							MESSAGE = message;
							//send MESSAGE back to the client
							sendMessage(MESSAGE);
						}
					}
					catch(ClassNotFoundException classnot){
							System.err.println("Data received in unknown format");
						}
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + no);
				}
				finally{
					//Close connections
					try{
						in.close();
						out.close();
						connection.close();
					}
					catch(IOException ioException){
						System.out.println("Disconnect with Client " + no);
					}
				}
			}

		//send a message to the output stream
		public void sendMessage(byte [] msg)
		{
			try{
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg + " to Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}

    }