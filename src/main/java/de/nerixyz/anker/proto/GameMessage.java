package de.nerixyz.anker.proto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatMessage.class, name = "chat"),
        @JsonSubTypes.Type(value = ChangeNameMessage.class, name = "change-name"),
})
public sealed abstract class GameMessage permits ChangeNameMessage, ChatMessage {
}
