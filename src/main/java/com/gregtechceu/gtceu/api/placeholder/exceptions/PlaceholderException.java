package com.gregtechceu.gtceu.api.placeholder.exceptions;

public class PlaceholderException extends Exception {

    private String message;
    private int line = 0, symbol = 0;

    public PlaceholderException(String message) {
        super(message);
        this.message = message;
    }

    public void setLineInfo(int line, int symbol) {
        this.line = line;
        this.symbol = symbol;
    }

    @Override
    public String getMessage() {
        return "%d:%d: %s".formatted(line, symbol, message);
    }
}
