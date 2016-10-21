import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.math.BigInteger;

public class Message {

    private int length;         // length of message (not including length field)
    private int type;           // type of message, see list above
    private int peerID;         // peerID
    private byte[] payload;     // payload of message

    public Message (int type, byte[] payload) {
        length = 0;
        this.type = type;
        this.payload = payload;
    }

    public void send (Socket s) throws IOException {

        // set length before sending
        length = payload.length + 4;
        // get output stream from socket
        OutputStream out = s.getOutputStream();
        // 1. send length
        out.write((byte)length);
        // 2. send type
        out.write((byte)type);
        // 3. send payload (if any)
        if (payload != null) {
            out.write(payload);
        }
        out.flush();
    }
}