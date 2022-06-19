package de.nerixyz.anker.io.codec;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nerixyz.anker.proto.Protocol;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Log4j2
public class Decoder<T> extends Thread {
    private static final int MAX_PAYLOAD_LENGTH = 1048576; // 2^20
    private final @NonNull InputStream istream;
    private final @NonNull CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
    private final @NonNull Class<T> rootJsonClass;
    private final @NonNull Listener<T> listener;

    public Decoder(@NonNull InputStream istream, @NonNull Class<T> rootJsonClass, @NonNull Listener<T> listener) {
        super();
        this.istream = istream;
        this.rootJsonClass = rootJsonClass;
        this.listener = listener;
        setName("Decoder-" + getId());
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Part 1: Read flags and length
                var flagsAndLength = istream.readNBytes(4);
                if (flagsAndLength.length < 4) {
                    log.debug("Not enough bytes for length, got {}, exiting", flagsAndLength.length);
                    break;
                }
                var flags = flagsAndLength[0];
                var length = (flagsAndLength[1] << 16) | (flagsAndLength[2] << 8) | (flagsAndLength[3]);
                if (length < 0 || length > MAX_PAYLOAD_LENGTH) {
                    log.warn("bad payload length {}", length);
                    break;
                }
                // Part 2: Read payload hash
                var hash = istream.readNBytes(32);
                if (hash.length < 32) {
                    log.debug("Not enough bytes for hash, got {}, exiting", hash.length);
                    break;
                }

                // Part 3: Read payload
                var payload = istream.readNBytes(length);
                if (payload.length < length) {
                    log.debug("Not enough bytes for payload, expected {}, got {}, exiting", length, payload.length);
                    break;
                }

                if (!Arrays.equals(hash, Protocol.computeHash(payload))) {
                    log.error("Mismatched hash");
                    break;
                }
                if (flags != 0) {
                    log.warn("Got payload with non-zero flags: flags={}", flags);
                    continue;
                }
                try {
                    var packet = new ObjectMapper().readValue(payload, rootJsonClass);
                    log.debug("received: {}", packet);
                    listener.onPacket(packet);
                } catch (JacksonException e) {
                    log.warn("Couldn't decode payload", e);
                } catch (Exception e) {
                    log.warn("Couldn't handle payload", e);
                }
            }
        } catch (ClosedByInterruptException e) {
            log.debug("Server thread interrupted, ending", e);
        } catch (SocketException e) {
            log.debug("Encountered SocketException, socket was probably closed ({})", e.getLocalizedMessage());
        } catch (IOException e) {
            log.error("Unexpected IoException", e);
        }
        try {
            istream.close();
        } catch (IOException e) {
            log.warn("Couldn't close input stream");
        }
        listener.onClose();
        log.debug("Exited.");
    }

    public interface Listener<T> {
        void onPacket(@NonNull T packet);

        void onClose();
    }
}
