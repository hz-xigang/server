package com.gz.xg.enums;

public enum BusinessType {
    OTHER(0),
    INSERT(1),
    UPDATE(2),
    DELETE(3),
    UPLOAD(4),
    EXPORT(5);

    private final int value;

    BusinessType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
