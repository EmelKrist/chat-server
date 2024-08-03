package ru.emelkrist.models;

import ru.emelkrist.utils.MessageType;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private MessageType type;
    private String message;

    public ChatMessage(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
