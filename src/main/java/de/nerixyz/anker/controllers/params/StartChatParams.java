package de.nerixyz.anker.controllers.params;

import de.nerixyz.anker.net.NetworkConnection;
import lombok.NonNull;

public record StartChatParams(@NonNull String username, @NonNull NetworkConnection connection) {
}
