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

    public Message () {
        length = 0;
        type = 0;
        payload = null;
    }

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

    void setLength (int length) {
        this.length = length;
    }

    int getLength () {
        return length;
    }
    void setType (int type) {
        if (type < 8 && type > 0) {
            this.type = type;
        }
    }

    int getType () {
        return type;
    }

    void setPayload (bit[] payload) {
        this.payload = payload;
    }

    bit[] getPayload () {
        return payload;
    }
}