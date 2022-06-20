package de.nerixyz.anker.proto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@ToString
@EqualsAndHashCode(callSuper = false)
public non-sealed class ChatMessage extends GameMessage {
    @Getter(onMethod_ = @JsonGetter)
    private final @NonNull String message;

    @JsonCreator
    public ChatMessage(@JsonProperty("message") @NonNull String message) {
        this.message = message;
    }
}
