package edu.cit595.qyccy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.cit595.qyccy.commons.Configs;
import edu.cit595.qyccy.commons.Encryption;

public class Server {

    private Configs configs = Configs.INSTANCE;
    private ServerSocket serverSocket = null;
    private ExecutorService executor = null;
    private boolean running = false;

    private ArrayList<Socket> sockets = null;

    public Server() {
        init();
    }

    private void init() {
        try {
            serverSocket = new ServerSocket(configs.serverPort);
            executor = Executors.newFixedThreadPool(configs.maxConnection);
            sockets = new ArrayList<Socket>();
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
                Encryption encryption = new Encryption();
                sockets.add(socket);
                executor.execute(new ServerConnection(socket, encryption));
            } catch (IOException e) {
                e.printStackTrace(); // may still run
                shutdown();
                System.exit(-1);
            }
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
            serverSocket = null;
            // refactor?
            for (Socket s : sockets)
                if (s != null)
                    s.close();
            executor.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
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
