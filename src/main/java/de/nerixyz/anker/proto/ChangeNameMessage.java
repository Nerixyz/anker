package de.nerixyz.anker.proto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@ToString
public non-sealed class ChangeNameMessage extends GameMessage {
    @Getter(onMethod_ = @JsonGetter)
    private final @NonNull String name;

    @JsonCreator
    public ChangeNameMessage(@JsonProperty("name") @NonNull String name) {
        this.name = name;
    }
}
