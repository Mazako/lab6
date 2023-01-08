package client;

import model.CommunicationSignals;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatConnectionPanel extends JFrame implements KeyListener {
    private final User serverUser;
    private final User clientUser;
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 820;
    private final JPanel mainPanel = new JPanel();
    private final JTextArea promptTextArea = new JTextArea();
    private final JScrollPane scrollPane;
    private final JTextField commandField = new JTextField();

    public ChatConnectionPanel(User serverUser, User clientUser, Socket socket, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.serverUser = serverUser;
        this.clientUser = clientUser;
        this.socket = socket;
        this.out = out;
        this.in = in;

        this.setResizable(false);
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setTitle(clientUser.getName() + ": Czat z " + serverUser.getName());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainPanel.setBackground(Color.gray);

        promptTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,  18));
        promptTextArea.setEditable(false);
        promptTextArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(promptTextArea);
        scrollPane.setPreferredSize(new Dimension(750, 550));
        mainPanel.add(scrollPane);

        commandField.setPreferredSize(new Dimension(750, 28));
        commandField.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        commandField.addKeyListener(this);
        mainPanel.add(commandField);
        this.add(mainPanel);
        promptMessage("SERWER", "Rozpoczęto rozmowę z: " + serverUser.getName(), LocalTime.now() );
        new Thread(new MessageListener()).start();
        this.setVisible(true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    out.close();
                    in.close();
                    socket.close();
                    dispose();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                windowClosing(e);
            }
        });

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        Object source = e.getSource();
        if (source == commandField && e.getKeyCode() == KeyEvent.VK_ENTER) {
            String text = commandField.getText();
            try {
                out.writeObject(text);
                if (!text.isBlank()) {
                    promptMessage(clientUser.getName(), text, LocalTime.now());
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    private synchronized void promptMessage(String sender, String text, LocalTime time) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formatted = time.format(dateTimeFormatter);
        String message =  String.format("%s [%s] >>> %s\n", sender, formatted, text);
        promptTextArea.append(message);
        promptTextArea.setCaretPosition(promptTextArea.getDocument().getLength());
        commandField.setText("");
    }

    private class MessageListener implements Runnable{

        @Override
        public void run() {
            try {
                while (true) {
                    Object command = in.readObject();
                    if (command instanceof String) {
                        String message = (String) command;
                        promptMessage(serverUser.getName(), message, LocalTime.now());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                try {
                    promptMessage("SERWER", serverUser.getName() + " zakończył połączenie", LocalTime.now());
                    commandField.setEnabled(false);
                    socket.close();
                    out.close();
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
