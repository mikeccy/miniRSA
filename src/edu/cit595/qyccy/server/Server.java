package edu.cit595.qyccy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.cit595.qyccy.common.Configs;

public class Server {

    private Configs configs = Configs.INSTANCE;
    private ServerSocket serverSocket = null;
    private ExecutorService executor = null;
    private boolean running = false;

    private int counter = 0;

    private ArrayList<Socket> sockets = null;
    private Map<Integer, ServerConnection> clientMap = new ConcurrentHashMap<Integer, ServerConnection>();

    public Server() {
        init();
    }

    private void init() {
        try {
            serverSocket = new ServerSocket(configs.serverPort);
            executor = Executors.newFixedThreadPool(configs.maxConnection);
            sockets = new ArrayList<Socket>();
            System.out.println("Server running @" + configs.serverHost + ":"
                    + configs.serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void start() {
        running = true;
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                // negotiate public key
                sockets.add(socket);
                executor.execute(new ServerConnection(socket, this));
            } catch (IOException e) {
                e.printStackTrace();
                shutdown();
                System.exit(-1);
            }
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
            serverSocket = null;
            for (Socket s : sockets)
                if (s != null)
                    s.close();
            executor.shutdownNow();
        } catch (IOException e) {
            System.out.println("Socket closed");
        }
    }

    public int addNewClient(final ServerConnection conn) {
        while (true) {
            counter += 1;
            if (!clientMap.containsKey(counter)) {
                clientMap.put(counter, conn);
                return counter;
            }
        }
    }

    public final Map<Integer, ServerConnection> getClientMap() {
        return clientMap;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        server.shutdown();
    }

}
