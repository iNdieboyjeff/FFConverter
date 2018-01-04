package uk.me.jeffsutton.ui;

import uk.me.jeffsutton.FFConvert;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jeff on 09/02/2017.
 */
public class FFBuilder {
    private JTextField textField1;
    private JButton btnDirectoryChooser;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JComboBox comboBox5;
    private JComboBox comboBox6;
    private JPanel panel1;
    private JButton PROCESSButton;
    private JTextPane textPane1;
    private JCheckBox generateScriptCheckBox;
    private JCheckBox appendToOutputCheckBox;
    private JCheckBox chkDownmix;
    private JComboBox comboBox7;
    private JCheckBox forceVideoEncodingCheckBox;
    private JComboBox comboBox8;
    private JComboBox comboBox9;
    private JComboBox comboBox10;
    private JCheckBox deinterlaceCheckBox;
    private JComboBox comboBox11;
    private JComboBox comboBox12;
    private JCheckBox copyVideoOnlyCheckBox;
    private JCheckBox copyAudioOnlyProcessCheckBox;

    public FFBuilder() {
        btnDirectoryChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                try {
                    chooser.setSelectedFile(new File(textField1.getText()));
                } catch (Exception ignored) {}
                int result = chooser.showOpenDialog(btnDirectoryChooser);

                if (result == JFileChooser.APPROVE_OPTION) {
                    textField1.setText(chooser.getSelectedFile().getAbsolutePath().toString());
                }
            }
        });
        PROCESSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = (FFConvert.getCommands(new File(textField1.getText()),
                        comboBox1.getSelectedItem().toString(),
                        comboBox6.getSelectedItem().toString(),
                        comboBox2.getSelectedItem().toString(),
                        comboBox3.getSelectedItem().toString(),
                        comboBox4.getSelectedItem().toString(),
                        comboBox5.getSelectedItem().toString(),
                        chkDownmix.isSelected(),
                        comboBox7.getSelectedItem().toString(),
                        forceVideoEncodingCheckBox.isSelected(),
                        comboBox8.getSelectedItem().toString(),
                        comboBox9.getSelectedItem().toString(),
                        comboBox10.getSelectedItem().toString(),
                        deinterlaceCheckBox.isSelected(),
                        comboBox11.getSelectedItem().toString(),
                        comboBox12.getSelectedItem().toString(),
                        copyVideoOnlyCheckBox.isSelected(),
                        copyAudioOnlyProcessCheckBox.isSelected()));

                if (appendToOutputCheckBox.isSelected()) {
                    textPane1.setText(textPane1.getText() + cmd);
                } else {
                    textPane1.setText(cmd);
                }

                if (generateScriptCheckBox.isSelected()) {
                    File f = new File(textField1.getText(), "do.sh");

                    try {
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(textPane1.getText().getBytes());
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    f.setExecutable(true);

                }
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("FFBuilder");
        frame.setContentPane(new FFBuilder().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(920, 650);
        frame.setVisible(true);
    }
}
