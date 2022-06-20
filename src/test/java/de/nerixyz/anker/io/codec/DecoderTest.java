package de.nerixyz.anker.io.codec;

import de.nerixyz.anker.proto.*;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static de.nerixyz.anker.io.codec.Utils.decodeBase64;

public class DecoderTest {
    private static final String VALID_FRAMES = "AAAAHJlro5Ai8vSBNI/be2dZ6ZEYb5le7ZC" +
                                               "+cz9uw2kpcon7eyJjaGFuZ2UtbmFtZSI6eyJuYW1lIjoiYSJ9fQAAABk7" +
                                               "+bL5qOaAos4M3QRatHXHjBucmNJI3" +
                                               "/dQJ3D1P7KLwXsiY2hhdCI6eyJtZXNzYWdlIjoiaGkifX0AAAAcYVj6tlhvro9ZDh1dSnU9h/kAaZccb3vBE6DpL1CgOiZ7ImNoYW5nZS1uYW1lIjp7Im5hbWUiOiJiIn19";
    private static final String INVALID_FRAMES = "AAAAHJlro5Ai8vSBNI/be2dZ6ZEYb5le7ZC" +
                                                 "+cz9uw2kpcon7eyJjaGFuZ2UtbmFtZSI6eyJuYW1lIjoiYSJ9fQAAABk7" +
                                                 "+bL5qOaAos5M3QRatHXHjBucmNJI3" +
                                                 "/dQJ3D1P7KLwXsiY2hhdCI6eyJtZXNzYWdlIjoiaGkifX0AAAAcYVj6tlhvro9ZDh1dSnU9h/kAaZccb3vBE6DpL1CgOiZ7ImNoYW5nZS1uYW1lIjp7Im5hbWUiOiJiIn19";

    @Test
    public void validFrames() throws ExecutionException, InterruptedException {
        var listener = new DecoderListener();
        var decoder = new Decoder<>(new ByteArrayInputStream(decodeBase64(VALID_FRAMES)), GameMessage.class, listener);
        decoder.start();
        listener.getCloseFut().get();
        Assertions.assertEquals(List.of(new ChangeNameMessage("a"), new ChatMessage("hi"), new ChangeNameMessage("b")),
                                listener.receivedMessages);
    }

    @Test
    public void invalidFrame() throws ExecutionException, InterruptedException {
        var listener = new DecoderListener();
        var decoder = new Decoder<>(new ByteArrayInputStream(decodeBase64(INVALID_FRAMES)),
                                    GameMessage.class,
                                    listener);
        decoder.start();
        listener.getCloseFut().get();
        Assertions.assertEquals(List.of(new ChangeNameMessage("a")), listener.receivedMessages);
    }

    private static class DecoderListener implements Decoder.Listener<GameMessage> {
        @Getter
        private final List<@NonNull GameMessage> receivedMessages = new ArrayList<>();
        @Getter
        private final CompletableFuture<Void> closeFut = new CompletableFuture<>();

        @Override
        public synchronized void onPacket(@NonNull GameMessage packet) {
            receivedMessages.add(packet);
        }

        @Override
        public synchronized void onClose() {
            closeFut.complete(null);
        }
    }
}
