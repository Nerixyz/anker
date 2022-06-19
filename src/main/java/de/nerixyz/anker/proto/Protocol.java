package de.nerixyz.anker.proto;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Log4j2
public class Protocol {
    private static final @NonNull String DIGEST_ALGORITHM = "SHA-256";

    private Protocol() {
    }

    private static @NonNull MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.fatal("Bad java installation", e);
            System.exit(1);
        }
        throw new RuntimeException("unreachable");
    }

    public static @NonNull byte[] computeHash(@NonNull byte[] payload) {
        var digest = createDigest();
        digest.update(payload);
        return digest.digest();
    }
}
