package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AllowedPortsPool {
    private LinkedBlockingQueue<Integer> ports;

    public AllowedPortsPool() {
        ports = IntStream.range(ActiveUsersTerminal.PORT + 1, ActiveUsersTerminal.PORT + 300)
                .boxed()
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
    }

    public int getNextPort() {
        return ports.poll();
    }

    public void addPort(int port) {
        ports.add(port);
    }
}