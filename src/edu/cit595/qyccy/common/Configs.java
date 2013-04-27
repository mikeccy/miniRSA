package edu.cit595.qyccy.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public enum Configs {
    INSTANCE;

    public final String serverHost;
    public final int serverPort;

    public final int maxConnection = 10;

    private Configs() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("configs/configs.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        serverHost = props.getProperty("server_host");
        serverPort = Integer.parseInt(props.getProperty("server_port"));
    }
}
