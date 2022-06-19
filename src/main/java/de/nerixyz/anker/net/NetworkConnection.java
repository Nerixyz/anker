package de.nerixyz.anker.net;

import de.nerixyz.anker.io.codec.Decoder;
import de.nerixyz.anker.io.codec.Encoder;
import de.nerixyz.anker.proto.GameMessage;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public abstract class NetworkConnection implements Closeable {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final @NonNull AtomicReference<Listener> listener;
    private final @NonNull Object connectionLock = new Object();
    private Decoder<GameMessage> decoder;
    private Encoder encoder;

    public NetworkConnection(Listener listener) {
        this.listener = new AtomicReference<>(listener);
    }

    public abstract void startConnection();

    public void sendMessage(@NonNull GameMessage message) {
        if (encoder != null) {
            encoder.write(message);
        } else {
            log.warn("Discarding message {}, there's no connection currently", message);
        }
    }

    protected void start(InputStream is, OutputStream os) {
        if (isClosed.get())
            return;
        try {
            synchronized (connectionLock) {
                encoder = new Encoder(os);
                decoder = new Decoder<>(is, GameMessage.class, new DecoderListener(this));
                encoder.start();
                decoder.start();
                log.debug("Starting {} and {}", encoder.getName(), decoder.getName());
                var ref = listener.get();
                if (ref != null)
                    ref.onConnection();
            }
        } catch (Exception e) {
            log.fatal("Cannot create encoder and decoder", e);
            close();
        }
    }

    protected void closeCodec() {
        synchronized (connectionLock) {
            var notifyListener = (encoder != null || decoder != null) && !isClosed.get();
            if (encoder != null) {
                encoder.interrupt();
                encoder = null;
            }
            if (decoder != null) {
                decoder.interrupt();
                decoder = null;
            }
            if (notifyListener) {
                var ref = listener.get();
                if (ref != null)
                    ref.onSingleConnectionClosed();
            }
        }
    }

    @Override
    public void close() {
        if (!isClosed.compareAndSet(false, true))
            return;
        closeCodec();
        onClosed();
        var reference = listener.get();
        if (reference != null)
            reference.onClosed();
    }

    protected abstract void onDecoderClosed();

    protected abstract void onClosed();

    public void setListener(Listener listener) {
        this.listener.set(listener);
    }

    public interface Listener {
        void onConnection();

        void onMessage(@NonNull GameMessage message);

        void onClosed();

        void onSingleConnectionClosed();
    }

    private record DecoderListener(@NonNull NetworkConnection self) implements Decoder.Listener<GameMessage> {

        @Override
        public void onPacket(@NonNull GameMessage packet) {
            if (self.isClosed.get())
                return; // make sure to not emit after we're closed
            var ref = self.listener.get();
            if (ref != null)
                ref.onMessage(packet); // TODO: figure out on which thread this should be sent
        }

        @Override
        public void onClose() {
            self.onDecoderClosed();
        }
    }
}
