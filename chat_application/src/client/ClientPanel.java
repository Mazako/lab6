package client;

import model.CommunicationSignals;
import model.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientPanel extends JFrame implements ActionListener {
    public static final int WIDTH = 200;
    public static final int HEIGHT = 100;

    private final User user;
    private final Socket terminalSocket;
    private ObjectInputStream terminalIn;
    private ObjectOutputStream terminalOut;
    private JComboBox<User> activeUsersComboBox = new JComboBox<>();

    private ConnectionWithTerminalController connectionWithTerminalController = new ConnectionWithTerminalController();

    public ClientPanel(Socket socket, String userName) throws IOException {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        user = new User(userName);
        this.terminalSocket = socket;
        try {
            terminalIn = new ObjectInputStream(socket.getInputStream());
            terminalOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Błąd połaczenia z serwerem. Program zostanie zamknięty",
                    "BŁĄD",
                    JOptionPane.ERROR_MESSAGE
            );
            socket.close();
        }
        terminalOut.writeObject(CommunicationSignals.SAVE_ME);
        terminalOut.writeObject(user);
        new Thread(connectionWithTerminalController).start();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    connectionWithTerminalController.isConnectedWithTerminal = false;
                    synchronized (terminalOut) {
                        terminalOut.writeObject(CommunicationSignals.CLOSE_TERMINAL_CONNECTION);
                        Object object = terminalIn.readObject();
                        terminalOut.close();
                        terminalIn.close();
                        terminalSocket.close();
                    }

                } catch (IOException | ClassNotFoundException ex) {

                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                windowClosing(e);
            }
        });
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setTitle("BlaBla BlaBla - " + user.getName());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.add(activeUsersComboBox);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private class ConnectionWithTerminalController implements Runnable {
        volatile boolean isConnectedWithTerminal = true;
        @Override
        public void run() {
            try {
                while (isConnectedWithTerminal) {
                    terminalOut.writeObject(CommunicationSignals.SEND_ACTIVE_USERS);
                    User[] users = (User[]) terminalIn.readObject();
                    SwingUtilities.invokeLater(() -> {
                        activeUsersComboBox.removeAllItems();
                        for (User u : users) {
                            activeUsersComboBox.addItem(u);
                        }
                    });
                    Thread.sleep(3000);
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
