package de.nerixyz.anker.io.codec;

import de.nerixyz.anker.proto.ChangeNameMessage;
import de.nerixyz.anker.proto.ChatMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static de.nerixyz.anker.io.codec.Utils.decodeBase64;

public class EncoderTest {
    private static final String VALID_FRAMES = "AAAAHJlro5Ai8vSBNI/be2dZ6ZEYb5le7ZC" +
                                               "+cz9uw2kpcon7eyJjaGFuZ2UtbmFtZSI6eyJuYW1lIjoiYSJ9fQAAABk7" +
                                               "+bL5qOaAos4M3QRatHXHjBucmNJI3" +
                                               "/dQJ3D1P7KLwXsiY2hhdCI6eyJtZXNzYWdlIjoiaGkifX0AAAAcYVj6tlhvro9ZDh1dSnU9h/kAaZccb3vBE6DpL1CgOiZ7ImNoYW5nZS1uYW1lIjp7Im5hbWUiOiJiIn19";

    @Test
    public void basic() throws InterruptedException {
        var os = new ByteArrayOutputStream();
        var encoder = new Encoder(os);
        encoder.start();
        encoder.write(new ChangeNameMessage("a"));
        encoder.write(new ChatMessage("hi"));
        encoder.write(new ChangeNameMessage("b"));
        Thread.sleep(200); // make sure encoder wrote everything
        encoder.interrupt(); // we can't simulate a closed OStream
        Assertions.assertArrayEquals(decodeBase64(VALID_FRAMES), os.toByteArray());
    }
}
