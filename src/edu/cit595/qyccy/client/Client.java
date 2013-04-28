package edu.cit595.qyccy.client;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import edu.cit595.qyccy.common.Configs;
import edu.cit595.qyccy.exception.InvalidDataFormatException;
import edu.cit595.qyccy.exception.InvalidKeyException;
import edu.cit595.qyccy.model.Message;
import edu.cit595.qyccy.model.Respond;
import edu.cit595.qyccy.transfer.Connection;
import edu.cit595.qyccy.transfer.Encryption;
import edu.cit595.qyccy.transfer.Protocol;

public class Client {

    private Configs configs = Configs.INSTANCE;
    private Encryption encryption = null;

    private Protocol protocol = Protocol.INSTANCE;

    private Connection connection = null;

    private ClientGui cg = null;

    public Client() {
        // init gui
        cg = new ClientGui();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                cg.setVisible(true);
            }
        });
        init();
    }

    private void init() {
        try {
            // set encryption based on user input
            encryption = new Encryption(451, 1531, 2623);
            // init and setup connection
            connection = new Connection(configs.serverHost, configs.serverPort,
                    encryption);
            cg.setConnection(connection);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(cg, e.getMessage());
            cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(cg, e.getMessage());
            cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
        }
    }

    public void start() {
        try {
            // protocol init (header only msg)
            connection.sendMessage(protocol.requestInit(connection.getE(),
                    connection.getC()));
            Message msg = connection.recvMessage(true);
            msg.header.parseResponseHeader();
            if (msg.header.respondType == Respond.OK) {
                // update client id
                cg.updateClientId(msg.header.clientId);
            } else if (msg.header.respondType == Respond.BAD) {
                // bad response
                errorClose();
                return;
            } else if (msg.header.respondType == Respond.END) {
                // end response
                serverClose();
                return;
            }
            while (true) {
                Message message = connection.recvMessage(true);
                message.header.parseResponseHeader();
                if (message.header.respondType == Respond.OK) {
                    // display content
                    cg.updateClientId(msg.header.clientId);
                    cg.display(message.content + "\n");
                } else if (message.header.respondType == Respond.BAD) {
                    // bad response
                    errorClose();
                    return;
                } else if (message.header.respondType == Respond.END) {
                    // end response
                    serverClose();
                    return;
                } else if (message.header.respondType == Respond.UPDT) {
                    cg.updateAllClient(message.header.clients);
                }
            }
        } catch (UnsupportedEncodingException e) {
            // should not happen
            e.printStackTrace();
        } catch (IOException e) {
            // socket close
            System.out.println("Socket closed");
            serverClose();
        } catch (InvalidDataFormatException e) {
            // bad response
            errorClose();
        } catch (InvalidKeyException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            connection.shutdown();
            connection = null;
        } catch (IOException e) {
            System.out.println("Socket closed");
            serverClose();
        }
    }

    private void errorClose() {
        JOptionPane.showMessageDialog(cg, "Bad response quitting...");
        cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
    }

    private void serverClose() {
        JOptionPane.showMessageDialog(cg, "Server quitting...");
        cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
        client.shutdown();
    }

}
