package edu.cit595.qyccy.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class JListExtended extends JList implements ActionListener {

    private static final long serialVersionUID = -4430156041488818852L;

    private ClientGui parent = null;
    private boolean isEvesdrop = false;
    private boolean isCrack = false;

    public String selectedValue;

    JListExtended(ClientGui parent) {
        this.parent = parent;
        init();
    }

    private void init() {
        this.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // clean up client cache

                    JListExtended list = (JListExtended) e.getSource();
                    Object selected = list.getSelectedValue();
                    if (selected != null) {
                        list.selectedValue = selected.toString();
                    } else {
                        if (list.selectedValue != ClientGui.selfName)
                            parent.alert("Client list updated.");
                        ListModel items = list.getModel();
                        for (int i = 0; i < items.getSize(); i++) {
                            if (items.getElementAt(i).toString()
                                    .compareTo(ClientGui.selfName) == 0)
                                list.setSelectedIndex(i);
                        }
                        list.selectedValue = ClientGui.selfName;
                    }
                    if (list.selectedValue.compareTo(ClientGui.selfName) != 0) {
                        list.isEvesdrop = true;
                        list.isCrack = true;
                    } else {
                        list.isEvesdrop = false;
                        list.isCrack = false;
                    }
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        if (source.getText().compareTo(PopupMenu.eavesdrop) == 0) {
            // eavesdrop
            parent.sendFollowRequest(selectedValue
                    .substring(ClientGui.clientPrefix.length()));
        } else if (source.getText().compareTo(PopupMenu.crack) == 0) {
            // crack
            try {
                parent.crackPrivateKey(Integer.parseInt(selectedValue
                        .substring(ClientGui.clientPrefix.length())));
            } catch (NumberFormatException e1) {
                parent.alert("Client ID error");
            }
        }
    }

    public boolean getEvesdrop() {
        return isEvesdrop;
    }

    public boolean getCrack() {
        return isCrack;
    }

}
