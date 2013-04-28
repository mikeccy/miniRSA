package edu.cit595.qyccy.client;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;

import edu.cit595.qyccy.common.Configs;
import edu.cit595.qyccy.exception.InvalidDataFormatException;
import edu.cit595.qyccy.exception.InvalidHeaderException;
import edu.cit595.qyccy.exception.InvalidKeyException;
import edu.cit595.qyccy.model.Message;
import edu.cit595.qyccy.model.Respond;
import edu.cit595.qyccy.transfer.Connection;
import edu.cit595.qyccy.transfer.Encryption;
import edu.cit595.qyccy.transfer.Protocol;
import edu.cit595.qyccy.transfer.RSA;

public class Client {

    private int clientId;

    private Configs configs = Configs.INSTANCE;
    private Encryption encryption = null;

    private Protocol protocol = Protocol.INSTANCE;

    private Connection connection = null;

    private ClientGui cg = null;

    private long e = Encryption.notSet;
    private long d = Encryption.notSet;
    private long c = Encryption.notSet;

    private HashMap<Integer, Encryption> following = new HashMap<Integer, Encryption>();

    public Client() {
        // init gui
        cg = new ClientGui(this);
        StringBuilder welcomeText = new StringBuilder();
        welcomeText
                .append("Welcome to miniRSA!\nby Quan Yuan and Chongyu Chen\n\n");
        welcomeText
                .append("1. You can view hints by hovering mouse on each GUI element.\n\n");
        welcomeText.append("Client list on the right:\n");
        welcomeText.append(ClientGui.clientListTip + "\n\n");
        welcomeText.append("Input area at the bottom:\n");
        welcomeText.append(ClientGui.inputAreaTip + "\n\n");
        welcomeText
                .append("2. Open another client window to evasdrop and crack.\n\n");
        cg.display(welcomeText.toString());
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                cg.setVisible(true);
            }
        });
        init();
    }

    private void init() {
        try {
            while (!calcPublicKey(cg.askForPrime())) {
                cg.alert("Please input again. m != n; m,n in [4,4000]");
            }

            // set encryption based on user input
            encryption = new Encryption(e, d, c);
            // init and setup connection
            connection = new Connection(configs.serverHost, configs.serverPort,
                    encryption);
            cg.setConnection(connection);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            cg.alert(e.getMessage());
            cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
        } catch (IOException e) {
            e.printStackTrace();
            cg.alert(e.getMessage());
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
                this.clientId = msg.header.clientId;
                cg.updateTitle();
            } else if (msg.header.respondType == Respond.BAD) {
                // bad response
                errorClose();
                return;
            } else if (msg.header.respondType == Respond.END) {
                // end response
                serverClose();
                return;
            }
            cg.display("# You have public key (" + e + "," + c
                    + ") private key (" + d + "," + c + ")\n\n");
            cg.display("# Connected to server, have fun!\n");

            while (true) {
                msg = connection.recvMessage(true);
                msg.header.parseResponseHeader();
                if (msg.header.respondType == Respond.OK) {
                    // display content
                    cg.display(ClientGui.selfName + ": " + msg.content + "\n");
                } else if (msg.header.respondType == Respond.BAD) {
                    // bad response may still proceed
                    cg.alert(msg.header.error);
                } else if (msg.header.respondType == Respond.END) {
                    // end response
                    serverClose();
                    return;
                } else if (msg.header.respondType == Respond.UPDT) {
                    cg.updateAllClient(msg.header.clients);
                } else if (msg.header.respondType == Respond.FWD) {
                    if (!following.containsKey(msg.header.targetId)) {
                        following.put(msg.header.targetId, new Encryption(
                                msg.header.targetE, Encryption.notSet,
                                msg.header.targetC));
                        cg.display("Eavesdropping on " + ClientGui.clientPrefix
                                + msg.header.targetId + "\n");
                    } else {
                        String content;
                        Encryption targetEncrypt = following
                                .get(msg.header.targetId);
                        content = targetEncrypt.decryptMessage(false,
                                msg.rawContent, 0, msg.rawContent.length);
                        if (content.length() > 0)
                            cg.display(ClientGui.clientPrefix
                                    + msg.header.targetId + " ENCRY: "
                                    + content + "\n");
                        if (targetEncrypt.getD() != Encryption.notSet) {
                            content = targetEncrypt.decryptMessage(true,
                                    msg.rawContent, 0, msg.rawContent.length);
                            if (content.length() > 0)
                                cg.display(ClientGui.clientPrefix
                                        + msg.header.targetId + " DECRY: "
                                        + content + "\n");
                        }
                    }
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
            // corrupted response
            errorClose();
        } catch (InvalidKeyException e) {
            // should not happen
            e.printStackTrace();
        } catch (InvalidHeaderException e) {
            // corrupted response
            errorClose();
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

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void sendFollowRequest(final String clientId) {
        try {
            connection.sendMessage(protocol.requestEar(clientId));
        } catch (UnsupportedEncodingException e) {
            // should not happen
            e.printStackTrace();
        } catch (IOException e) {
            // socket close
            System.out.println("Socket closed");
            serverClose();
        } catch (InvalidKeyException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    private void errorClose() {
        cg.alert("Bad response quitting...");
        cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
    }

    private void serverClose() {
        cg.alert("Server quitting...");
        cg.dispatchEvent(new WindowEvent(cg, WindowEvent.WINDOW_CLOSING));
    }

    private boolean calcPublicKey(final String input) {
        try {
            String[] num = input.split(",");
            int a = Integer.parseInt(num[0]);
            int b = Integer.parseInt(num[1]);
            if (a == b || a < 7 || b < 7 || a > 4000 || b > 4000)
                return false;
            long[] result = RSA.generateKeyFromNthPrime(a, b);
            if (result.length == 3) {
                this.e = result[0];
                this.d = result[1];
                this.c = result[2];
                return true;
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void crackPrivateKey(int clientId) {
        Encryption encrypt = following.get(clientId);
        if (encrypt != null && encryption.getD() != Encryption.notSet) {
            String targetName = ClientGui.clientPrefix + " " + clientId;
            cg.display("Cracking " + targetName + " with public key: " + "("
                    + encrypt.getE() + "," + encrypt.getC() + ")\n");
            final long[] pubLong = new long[2];
            pubLong[0] = encrypt.getE();
            pubLong[1] = encrypt.getC();
            if (encrypt.crackedD(RSA.bruteFind(pubLong))) {
                cg.display("Success: " + targetName + " has private key: "
                        + "(" + encrypt.getD() + "," + encrypt.getC() + ")\n");
                cg.display("Future messages from " + targetName
                        + " will also be decrypted.\n");
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
        client.shutdown();
    }

}
