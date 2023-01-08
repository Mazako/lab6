package client;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

class ClientPanel extends JFrame implements ActionListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;

    private final User user;
    private final Socket socket;

    public ClientPanel(Socket socket, String userName) {
        user = new User(userName);
        this.socket = socket;

        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setTitle("BlaBla BlaBla - " + user.getName());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
