package edu.cit595.qyccy.transfer;

import java.util.Set;

import edu.cit595.qyccy.model.Header;
import edu.cit595.qyccy.model.Request;
import edu.cit595.qyccy.model.Respond;

public enum Protocol {
    INSTANCE;

    public final String sep = " ";
    public final String endLine = "\r\n";

    public final String name = "MINIRSA";

    public final String pubKey = "Public-key:";
    public final String clientInfo = "Client-info:";
    public final String currentClients = "Current-clients:";

    public final String endHeader = "\r\n\r\n";

    public final String encoding = "UTF-8";
    public final int recvBuffSize = 1024;
    public final int byteSize = 256;
    public final int byteBit = 8;

    private Protocol() {
    }

    public final Header requestInit(final long e, final long c) {
        final Header header = new Header();
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(sep);
        sb.append(Request.INIT.toString());
        sb.append(endLine);

        sb.append(pubKey);
        sb.append(sep);
        sb.append(e);
        sb.append(sep);
        sb.append(c);
        sb.append(endLine);

        sb.append(endHeader);
        header.raw = sb.toString();
        return header;
    }

    public final Header requestChat() {
        final Header header = new Header();
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(sep);
        sb.append(Request.CHAT.toString());
        sb.append(endLine);

        sb.append(endHeader);
        header.raw = sb.toString();
        return header;
    }

    public final Header requestEnd() {
        final Header header = new Header();
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(sep);
        sb.append(Request.END.toString());
        sb.append(endLine);

        sb.append(endHeader);
        header.raw = sb.toString();
        return header;
    }

    public final Header respond(final Respond statusCode, final String client) {
        final Header header = new Header();
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(sep);
        sb.append(statusCode);
        sb.append(endLine);

        sb.append(clientInfo);
        sb.append(sep);
        sb.append(client);
        sb.append(endLine);
        
        sb.append(endHeader);
        header.raw = sb.toString();
        return header;
    }
    
    public final Header broadcastClientInfo(final Set<Integer> clients) {
        final Header header = new Header();
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(sep);
        sb.append(Respond.UPDT);
        sb.append(endLine);
        
        sb.append(currentClients);
        for(Integer i : clients) {
            sb.append(sep);
            sb.append(i);
        }
        sb.append(endLine);
        
        sb.append(endHeader);
        header.raw = sb.toString();
        return header;
    }
}
