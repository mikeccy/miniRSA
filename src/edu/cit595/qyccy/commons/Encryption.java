package edu.cit595.qyccy.commons;

import java.io.UnsupportedEncodingException;

public class Encryption {

    private Configs configs = Configs.INSTANCE;

//    private long e = 451;
//    private long d = 1531;
//    private long c = 2623;
    
    private long e = 17;
    private long d = 2753;
    private long c = 3233;

    private int procBytes = 0;
    private int sendBytes = 0;

    public Encryption() {
        init();
    }

    public Encryption(final long e, final long d, final long c) {
        this.e = e;
        this.d = d;
        this.c = c;
        init();
    }

    private void init() {
        long rc = c;
        int numBytes = 0;
        long remain = rc % configs.byteSize;
        while (true) {
            rc /= configs.byteSize;
            if (rc > 0 && numBytes != configs.byteBit) {
                numBytes++;
            } else {
                procBytes = (numBytes > 0) ? numBytes : numBytes + 1;
                sendBytes = (remain == 0) ? numBytes : numBytes + 1;
                break;
            }
        }
    }

    public final byte[] encryptMessage(final String msg)
            throws UnsupportedEncodingException {
        final byte[] msgData = msg.getBytes(configs.encoding);
        int sendLength = msgData.length * sendBytes;
        final byte[] sendData = new byte[sendLength];
        for (int i = 0; i < msgData.length; i += procBytes) {
            long reGrouped = 0;
            for (int j = 0; j < procBytes; j++) {
                reGrouped += (msgData[i + j] & 0xFF) << (configs.byteBit * j);
            }
            long toSend = RSA.encrypt(reGrouped, e, c);
            int startIndex = i / procBytes * sendBytes;
            for (int j = 0; j < sendBytes; j++) {
                sendData[startIndex + j] = (byte) (toSend & (configs.byteSize - 1));
                toSend = toSend >> configs.byteBit;
            }
        }
        return sendData;
    }

    public final String decryptMessage(final byte[] msgData, final int msgLength)
            throws InvalidDataFormatException, UnsupportedEncodingException {
        if (msgLength % sendBytes != 0)
            new InvalidDataFormatException("Received message corrupted");
        int predMsgLength = msgLength / sendBytes;
        byte[] recvData = new byte[predMsgLength];
        for (int i = 0; i < msgLength; i += sendBytes) {
            long reGrouped = 0;
            for (int j = 0; j < sendBytes; j++) {
                reGrouped += (msgData[i + j] & 0xFF) << (configs.byteBit * j);
            }
            long received = RSA.decrypt(reGrouped, d, c);
            int startIndex = i / sendBytes * procBytes;
            for (int j = 0; j < procBytes; j++) {
                recvData[startIndex + j] = (byte) (received & 255);
                received = received >> configs.byteBit;
            }
        }
        final String msg = new String(recvData, configs.encoding);
        return msg;
    }
    
    public final long getE() {
        return e;
    }
    
    public final long getC() {
        return c;
    }
}
