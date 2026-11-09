package org.bsu.graphics.raster_algo.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

class Form extends JPanel {

    private int row;

    Form(String title) {
        super(new GridBagLayout());
        if (title != null) {
            setBorder(BorderFactory.createTitledBorder(title));
        }
    }

    JLabel row(String label, JComponent component) {
        JLabel l = new JLabel(label);
        GridBagConstraints c = constraints();
        c.gridx = 0;
        add(l, c);
        c = constraints();
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        component.setPreferredSize(new Dimension(120, component.getPreferredSize().height));
        add(component, c);
        row++;
        return l;
    }

    void full(JComponent component) {
        GridBagConstraints c = constraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(component, c);
        row++;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }
}
