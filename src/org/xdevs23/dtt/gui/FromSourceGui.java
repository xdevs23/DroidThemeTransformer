package org.xdevs23.dtt.gui;

import org.xdevs23.cli.dtt.transformer.ThemeTransformer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class FromSourceGui {

    public static JTextArea consoleBox = null;

    JTextField inputSrcTxt, outputSrcTxt;

    public FromSourceGui() {
        startGui();
    }

    public void startGui() {
        JFrame frame = new JFrame("From source - DroidThemeTransformer");

        int
                dm      = 16,   // Default margin
                lbw     = 240,  // Default label width
                lbh     = 18,   // Default label height
                btnw    = 200,  // Default button width
                btnh    = 32,   // Default button height
                frmw    = 640,  // Default frame width
                frmh    = 560   // Default frame height
                        ;

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(640, 560));
        frame.setMaximumSize(new Dimension(640, 560));
        frame.setSize(640, 560);

        frame.pack();
        frame.setVisible(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);

        frame.add(mainPanel);
        frame.invalidate();

        JLabel inputSrcLbl  = new JLabel("Overlays folder of CM theme");
        JLabel outputSrcLbl = new JLabel("Output folder for Substratum theme");
        mainPanel.add(inputSrcLbl);
        mainPanel.add(outputSrcLbl);

        inputSrcLbl .setBounds(dm, dm, lbw, lbh);
        outputSrcLbl.setBounds(dm, inputSrcLbl.getBounds().height + dm,
                inputSrcLbl.getBounds().width, inputSrcLbl.getBounds().height);

        inputSrcTxt  = new JTextField();
        outputSrcTxt = new JTextField();
        mainPanel.add(inputSrcTxt);
        mainPanel.add(outputSrcTxt);

        inputSrcTxt.setBounds(inputSrcLbl.getBounds().x + inputSrcLbl.getBounds().width + dm,
                inputSrcLbl.getBounds().y, lbw, lbh);
        outputSrcTxt.setBounds(outputSrcLbl.getBounds().x + outputSrcLbl.getBounds().width + dm,
                outputSrcLbl.getBounds().y, lbw, lbh);

        JButton chooseCmBtn = new JButton("Browse");
        mainPanel.add(chooseCmBtn);

        chooseCmBtn.setBounds(inputSrcTxt.getBounds().x + inputSrcTxt.getBounds().width + dm,
                inputSrcTxt.getBounds().y, btnw/2, lbh);
        chooseCmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setDialogTitle("Choose CM overlay directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                if(chooser.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION)
                    inputSrcTxt.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton chooseLrsBtn = new JButton("Browse");
        mainPanel.add(chooseLrsBtn);

        chooseLrsBtn.setBounds(outputSrcTxt.getBounds().x + outputSrcTxt.getBounds().width + dm,
                outputSrcTxt.getBounds().y, btnw/2, lbh);
        chooseLrsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setDialogTitle("Choose Substratum output directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                if(chooser.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION)
                    outputSrcTxt.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton startTransformBtn = new JButton("Start transform");
        mainPanel.add(startTransformBtn);

        startTransformBtn.setBounds(outputSrcTxt.getBounds().x,
                outputSrcTxt.getBounds().y + outputSrcTxt.getBounds().height + dm, btnw, btnh);
        startTransformBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThemeTransformer.startNewTransform(new String[] {
                        "to=oms",
                        "id=" + inputSrcTxt.getText(),
                        "od=" + outputSrcTxt.getText()
                });
            }
        });

        consoleBox = new JTextArea();
        mainPanel.add(consoleBox);

        consoleBox.setBounds(dm, startTransformBtn.getBounds().y + dm*2, frmw - dm*3, frmh - dm*3);
        consoleBox.setText("Console\n\n");

        mainPanel.updateUI();

        frame.pack();
        frame.invalidate();
    }

    public static void appendToConsole(String msg) {
        consoleBox.setText(consoleBox.getText() + msg);
    }

}
