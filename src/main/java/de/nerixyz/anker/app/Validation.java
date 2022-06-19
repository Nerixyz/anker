package de.nerixyz.anker.app;

import lombok.NonNull;

import java.util.regex.Pattern;

public class Validation {
    private static final Pattern USERNAME_REGEX = Pattern.compile("^\\w+$");

    private Validation() {
    }

    public static boolean validateUsername(@NonNull String username) {
        return USERNAME_REGEX.matcher(username).matches();
    }

    public static int parsePort(@NonNull String portStr) {
        try {
            var port = Integer.parseUnsignedInt(portStr);
            return port < 65536 ? port : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
