package edu.cit595.qyccy.model;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cit595.qyccy.exception.InvalidHeaderException;
import edu.cit595.qyccy.transfer.Encryption;
import edu.cit595.qyccy.transfer.Protocol;

public class Header {

    private Pattern pubKey = Pattern.compile("^" + Protocol.INSTANCE.pubKey
            + Protocol.INSTANCE.sep + "([0-9]+)" + Protocol.INSTANCE.sep
            + "([0-9]+)$");
    private Pattern clientInfo = Pattern.compile("^"
            + Protocol.INSTANCE.clientInfo + Protocol.INSTANCE.sep
            + "([0-9]+)$");
    private Pattern errorMsg = Pattern.compile("^" + Protocol.INSTANCE.errorMsg
            + Protocol.INSTANCE.sep + "(.*)$");
    private Pattern allClients = Pattern.compile("^"
            + Protocol.INSTANCE.currentClients + "((" + Protocol.INSTANCE.sep
            + "[0-9]+)+)$");

    public String raw = null;

    public Request requestType = null;
    public Respond respondType = null;

    public String error = null; // bad request

    public long pubKeyE = Encryption.notSet;
    public long pubKeyC = Encryption.notSet;

    public int clientId;

    public int targetId;
    public long targetE;
    public long targetC;

    public Set<Integer> clients = new HashSet<Integer>();

    private Protocol protocol = Protocol.INSTANCE;

    public void parseRequestHeader() throws InvalidHeaderException {
        String[] strLine = raw.split(protocol.endLine);
        if (strLine.length < 1)
            throw new InvalidHeaderException();
        String[] status = strLine[0].split(protocol.sep);
        if (status.length != 2)
            throw new InvalidHeaderException();
        requestType = Request.UNKN;
        if (status[1].compareTo(Request.CHAT.toString()) == 0) {
            // normal request
            requestType = Request.CHAT;
            return;
        } else if (status[1].compareTo(Request.END.toString()) == 0) {
            // end request
            requestType = Request.END;
            return;
        } else if (status[1].compareTo(Request.INIT.toString()) == 0) {
            // init request
            if (strLine.length != 2)
                throw new InvalidHeaderException();
            Matcher m = pubKey.matcher(strLine[1]);
            if (m.find()) {
                pubKeyE = Long.parseLong(m.group(1));
                pubKeyC = Long.parseLong(m.group(2));
            }
            requestType = Request.INIT;
            return;
        } else if (status[1].compareTo(Request.EAR.toString()) == 0) {
            // eavesdrop request
            if (strLine.length != 2)
                throw new InvalidHeaderException();
            Matcher clientIdM = clientInfo.matcher(strLine[1]);
            if (clientIdM.find()) {
                targetId = Integer.parseInt(clientIdM.group(1));
            } else {
                throw new InvalidHeaderException();
            }
            requestType = Request.EAR;
            return;
        }
        // parse failure
        throw new InvalidHeaderException();
    }

    public void parseResponseHeader() throws InvalidHeaderException {
        String[] strLine = raw.split(protocol.endLine);
        if (strLine.length < 1)
            throw new InvalidHeaderException();
        String[] status = strLine[0].split(protocol.sep);
        if (status.length < 2)
            throw new InvalidHeaderException();
        if (status[0].compareTo(protocol.name) == 0) {
            // broadcast respond
            if (status[1].compareTo(Respond.UPDT.toString()) == 0) {
                respondType = Respond.UPDT;
                // all clients
                if (strLine.length < 2)
                    throw new InvalidHeaderException();
                Matcher clientsM = allClients.matcher(strLine[1]);
                if (clientsM.find()) {
                    String[] cs = clientsM.group(1).split(protocol.sep);
                    for (String str : cs) {
                        if (str.length() > 0)
                            clients.add(Integer.valueOf(str));
                    }
                } else {
                    throw new InvalidHeaderException();
                }
                return;
            } else if (status[1].compareTo(Respond.OK.toString()) == 0) {
                respondType = Respond.OK;
                // client id
                if (strLine.length < 2)
                    throw new InvalidHeaderException();
                Matcher clientIdM = clientInfo.matcher(strLine[1]);
                if (clientIdM.find()) {
                    clientId = Integer.parseInt(clientIdM.group(1));
                } else {
                    throw new InvalidHeaderException();
                }
                return;
            } else if (status[1].compareTo(Respond.END.toString()) == 0) {
                respondType = Respond.END;
                return;
            } else if (status[1].compareTo(Respond.BAD.toString()) == 0) {
                respondType = Respond.BAD;
                // error message
                if (strLine.length < 2)
                    throw new InvalidHeaderException();
                Matcher errorMsgM = errorMsg.matcher(strLine[1]);
                if (errorMsgM.find()) {
                    error = errorMsgM.group(1);
                } else {
                    throw new InvalidHeaderException();
                }
                return;
            } else if (status[1].compareTo(Respond.FWD.toString()) == 0) {
                respondType = Respond.FWD;
                // target id
                if (strLine.length < 3)
                    throw new InvalidHeaderException();
                Matcher targetIdM = clientInfo.matcher(strLine[1]);
                if (targetIdM.find()) {
                    targetId = Integer.parseInt(targetIdM.group(1));
                } else {
                    throw new InvalidHeaderException();
                }
                // target public key
                Matcher m = pubKey.matcher(strLine[2]);
                if (m.find()) {
                    targetE = Long.parseLong(m.group(1));
                    targetC = Long.parseLong(m.group(2));
                }
                return;
            } else {
                throw new InvalidHeaderException();
            }
        }
        // parse failure
        throw new InvalidHeaderException();
    }

}
