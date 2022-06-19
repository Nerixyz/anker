package de.nerixyz.anker.io.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nerixyz.anker.proto.Protocol;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class Encoder extends Thread {
    private final @NonNull OutputStream ostream;
    private final @NonNull BlockingQueue<? super Object> writeQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public Encoder(@NonNull OutputStream ostream) {
        this.ostream = ostream;
        setName("Encoder-" + getId());
    }

    @Override
    public void run() {
        try {
            var lengthBytes = new byte[4];
            while (!isClosed.get()) {
                var obj = writeQueue.take();
                try {
                    var payload = new ObjectMapper().writeValueAsBytes(obj);
                    lengthBytes[0] = 0; // flags
                    lengthBytes[1] = (byte) ((payload.length >> 16) & 0xff);
                    lengthBytes[2] = (byte) ((payload.length >> 8) & 0xff);
                    lengthBytes[3] = (byte) (payload.length & 0xff);
                    ostream.write(lengthBytes);
                    ostream.write(Protocol.computeHash(payload));
                    ostream.write(payload);
                    // make sure everything is sent
                    ostream.flush();
                } catch (JsonProcessingException e) {
                    log.error("Cannot serialize json", e);
                }
            }
            log.debug("Exited encoder worker");
        } catch (InterruptedException e) {
            log.debug("Interrupted, exiting");
        } catch (SocketException e) {
            log.debug("Encountered SocketException, socket was probably closed ({})", e.getLocalizedMessage());
        } catch (IOException e) {
            log.warn("Encountered IOException when encoding", e);
        }
        isClosed.set(true);
        try {
            ostream.close();
        } catch (IOException e) {
            log.warn("Couldn't close output stream", e);
        }
        log.debug("Exited.");
    }

    public <T> void write(@NonNull T obj) {
        if (isClosed.get())
            return;
        try {
            writeQueue.put(obj);
        } catch (InterruptedException e) {
            log.error("Got interrupted while putting an item into the queue, this should never happen", e);
        }
    }
}
