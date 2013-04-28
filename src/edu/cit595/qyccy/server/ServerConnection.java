package edu.cit595.qyccy.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.cit595.qyccy.exception.InvalidDataFormatException;
import edu.cit595.qyccy.exception.InvalidHeaderException;
import edu.cit595.qyccy.exception.InvalidKeyException;
import edu.cit595.qyccy.model.Message;
import edu.cit595.qyccy.model.Request;
import edu.cit595.qyccy.model.Respond;
import edu.cit595.qyccy.transfer.Connection;
import edu.cit595.qyccy.transfer.Encryption;

public class ServerConnection extends Connection implements Runnable {

    private Integer clientId = null;
    private Server server = null;
    private ConcurrentHashMap<Integer, Integer> followersId = null;
    private Map<Integer, ClientStatus> clientMap = null;

    public ServerConnection(final Socket socket, final Server server)
            throws IOException {
        super(socket);
        this.server = server;
    }

    private void setClientInfo() {
        followersId = new ConcurrentHashMap<Integer, Integer>();
        this.clientId = server
                .addNewClient(new ClientStatus(this, followersId));
        this.clientMap = server.getClientMap();
    }

    @Override
    public void run() {
        try {
            // protocol exchange
            Message msg = recvMessage(false);
            msg.header.parseRequestHeader();
            if (msg.header.requestType == Request.INIT) {
                setClientInfo();
                this.encryption = new Encryption(msg.header.pubKeyE,
                        Encryption.notSet, msg.header.pubKeyC);
                sendMessage(protocol.respond(Respond.OK, clientId.toString()));
                broadcastClientInfo();
            } else {
                endClient("Corrupted initial request");
                return;
            }

            while (true) {
                Message message = recvMessage(false);
                message.header.parseRequestHeader();
                // if quit request
                if (message.header.requestType == Request.END) {
                    sendMessage(protocol.respond(Respond.END,
                            clientId.toString()));
                    shutdown();
                    return;
                } else if (message.header.requestType == Request.EAR) {
                    // find target client
                    ClientStatus cs = clientMap.get(message.header.targetId);
                    if (cs != null) {
                        cs.followersId.put(clientId, 0);
                        sendMessage(protocol.forward(""
                                + message.header.targetId, cs.conn.getE(),
                                cs.conn.getC()));
                    } else {
                        sendMessage(protocol.respondBad(clientId.toString(),
                                "Failed to eavesdrop"));
                    }
                } else if (message.header.requestType == Request.CHAT) {
                    sendMessage(
                            protocol.respond(Respond.OK, clientId.toString()),
                            message.rawContent);
                    // send copy to followers
                    for (Integer id : followersId.keySet()) {
                        ClientStatus cs = clientMap.get(id);
                        if (cs != null && cs.conn != null) {
                            try {
                                cs.conn.sendMessage(protocol.forward(
                                        clientId.toString(),
                                        cs.conn.encryption.getE(),
                                        cs.conn.encryption.getC()),
                                        message.rawContent);
                            } catch (IOException e) {
                            }
                        }
                    }
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
            endClient("Corrupted request");
        } catch (InvalidKeyException e) {
            // should not happen
            e.printStackTrace();
        } catch (InvalidHeaderException e) {
            // end
            endClient("Corrupted request");
        } finally {
            // remove from client map
            if (clientMap != null)
                clientMap.remove(clientId);
            // remove from follower list

            broadcastClientInfo();
        }
    }

    private void endClient(final String errorMsg) {
        try {
            sendMessage(protocol.respondBad(clientId.toString(), errorMsg));
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

    private void broadcastClientInfo() {
        if (clientMap == null)
            return;
        for (ClientStatus client : clientMap.values()) {
            try {
                client.conn.sendMessage(protocol.broadcastClientInfo(clientMap
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
