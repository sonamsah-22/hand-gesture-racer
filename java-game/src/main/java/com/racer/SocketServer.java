package com.racer;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;

public class SocketServer {
    private final int port;
    private final AtomicReference<String> latestCommand = new AtomicReference<>("NONE");
    private volatile boolean connected = false;

    public SocketServer(int port) { this.port = port; }

    public void startAsync() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Waiting for Python AI on port " + port);
                Socket client = server.accept();
                connected = true;
                System.out.println("Python AI connected!");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    latestCommand.set(line.trim());
                }
            } catch (IOException e) {
                System.err.println("Socket error: " + e.getMessage());
            }
        }, "socket-thread").start();
    }

    public String getCommand() { return latestCommand.get(); }
    public boolean isConnected() { return connected; }
}