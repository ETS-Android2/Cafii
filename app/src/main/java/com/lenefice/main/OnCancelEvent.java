package com.lenefice.main;

public class OnCancelEvent {
    private final boolean value;

    public OnCancelEvent(Boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
