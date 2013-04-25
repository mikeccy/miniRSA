package edu.cit595.qyccy.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import edu.cit595.qyccy.commons.Connection;
import edu.cit595.qyccy.commons.Encryption;
import edu.cit595.qyccy.commons.InvalidDataFormatException;

public class ServerConnection extends Connection implements Runnable {

    public ServerConnection(final Socket socket, final Encryption encryption)
            throws IOException {
        super(socket, encryption);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String s = recvMessage();
                if (s.compareTo("quit") == 0) {
                    System.out.println("exit");
                    sendMessage("quit");
                    break;
                }
                System.out.println(s);
                sendMessage(s);
            }
            shutdown();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidDataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
