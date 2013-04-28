package edu.cit595.qyccy.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.cit595.qyccy.exception.InvalidDataFormatException;
import edu.cit595.qyccy.exception.InvalidKeyException;
import edu.cit595.qyccy.model.Header;
import edu.cit595.qyccy.model.Message;

public class Connection {

    protected Protocol protocol = Protocol.INSTANCE;

    protected Encryption encryption = null;

    public Socket socket = null;
    public InputStream socketInput = null;
    public OutputStream socketOutput = null;

    final byte[] recvBuff = new byte[protocol.recvBuffSize];

    public Connection(final Socket socket) throws IOException {
        this.socket = socket;
        if (socket != null) {
            if (socket.isConnected()) {
                init();
                return;
            }
        }
        throw new UnknownHostException();
    }

    public Connection(final String serverHost, final int serverPort,
            final Encryption encryption) throws UnknownHostException,
            IOException {
        this.encryption = encryption;
        if (encryption == null) {
            throw new RuntimeException("Encryption should not be null");
        }
        socket = new Socket(serverHost, serverPort);
        init();
    }

    private void init() throws IOException {
        socketInput = socket.getInputStream();
        socketOutput = socket.getOutputStream();
    }

    public void sendMessage(final Header header)
            throws UnsupportedEncodingException, IOException,
            InvalidKeyException {
        sendMessage(header, "");
    }

    public void sendMessage(final Header header, final byte[] msgData)
            throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(header.raw.getBytes());
        if (msgData.length > 0)
            byteStream.write(msgData);
        final byte[] data = byteStream.toByteArray();
        socketOutput.write(data, 0, data.length);
    }

    public void sendMessage(final Header header, final String msg)
            throws IOException, UnsupportedEncodingException,
            InvalidKeyException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(header.raw.getBytes());
        if (msg.length() > 0)
            byteStream.write(encryption.encryptMessage(msg));
        final byte[] data = byteStream.toByteArray();
        socketOutput.write(data, 0, data.length);
    }

    public void sendMessage(final String msg) throws IOException,
            UnsupportedEncodingException, InvalidKeyException {
        sendMessage(protocol.requestChat(), msg);
    }

    public final Message recvMessage(boolean decrypt) throws IOException,
            InvalidDataFormatException, InvalidKeyException {
        int bytesRead = 0;
        bytesRead = socketInput.read(recvBuff, 0, protocol.recvBuffSize);
        Message msg = new Message();
        if (bytesRead > 0) {
            String headerMsg = new String(recvBuff, 0, bytesRead,
                    protocol.encoding);
            String header = headerMsg.substring(0,
                    headerMsg.lastIndexOf(protocol.endHeader)
                            + protocol.endHeader.length());
            msg.header.raw = header;
            int headerLength = header.getBytes().length;
            if (encryption != null) {
                msg.content = encryption.decryptMessage(decrypt, recvBuff,
                        headerLength, bytesRead - headerLength);
            } else {
                msg.content = "";
            }
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byteStream.write(recvBuff, headerLength, bytesRead - headerLength);
            msg.rawContent = byteStream.toByteArray();
        } else if (bytesRead == -1) {
            throw new IOException("End of socket");
        } else {
            // no data or end of stream
            throw new InvalidDataFormatException("No data received");
        }
        return msg;
    }

    public void shutdown() throws IOException {
        if (socket != null)
            socket.close();
        socket = null;
    }

    public final long getE() {
        return encryption.getE();
    }

    public final long getC() {
        return encryption.getC();
    }

}
