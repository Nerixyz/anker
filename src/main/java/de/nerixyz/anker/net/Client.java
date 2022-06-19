package de.nerixyz.anker.net;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.Socket;

@Log4j2
public class Client extends NetworkConnection {
    private final int port;
    private Socket socket;

    public Client(int port) {
        super(null);
        this.port = port;
    }

    @Override
    public void startConnection() {
        try {
            socket = new Socket("127.0.0.1", port);
            start(socket.getInputStream(), socket.getOutputStream());
        } catch (IOException e) {
            log.warn("Couldn't start, closing", e);
            close();
        }
    }

    @Override
    protected void onDecoderClosed() {
        // This means the socket was closed, and we cannot reconnect
        // thus we close
        close();
    }

    @Override
    protected void onClosed() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                log.warn("Failed to close socket", e);
            }
        }
    }
}
