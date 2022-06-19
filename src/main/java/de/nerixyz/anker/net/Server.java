package de.nerixyz.anker.net;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class Server extends NetworkConnection {
    private final int port;
    private final @NonNull AtomicReference<Socket> connectedSocket = new AtomicReference<>(null);
    private ServerSocket server;
    private Thread acceptorThread;

    public Server(int port) {
        super(null);
        this.port = port;
    }

    @Override
    public void startConnection() {
        if (acceptorThread != null || server != null)
            throw new IllegalStateException("Already running");

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Cannot bind to {}: {}", port, e.getLocalizedMessage());
            close();
            return;
        }

        acceptorThread = new Thread(() -> {
            try {
                while (true) {
                    var newSocket = server.accept();
                    if (!connectedSocket.compareAndSet(null, newSocket))
                        continue;
                    closeCodec();
                    start(newSocket.getInputStream(), newSocket.getOutputStream());
                }
            } catch (SocketException e) {
                log.debug("Encountered SocketException, server was probably closed ({})", e.getLocalizedMessage());
            } catch (Exception e) {
                log.warn("Encountered exception while accepting", e);
            }
            close();
            log.debug("Exited.");
        });
        acceptorThread.start();
        acceptorThread.setName("Acceptor-" + acceptorThread.getId());
        log.debug("Starting " + acceptorThread.getName());
    }

    @Override
    protected void onDecoderClosed() {
        connectedSocket.set(null);
        closeCodec();
    }

    @Override
    protected void onClosed() {
        if (server == null)
            return;
        if (!server.isClosed()) {
            try {
                server.close();
            } catch (IOException e) {
                log.warn("Couldn't close server", e);
            }
        }
    }
}
