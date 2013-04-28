package edu.cit595.qyccy.transfer;

import java.io.UnsupportedEncodingException;

import edu.cit595.qyccy.exception.InvalidDataFormatException;
import edu.cit595.qyccy.exception.InvalidKeyException;

public class Encryption {

    public final static long notSet = -1;

    private Protocol protocol = Protocol.INSTANCE;

    private long e = notSet;
    private long d = notSet;
    private long c = notSet;

    private int procBytes = 0;
    private int sendBytes = 0;

    public Encryption(final long e, final long d, final long c) {
        this.e = e;
        this.d = d;
        this.c = c;
        init();
    }

    private void init() {
        long rc = c;
        int numBytes = 0;
        long remain = c % protocol.byteSize;
        while (true) {
            rc /= protocol.byteSize;
            if (rc > 0 && numBytes != protocol.byteBit) {
                numBytes++;
            } else {
                procBytes = (numBytes > 0) ? numBytes : numBytes + 1;
                sendBytes = (remain != 0 && numBytes < protocol.byteBit) ? numBytes + 1
                        : numBytes;
                break;
            }
        }
    }

    public final byte[] encryptMessage(final String msg)
            throws UnsupportedEncodingException, InvalidKeyException {
        if (e == notSet || c == notSet)
            throw new InvalidKeyException("Encryption key not found");
        final byte[] msgData = msg.getBytes(protocol.encoding);
        int sendLength = (int) Math.ceil((double) msgData.length
                / (double) procBytes)
                * sendBytes;
        final byte[] sendData = new byte[sendLength];
        for (int i = 0; i < msgData.length; i += procBytes) {
            long reGrouped = 0;
            for (int j = 0; j < procBytes; j++) {
                int index = i + j;
                byte data;
                if (index < msgData.length) {
                    data = msgData[i + j];
                } else {
                    data = 0;
                }
                reGrouped += (data & 0xFF) << (protocol.byteBit * j);
            }
            long toSend = RSA.encrypt(reGrouped, e, c);
            int startIndex = i / procBytes * sendBytes;
            for (int j = 0; j < sendBytes; j++) {
                sendData[startIndex + j] = (byte) (toSend & (protocol.byteSize - 1));
                toSend = toSend >> protocol.byteBit;
            }
        }
        return sendData;
    }

    public final String decryptMessage(final boolean decrypt,
            final byte[] msgData, final int offset, final int msgLength)
            throws InvalidDataFormatException, UnsupportedEncodingException,
            InvalidKeyException {
        if (msgLength == 0)
            return "";
        if (msgLength % sendBytes != 0 || msgLength < 0
                || offset > msgData.length)
            throw new InvalidDataFormatException("Received message corrupted");
        if (!decrypt) {
            // return raw data
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < msgLength; i++) {
                sb.append(msgData[offset + i] & 0xFF);
                sb.append(protocol.sep);
            }
            return sb.toString();
        }
        if (d == notSet || c == notSet)
            throw new InvalidKeyException("Decryption key not found");

        int predMsgLength = msgLength / sendBytes * procBytes;
        byte[] recvData = new byte[predMsgLength];
        for (int i = 0; i < msgLength; i += sendBytes) {
            long reGrouped = 0;
            for (int j = 0; j < sendBytes; j++) {
                reGrouped += (msgData[offset + i + j] & 0xFF) << (protocol.byteBit * j);
            }
            long received = RSA.decrypt(reGrouped, d, c);
            int startIndex = i / sendBytes * procBytes;
            for (int j = 0; j < procBytes; j++) {
                recvData[startIndex + j] = (byte) (received & (protocol.byteSize - 1));
                received = received >> protocol.byteBit;
            }
        }
        final String msg = new String(recvData, protocol.encoding);
        return msg;
    }

    public final long getE() {
        return e;
    }

    public final long getC() {
        return c;
    }

    public final long getD() {
        return d;
    }

    public boolean crackedD(final long d) {
        if (this.d == notSet) {
            this.d = d;
            return true;
        }
        return false;
    }
}
