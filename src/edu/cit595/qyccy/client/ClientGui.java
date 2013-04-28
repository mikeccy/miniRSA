package edu.cit595.qyccy.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import edu.cit595.qyccy.exception.InvalidKeyException;
import edu.cit595.qyccy.transfer.Connection;
import edu.cit595.qyccy.transfer.Protocol;

public class ClientGui extends JFrame {

    public static final String clientPrefix = "Client ";
    public static final String selfName = "Me";

    public static final String chatroomAreaTip = "Main display";
    public static final String inputAreaTip = " Enter secret text (UTF-8 support),\n Press Ctrl-Enter to send.";
    public static final String clientListTip = " List current connected clients,\n Select and right click to choose actions.";

    private static final long serialVersionUID = 2572283678984390103L;

    private Client parent = null;

    private JScrollPane clientListPane;
    private JScrollPane chatroomAreaPane;
    private JScrollPane inputAreaPane;
    private JListExtended clientList;
    private JTextArea chatroomArea;
    private JTextArea inputArea;
    private PopupMenu popupMenu;

    private Connection connection = null;

    private Protocol protocol = Protocol.INSTANCE;

    public ClientGui(final Client parent) {
        this.parent = parent;
        init();
    }

    private void init() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                    .getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }

        this.setResizable(false);

        clientListPane = new JScrollPane();
        chatroomAreaPane = new JScrollPane();
        inputAreaPane = new JScrollPane();
        clientList = new JListExtended(this);
        chatroomArea = new JTextArea();
        inputArea = new JTextArea();
        popupMenu = new PopupMenu(clientList);

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                try {
                    if (connection != null) {
                        connection.sendMessage(protocol.requestEnd());
                        connection.shutdown();
                    }
                    System.exit(0);
                } catch (IOException e) {
                    System.out.println("Socket closed");
                    System.exit(-1);
                } catch (InvalidKeyException e) {
                    // should not happen
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        chatroomArea.setToolTipText(chatroomAreaTip);
        chatroomArea.setColumns(20);
        chatroomArea.setRows(5);
        chatroomAreaPane.setViewportView(chatroomArea);

        inputArea.setColumns(20);
        inputArea.setRows(5);
        inputArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                inputAreaKeyTyped(e);
            }

            public void keyTyped(KeyEvent e) {
                inputAreaKeyTyped(e);
            }
        });
        inputArea.setToolTipText(inputAreaTip);
        inputAreaPane.setViewportView(inputArea);

        clientList.addMouseListener(popupMenu);
        clientList.setToolTipText(clientListTip);
        clientListPane.setViewportView(clientList);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(inputAreaPane)
                                                .addGroup(
                                                        layout.createSequentialGroup()
                                                                .addComponent(
                                                                        chatroomAreaPane,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        382,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(
                                                                        clientListPane,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        91,
                                                                        Short.MAX_VALUE)))
                                .addContainerGap()));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                        clientListPane,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        362, Short.MAX_VALUE)
                                                .addComponent(chatroomAreaPane))
                                .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(inputAreaPane,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));

        pack();
    }

    public void updateTitle() {
        this.setTitle(clientPrefix + parent.getClientId());
    }
    @SuppressWarnings("unchecked")
    public void updateAllClient(final Set<Integer> clients) {
        Vector<String> v = new Vector<String>();
        for (Integer i : clients) {
            if (i != parent.getClientId()) {
                v.add(clientPrefix + i);
            } else {
                v.add(selfName);
            }
        }
        Collections.sort(v);
        this.clientList.setListData(v);
        this.clientList.validate();
    }

    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    public void display(final String content) {
        this.chatroomArea.append(content);
        JScrollBar vertical = chatroomAreaPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public void alert(final String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void sendFollowRequest(final String clientId) {
        this.parent.sendFollowRequest(clientId);
    }

    public void crackPrivateKey(final int clientId) {
        this.parent.crackPrivateKey(clientId);
    }

    public String askForPrime() {
        String input = JOptionPane.showInputDialog(this,
                "Select m'th and n'th prime: m,n");
        if (input == null) {
            return "";
        } else {
            return input;
        }
    }

    private void inputAreaKeyTyped(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
            String text = inputArea.getText();
            if (text != null && text.length() > 0) {
                if (text.length() > 200) {
                    alert("Message too long.");
                    return;
                }
                if (connection != null) {
                    try {
                        connection.sendMessage(text);
                    } catch (UnsupportedEncodingException e1) {
                        // should not happen
                        e1.printStackTrace();
                        System.exit(-1);
                    } catch (IOException e1) {
                        System.out.println("Socket closed");
                        this.dispatchEvent(new WindowEvent(this,
                                WindowEvent.WINDOW_CLOSING));
                    } catch (InvalidKeyException e1) {
                        // should not happen
                        e1.printStackTrace();
                        System.exit(-1);
                    }
                }
            } else {
                alert("Message should not be empty.");
            }
        }
    }

}
