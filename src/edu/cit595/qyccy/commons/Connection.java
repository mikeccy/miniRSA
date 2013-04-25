package edu.cit595.qyccy.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {

    private Configs configs = Configs.INSTANCE;

    private Encryption encryption = null;

    public Socket socket = null;
    public InputStream socketInput = null;
    public OutputStream socketOutput = null;

    final byte[] recvBuff = new byte[configs.recvBuffSize];

    public Connection(final Socket socket, final Encryption encryption)
            throws IOException {
        this.encryption = encryption;
        if (encryption == null) {
            new RuntimeException("Encryption should not be null");
        }
        this.socket = socket;
        if (socket != null) {
            if (socket.isConnected()) {
                init();
                return;
            }
        }
        new UnknownHostException();
    }

    public Connection(final String serverHost, final int serverPort,
            final Encryption encryption) throws UnknownHostException,
            IOException {
        this.encryption = encryption;
        if (encryption == null) {
            new RuntimeException("Encryption should not be null");
        }
        socket = new Socket(serverHost, serverPort);
        init();
    }

    private void init() throws IOException {
        socketInput = socket.getInputStream();
        socketOutput = socket.getOutputStream();
    }

    public void sendMessage(final String msg) throws IOException,
            UnsupportedEncodingException {
        final byte[] msgData = encryption.encryptMessage(msg);
        socketOutput.write(msgData, 0, msgData.length);
    }

    public final String recvMessage() throws IOException,
            UnsupportedEncodingException, InvalidDataFormatException {
        int bytesRead = 0;
        bytesRead = socketInput.read(recvBuff, 0, configs.recvBuffSize);
        if (bytesRead > 0) {
            return encryption.decryptMessage(recvBuff, bytesRead);
        } else {
            // no data or end of stream
            return "";
        }
    }

    public void shutdown() throws IOException {
        if (socket != null)
            socket.close();
        socket = null;
    }

}
