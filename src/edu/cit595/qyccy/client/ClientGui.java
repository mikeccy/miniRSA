package edu.cit595.qyccy.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import edu.cit595.qyccy.commons.Connection;

public class ClientGui extends JFrame {

    private static final long serialVersionUID = 2572283678984390103L;
    
    private JScrollPane clientListPane;
    private JScrollPane chatroomAreaPane;
    private JScrollPane inputAreaPane;
    private JList clientList;
    private JTextArea chatroomArea;
    private JTextArea inputArea;
    
    private Connection connection = null;
    
    public ClientGui() {
        init();
    }
    
    private void init() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        clientListPane = new JScrollPane();
        chatroomAreaPane = new JScrollPane();
        inputAreaPane = new JScrollPane();
        clientList = new JList();
        chatroomArea = new JTextArea();
        inputArea = new JTextArea();
        
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                try {
                    if (connection != null) connection.shutdown();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
        
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
        inputAreaPane.setViewportView(inputArea);
        
        clientList.setModel(new AbstractListModel() {
            private static final long serialVersionUID = 3191260450118574110L;
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        clientListPane.setViewportView(clientList);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(inputAreaPane)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(chatroomAreaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(clientListPane, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(clientListPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                        .addComponent(chatroomAreaPane))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(inputAreaPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );

        pack();
    }
    
    public void setConnection(final Connection connection) {
        this.connection = connection;
    }
    
    public void display(final String content) {
        this.chatroomArea.append(content);
    }
    
    private void inputAreaKeyTyped(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
            String text = inputArea.getText();
            if (text != null && text.length() > 0) {
                if (connection != null) {
                    try {
                        connection.sendMessage(text);
                    } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

}
