/*
 *  Laboratorium 6
 *
 *   Autor: Michal Maziarz, 263913
 *    Data: Styczeń 2023 r.
 */
package client;

import model.PhoneBook;
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

    private final JMenuBar menu = new JMenuBar();
        private final JMenu aboutMenu = new JMenu("O programie");
            private final JMenuItem aboutMenuItem = new JMenuItem("Autor");
            private final JMenuItem instructionsMenuItem = new JMenuItem("Instrukcja");
    public static final int WIDTH = 800;
    public static final int HEIGHT = 820;
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
        this.setResizable(false);
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setTitle("Klient do książeczki telefonicznej");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                    if (clientConnection != null) {
                        try {
                            clientConnection.closeConnection();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                windowClosing(e);
            }
        });
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainPanel.setBackground(Color.gray);

        promptTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,  18));
        promptTextArea.setEditable(false);
        promptTextArea.append(">Czekam na połączenie z serwerem...\n");
        promptTextArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(promptTextArea);
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


        aboutMenu.add(aboutMenuItem);
        aboutMenu.add(instructionsMenuItem);
        aboutMenuItem.addActionListener(this);
        instructionsMenuItem.addActionListener(this);
        menu.add(aboutMenu);
        this.setJMenuBar(menu);

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
        } else if (source == aboutMenuItem) {
            JOptionPane.showMessageDialog(this,
                    "Laboratorium 6\n\n" +
                    "Program będący klientem do książki telefonicznej\n" +
                    "Michał Maziarz, Styczeń 2023",
                    "O programie",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else if (source == instructionsMenuItem) {
            StringBuilder stringBuilder = new StringBuilder("Spis komend:\n\n");
            stringBuilder.append(PhoneBook.closeInstruction).append("\n")
                    .append(PhoneBook.byeInstruction).append("\n")
                    .append(PhoneBook.deleteInstruction).append("\n")
                    .append(PhoneBook.putInstruction).append("\n")
                    .append(PhoneBook.listInstruction).append("\n")
                    .append(PhoneBook.filesInstruction).append("\n")
                    .append(PhoneBook.getInstruction).append("\n")
                    .append(PhoneBook.replaceInstruction).append("\n")
                    .append(PhoneBook.loadInstruction).append("\n")
                    .append(PhoneBook.saveInstruction).append("\n");
            JOptionPane.showMessageDialog(this,
                    stringBuilder.toString(),
                    "Instrukcja",
                    JOptionPane.INFORMATION_MESSAGE);
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
            promptTextArea.append("KLIENT>>> " + command + '\n');
            clientConnection.out.println(command);
            commandField.setText("");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class ClientConnection implements Runnable{

        private final String ip;
        private final int port;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public ClientConnection(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public void  closeConnection() throws IOException {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
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
                return;
            }
            try {
                while (!socket.isClosed()) {
                    String response = in.readLine();
                    if ("STOP".equals(response)) {
                        closeConnection();
                        break;
                    }
                    promptTextArea.append("SERWER>>> " + response + "\n");
                    promptTextArea.setCaretPosition(promptTextArea.getDocument().getLength());
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
