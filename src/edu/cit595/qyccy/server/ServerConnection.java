package edu.cit595.qyccy.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Map;

import edu.cit595.qyccy.exception.InvalidDataFormatException;
import edu.cit595.qyccy.exception.InvalidKeyException;
import edu.cit595.qyccy.model.Message;
import edu.cit595.qyccy.model.Request;
import edu.cit595.qyccy.model.Respond;
import edu.cit595.qyccy.transfer.Connection;
import edu.cit595.qyccy.transfer.Encryption;

public class ServerConnection extends Connection implements Runnable {

    private Integer clientId = null;
    private Server server = null;
    private Map<Integer, ServerConnection> clientMap = null;

    public ServerConnection(final Socket socket, final Server server)
            throws IOException {
        super(socket);
        this.server = server;
    }

    private void setClientInfo() {
        this.clientId = server.addNewClient(this);
        this.clientMap = server.getClientMap();
    }

    @Override
    public void run() {
        try {
            // protocol exchange
            Message msg = recvMessage(false);
            msg.header.parseRequestHeader();
            if (msg.header.respondType == Respond.OK) {
                setClientInfo();
                this.encryption = new Encryption(msg.header.pubKeyE,
                        Encryption.notSet, msg.header.pubKeyC);
                sendMessage(protocol.respond(Respond.OK, clientId.toString()));
                broadCastClientInfo();
            } else {
                endClient(Respond.BAD);
                return;
            }

            while (true) {
                Message message = recvMessage(false);
                message.header.parseRequestHeader();
                // if quit request
                if (message.header.requestType == Request.END) {
                    endClient(Respond.OK);
                    return;
                } else {
                    System.out.println(message.content);
                    sendMessage(protocol.respond(Respond.OK,
                            clientId.toString()));
                }
            }

        } catch (UnsupportedEncodingException e) {
            // should not happen
            e.printStackTrace();
        } catch (IOException e) {
            // socket error close
            System.out.println("Socket closed");
        } catch (InvalidDataFormatException e) {
            // end
            endClient(Respond.BAD);
        } catch (InvalidKeyException e) {
            // should not happen
            e.printStackTrace();
        } finally {
            // remove from client map
            if (clientMap != null)
                clientMap.remove(clientId);
            broadCastClientInfo();
        }
    }

    private void endClient(final Respond statusCode) {
        try {
            sendMessage(protocol.respond(statusCode, clientId.toString()));
            shutdown();
        } catch (UnsupportedEncodingException e) {
            // should not happen
            e.printStackTrace();
        } catch (IOException e) {
            // socket error close
            System.out.println("Socket closed");
        } catch (InvalidKeyException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    private void broadCastClientInfo() {
        if (clientMap == null)
            return;
        for (ServerConnection conn : clientMap.values()) {
            try {
                conn.sendMessage(protocol.broadcastClientInfo(clientMap
                        .keySet()));
            } catch (UnsupportedEncodingException e) {
                // should not happen
                e.printStackTrace();
            } catch (IOException e) {
                // socket error close
                System.out.println("Socket closed");
            } catch (InvalidKeyException e) {
                // should not happen
                e.printStackTrace();
            }
        }
    }

}
