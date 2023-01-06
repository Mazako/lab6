package client;

import model.ServerConnectionException;
import server.PhoneBookServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PhoneBookClient extends JFrame implements ActionListener, KeyListener {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;

    private final JPanel mainPanel = new JPanel();

    private ClientConnection clientConnection;
    private final JTextArea promptTextArea = new JTextArea();
    private final JScrollPane scrollPane;
    private final JTextField commandField = new JTextField();
    private final JLabel portJlabel = new JLabel("port: ");
    private final JLabel ipJlabel = new JLabel("adres ip: ");
    private final JTextField ipJTextField = new JTextField("127.0.0.1");
    private final JTextField portJTextField = new JTextField();
    private final JButton startButton = new JButton("Połącz się");

    public PhoneBookClient() {
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setSize(WIDTH, HEIGHT);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainPanel.setBackground(Color.gray);

        promptTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,  18));
        promptTextArea.setEditable(false);
        promptTextArea.append(">Czekam na połączenie z serwerem...\n");
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(promptTextArea);
        scrollPane.setPreferredSize(new Dimension(750, 550));
        mainPanel.add(scrollPane);

        commandField.setPreferredSize(new Dimension(750, 28));
        commandField.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        commandField.addKeyListener(this);
        commandField.setEnabled(false);
        mainPanel.add(commandField);

        ipJlabel.setPreferredSize(new Dimension(100, 200));
        ipJlabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        mainPanel.add(ipJlabel);

        ipJTextField.setPreferredSize(new Dimension(150, 28));
        ipJTextField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        mainPanel.add(ipJTextField);

        portJlabel.setPreferredSize(new Dimension(100, 28));
        portJlabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        portJlabel.setHorizontalAlignment(SwingConstants.RIGHT);
        mainPanel.add(portJlabel);


        portJTextField.setPreferredSize(new Dimension(150, 28));
        portJTextField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        portJTextField.setText(Integer.toString(PhoneBookServer.PORT));
        portJTextField.addKeyListener(this);
        mainPanel.add(portJTextField);

        startButton.setPreferredSize(new Dimension(200, 100));
        startButton.addActionListener(this);
        mainPanel.add(startButton);



        this.add(mainPanel);
        this.setVisible(true);
    }




    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if  (source == startButton) {
            String ip = ipJTextField.getText();
            int port = Integer.parseInt(portJTextField.getText());
            clientConnection = new ClientConnection(ip, port);
            new Thread(clientConnection).start();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        Object source = e.getSource();
        int keyCode = e.getKeyCode();
        if (source == commandField && keyCode == KeyEvent.VK_ENTER && !commandField.getText().isBlank()) {
            String command = commandField.getText();
            promptTextArea.append("KLIENT>> " + command + '\n');
            clientConnection.out.println(command);
            commandField.setText("");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class ClientConnection implements Runnable{

        private String ip;
        private int port;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public ClientConnection(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public void  closeConnection() throws IOException {
            in.close();
            out.close();
            socket.close();
        }

        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                promptTextArea.append("SERWER>>> Połączono" + "\n");
                commandField.setEnabled(true);
                startButton.setEnabled(false);
                ipJTextField.setEnabled(false);
                portJTextField.setEnabled(false);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        PhoneBookClient.this,
                        "Nie udało się połączyć z serwerem. Spróbuj ponownie",
                        "Błąd połączenia",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            try {
                while (true) {
                    String response = in.readLine();
                    promptTextArea.append("SERWER>>> " + response + "\n");
                    if ("Koniec.".equals(response)) {
                        closeConnection();
                        break;
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(PhoneBookClient.this,
                        "Fatalny błąd serwera. Program zostanie wyłączony",
                        "Fatalny Błąd",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

        }
    }
}
