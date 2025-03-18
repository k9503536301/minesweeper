package ru.example.minesweeper.managers;

import lombok.Getter;
import ru.example.minesweeper.model.FieldCell;
import ru.example.minesweeper.model.FieldCellValueEnum;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

@Getter
public class FieldManager {

    private final int height;
    private final int width;
    private final FieldCell[][] field;

    public FieldManager(int height, int width) {
        this.height = height;
        this.width = width;
        this.field = new FieldCell[height][width];

        initField();
    }

    public FieldManager(FieldCell[][] field) {
        this.height = field.length;
        this.width = field[0].length;
        this.field = field;
    }

    private void initField() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j] = new FieldCell();
            }
        }
    }

    public void placeMines(int minesCount) {
        Random random = new Random();
        int mines = minesCount;

        while (mines != 0) {
            int row = random.nextInt(height);
            int col = random.nextInt(width);
            FieldCell cell = field[row][col];

            if (!cell.isMine()) {
                cell.setMine(true);

                incrementNeighbors(row, col);
                mines--;
            }
        }
    }

    private void incrementNeighbors(int row, int col) {
        for (int i = Math.max(0, row - 1); i <= Math.min(height - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(width - 1, col + 1); j++) {
                if (i != row || j != col) {
                    field[i][j].incrementNearbyMine();
                }
            }
        }
    }

    public void revealAllMines(FieldCellValueEnum symbol) {
        Stream.of(field)
                .flatMap(Stream::of)
                .filter(FieldCell::isMine)
                .forEach(fieldCell -> fieldCell.setDisplayingSymbol(symbol));
    }

    public boolean isGameComplete() {
        int openedCellsCount = 0;
        int minesCount = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (field[i][j].isRevealed() && !field[i][j].isMine()) {
                    openedCellsCount++;
                }

                if (field[i][j].isMine()) {
                    minesCount++;
                }
            }
        }

        int noMineCellsCount = width * height - minesCount;

        return openedCellsCount == noMineCellsCount;
    }

    public void cascadeOpenCells(int row, int col) {
        for (int i = Math.max(0, row - 1); i <= Math.min(height - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(width - 1, col + 1); j++) {
                if (field[i][j].isRevealed()) {
                    continue;
                }
                field[i][j].setRevealed(true);
                field[i][j].setDisplayingSymbol(
                        FieldCellValueEnum.fromSymbol(
                                String.valueOf(field[i][j].getNearbyMine())
                        )
                );

                if (field[i][j].getNearbyMine() == 0) {
                    cascadeOpenCells(i, j);
                }
            }
        }
    }
}
