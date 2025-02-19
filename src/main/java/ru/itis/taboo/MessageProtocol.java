package ru.itis.taboo;

public enum MessageProtocol {
    CHAT,
    NAME,
    START;

    public static MessageProtocol fromString(String str) {
        switch (str) {
            case "NAME":
                return NAME;
            case "CHAT":
                return CHAT;
            case "START":
                return START;
            default:
                throw new IllegalArgumentException("Unknown message: " + str);
        }
    }
}
