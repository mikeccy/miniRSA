package edu.cit595.qyccy.server;

import java.util.concurrent.ConcurrentHashMap;

public class ClientStatus {
    public ServerConnection conn = null;
    public ConcurrentHashMap<Integer, Integer> followersId = null;

    public ClientStatus(final ServerConnection connection,
            final ConcurrentHashMap<Integer, Integer> followersId) {
        this.conn = connection;
        this.followersId = followersId;
    }
}
