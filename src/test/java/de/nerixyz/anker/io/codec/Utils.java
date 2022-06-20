package de.nerixyz.anker.io.codec;

import lombok.NonNull;

import java.util.Base64;

public class Utils {
    private Utils() {
    }

    public static byte[] decodeBase64(@NonNull String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
