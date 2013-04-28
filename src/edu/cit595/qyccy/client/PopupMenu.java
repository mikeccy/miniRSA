package edu.cit595.qyccy.client;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class PopupMenu extends MouseAdapter {

    public static final String eavesdrop = "Eavesdrop";
    public static final String crack = "Crack";

    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem evesdropMenu = new JMenuItem(eavesdrop);
    private JMenuItem crackMenu = new JMenuItem(crack);
    private JListExtended source = null;

    PopupMenu(JListExtended source) {
        this.source = source;
        evesdropMenu.addActionListener(source);
        crackMenu.addActionListener(source);
    }

    private void setActive(boolean isEvesdrop, boolean isCrack) {
        if (isEvesdrop) {
            popup.add(evesdropMenu);
        } else {
            popup.remove(evesdropMenu);
        }
        if (isCrack) {
            popup.add(crackMenu);
        } else {
            popup.remove(crackMenu);
        }
        popup.validate();
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            final JList jlist = (JList) e.getComponent();
            final int index = jlist.getSelectedIndex();
            final Rectangle bounds = jlist.getCellBounds(index, index);
            if (bounds != null && bounds.contains(e.getX(), e.getY())) {
                setActive(source.getEvesdrop(), source.getCrack());
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
