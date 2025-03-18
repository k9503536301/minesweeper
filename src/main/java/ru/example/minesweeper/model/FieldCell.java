package ru.example.minesweeper.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldCell {

    private int nearbyMine = 0;
    private boolean revealed = false;
    private boolean mine = false;
    private FieldCellValueEnum displayingSymbol = FieldCellValueEnum.EMPTY;

    public void incrementNearbyMine() {
        if (!this.mine) {
            this.nearbyMine++;
        }
    }
}
