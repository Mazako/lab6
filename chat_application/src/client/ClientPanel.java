/*
 *  Laboratorium 4
 *
 *   Autor: Michal Maziarz, 263913
 *    Data: Styczeń 2023 r.
 */
package client;

import model.CommunicationSignals;
import model.User;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ClientPanel extends JFrame implements ActionListener, PopupMenuListener {
    public static final int WIDTH = 530;
    public static final int HEIGHT = 100;

    private final User user;
    private final Socket terminalSocket;
    private ObjectInputStream terminalIn;
    private ObjectOutputStream terminalOut;
    private final JComboBox<User> activeUsersComboBox = new JComboBox<>();
    private final JLabel activeUsersLabel = new JLabel("Aktywni użytkownicy: ");
    private final JButton startConnectionButton = new JButton("Połącz się");
    private final ServerSocket serverSocket;
    private final List<User> activeUsers = Collections.synchronizedList(new ArrayList<>());
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuItem = new JMenu("Pomoc");
    private final JMenuItem aboutMenu = new JMenuItem("Opis");

    public ClientPanel(Socket socket, String userName) throws IOException {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        //SOCKET PART
        user = new User(userName, socket.getInetAddress().getHostAddress());
        this.terminalSocket = socket;
        try {
            terminalIn = new ObjectInputStream(socket.getInputStream());
            terminalOut = new ObjectOutputStream(socket.getOutputStream());
            terminalOut.writeObject(CommunicationSignals.SAVE_ME);
            terminalOut.writeObject(user);
            int port = (int) terminalIn.readObject();
            user.setPort(port);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Błąd połaczenia z serwerem. Program zostanie zamknięty",
                    "BŁĄD",
                    JOptionPane.ERROR_MESSAGE
            );
            socket.close();
            dispose();
        }
        serverSocket = new ServerSocket(user.getPort());
        System.out.println("USER - " + user);
        //END OF SOCKET PART
        //GRAPHICAL PART
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setTitle("BlaBla BlaBla - " + user.getName());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        activeUsersLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.add(activeUsersLabel);
        this.add(activeUsersComboBox);
        activeUsersComboBox.setPrototypeDisplayValue(new User("                                                          ", null));
        activeUsersComboBox.addPopupMenuListener(this);
        startConnectionButton.addActionListener(this);
        menuItem.add(aboutMenu);
        menuBar.add(menuItem);
        aboutMenu.addActionListener(this);
        this.setJMenuBar(menuBar);
        this.add(startConnectionButton);
        new Thread(new GetActiveUsersThread()).start();
        new Thread(new ConnectionListener()).start();
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    synchronized (terminalOut) {
                        terminalOut.writeObject(CommunicationSignals.CLOSE_TERMINAL_CONNECTION);
                        Object object = terminalIn.readObject();
                        terminalOut.close();
                        terminalIn.close();
                        terminalSocket.close();
                        serverSocket.close();
                        System.exit(0);
                    }

                } catch (IOException | ClassNotFoundException ex) {

                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                windowClosing(e);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == startConnectionButton) {
            User selectedUser = (User) activeUsersComboBox.getSelectedItem();
            if (selectedUser == null) {
                return;
            }
            synchronized (activeUsers) {
                if (activeUsers.contains(selectedUser)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Istnieje już połączenie z tym użytkownikiem: " + selectedUser.getName(),
                            "Nie mozna połączyć",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
            }
            try {
                Socket socket = new Socket(selectedUser.getAddress(), selectedUser.getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                out.writeObject(user.getPort());
                new ChatConnectionPanel(selectedUser, user, socket, out, in, activeUsers);
                synchronized (activeUsers) {
                    activeUsers.add(selectedUser);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nie można połaczyć się z uzytkownikiem",
                        "BŁĄD",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (source == aboutMenu) {
            JOptionPane.showMessageDialog(
                    this,
                    "Michal Maziarz, Laboratorium 6\n\nProsty program w stylu Gadu-Gadu wykorzystujący Sockety\nStyczeń 2023",
                    "O programie",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (e.getSource() == activeUsersComboBox) {
            new Thread(new GetActiveUsersThread()).start();
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {

    }

    private class GetActiveUsersThread implements Runnable {
        @Override
        public void run() {
            try {
                terminalOut.writeObject(CommunicationSignals.SEND_ACTIVE_USERS);
                User[] users = (User[]) terminalIn.readObject();
                SwingUtilities.invokeLater(() -> {
                    activeUsersComboBox.removeAllItems();
                    for (User u : users) {
                        if (!u.getName().equals(user.getName())) {
                            activeUsersComboBox.addItem(u);
                        }
                    }
                });
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ConnectionListener implements Runnable {

        @Override
        public void run() {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    int port = (int) in.readObject();
                    terminalOut.writeObject(CommunicationSignals.GET_USER_BY_PORT);
                    terminalOut.writeObject(port);
                    User searchedUser = (User) terminalIn.readObject();
                    new ChatConnectionPanel(searchedUser, user, socket, out, in, activeUsers);
                    synchronized (activeUsers) {
                        activeUsers.add(searchedUser);
                    }
                } catch (IOException | ClassNotFoundException e) {

                }
            }
        }
    }
}
