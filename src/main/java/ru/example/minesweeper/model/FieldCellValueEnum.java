package ru.example.minesweeper.model;

import lombok.Getter;

@Getter
public enum FieldCellValueEnum {
    EMPTY(" "),
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    MINE("M"),
    OPENED_MINE("X");

    private final String symbol;

    FieldCellValueEnum(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static FieldCellValueEnum fromSymbol(String symbol) {
        for (FieldCellValueEnum value : values()) {
            if (value.getSymbol().equals(symbol)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown symbol: " + symbol);
    }
}
