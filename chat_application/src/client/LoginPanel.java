package client;

import model.InvalidUsernameException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

class LoginPanel extends JDialog implements ActionListener {

    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;
    public static final Font LABEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 18);
    public static final Dimension TEXT_FIELD_PREF_SIZE = new Dimension(200, 24);
    public static final Dimension DIV_PREF_SIZE = new Dimension(390, 40);

    private final JPanel mainPane = new JPanel();
    private final JPanel userNameDiv = new JPanel();
    private final JLabel userNameLabel = new JLabel("Nazwa użytkownika: ");
    private final JTextField userNameField = new JTextField();
    private final JPanel ipDiv = new JPanel();
    private final JLabel ipLabel = new JLabel("Adres terminala: ");
    private final JTextField ipField = new JTextField("127.0.0.1");
    private final JPanel portDiv = new JPanel();
    private final JLabel portLabel = new JLabel("Port terminala: ");
    private final JTextField portField = new JTextField(Integer.toString(6666));

    private final JButton connectButton = new JButton("Połącz");


    public LoginPanel() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setTitle("Logowanie do czatu");
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        mainPane.setLayout(new FlowLayout(FlowLayout.CENTER));

        userNameDiv.setPreferredSize(DIV_PREF_SIZE);
        userNameDiv.setLayout(new FlowLayout(FlowLayout.TRAILING));
        userNameLabel.setFont(LABEL_FONT);
        userNameField.setPreferredSize(TEXT_FIELD_PREF_SIZE);
        userNameField.setFont(LABEL_FONT);
        userNameDiv.add(userNameLabel);
        userNameDiv.add(userNameField);
        mainPane.add(userNameDiv);

        ipDiv.setPreferredSize(DIV_PREF_SIZE);
        ipDiv.setLayout(new FlowLayout(FlowLayout.TRAILING));
        ipLabel.setFont(LABEL_FONT);
        ipField.setPreferredSize(TEXT_FIELD_PREF_SIZE);
        ipField.setFont(LABEL_FONT);
        ipDiv.add(ipLabel);
        ipDiv.add(ipField);
        mainPane.add(ipDiv);

        portDiv.setPreferredSize(DIV_PREF_SIZE);
        portDiv.setLayout(new FlowLayout(FlowLayout.TRAILING));
        portLabel.setFont(LABEL_FONT);
        portField.setPreferredSize(TEXT_FIELD_PREF_SIZE);
        portField.setFont(LABEL_FONT);
        portDiv.add(portLabel);
        portDiv.add(portField);
        mainPane.add(portDiv);

        connectButton.setPreferredSize(new Dimension(300, 100));
        connectButton.addActionListener(this);
        mainPane.add(connectButton);
        this.add(mainPane);

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == connectButton) {
            try {
                String userName = userNameField.getText();
                if (userName.isBlank()) {
                    throw new InvalidUsernameException("Pole użytkownika nie może być puste");
                }
                String ip = ipField.getText();
                int port = Integer.parseInt(portField.getText());
                Socket socket = new Socket(ip, port);
                new ClientPanel(socket, userName);
                this.dispose();
            } catch (InvalidUsernameException exception) {
                JOptionPane.showMessageDialog(
                        this,
                        exception.getMessage(),
                        "BŁĄD",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nie Udało połączyć się z serwerem. Spróbuj ponownie",
                        "BŁĄD",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Niepoprawny numer portu",
                        "BŁĄD",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

    }
}
