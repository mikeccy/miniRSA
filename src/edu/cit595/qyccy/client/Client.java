package edu.cit595.qyccy.client;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import edu.cit595.qyccy.commons.Configs;
import edu.cit595.qyccy.commons.Connection;
import edu.cit595.qyccy.commons.Encryption;
import edu.cit595.qyccy.commons.InvalidDataFormatException;

public class Client {

    private Configs configs = Configs.INSTANCE;
    private Encryption encryption = null;

    private Connection connection = null;
    
    private ClientGui cg = null;

    public Client() {
        cg = new ClientGui();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                cg.setVisible(true);
            }
        });
        init();
    }

    private void init() {
        try {
            encryption = new Encryption();
            connection = new Connection(configs.serverHost, configs.serverPort,
                    encryption);
            cg.setConnection(connection);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void start() {
        try {
            while(true) {
                String s = connection.recvMessage();
                if (s.compareTo("quit") == 0) {
                    cg.display("closed\n");
                    cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
                }
                cg.display(s + "\n");
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidDataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            connection.shutdown();
            connection = null;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
        client.shutdown();
    }

}
