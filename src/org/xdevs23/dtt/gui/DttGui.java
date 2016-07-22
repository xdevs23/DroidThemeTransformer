package org.xdevs23.dtt.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class DttGui {

    public DttGui() {
        startGui();
    }

    private void startGui() {
        JFrame.setDefaultLookAndFeelDecorated(false);

        JFrame frame = new JFrame("DroidThemeTransformer");

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(320, 240));
        frame.setVisible(true);

        JPanel mainPanel = new JPanel();

        frame.add(mainPanel);

        JButton fromSourceBtn = new JButton("From source");
        fromSourceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FromSourceGui();
                frame.setVisible(false);
            }
        });

        fromSourceBtn.setLocation(16, 64);
        fromSourceBtn.setSize(100, 24);
        fromSourceBtn.setMaximumSize(fromSourceBtn.getSize());

        mainPanel.add(fromSourceBtn);
        mainPanel.updateUI();

        frame.invalidate();
        frame.pack();

    }

}
