package com.itvers.toolbox.item;

public enum Key {
    NONE(0), WEB(1), APP(2), PHONE(3);

    private final int value;

    Key(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}