package ru.example.minesweeper.managers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ru.example.minesweeper.model.FieldCell;
import ru.example.minesweeper.model.FieldCellValueEnum;

class FieldManagerTest {

    @Test
    void testConstructorWithHeightAndWidth() {
        int height = 5;
        int width = 10;

        FieldManager manager = new FieldManager(height, width);

        assertEquals(height, manager.getHeight());
        assertEquals(width, manager.getWidth());
        assertEquals(height,manager.getField().length);
    }

    @Test
    void testPlaceMines() {
        int height = 5;
        int width = 10;
        int minesCount = 10;
        FieldManager manager = new FieldManager(height, width);

        manager.placeMines(minesCount);

        assertEquals(minesCount, countMines(manager.getField()));

        checkNeighborCounts(manager.getField());
    }

    private int countMines(FieldCell[][] field) {
        int count = 0;
        for (FieldCell[] row : field) {
            for (FieldCell cell : row) {
                if (cell.isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void checkNeighborCounts(FieldCell[][] field) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                FieldCell cell = field[i][j];
                if (cell.isMine()) {
                    continue;
                }

                int expectedNearbyMines = countNearbyMines(field, i, j);
                assertEquals(cell.getNearbyMine(), expectedNearbyMines);
            }
        }
    }

    private int countNearbyMines(FieldCell[][] field, int row, int col) {
        int count = 0;
        for (int i = Math.max(0, row - 1); i <= Math.min(field.length - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(field[i].length - 1, col + 1); j++) {
                if ((i != row || j != col) && field[i][j].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void testRevealAllMines() {
        int height = 5;
        int width = 10;
        int minesCount = 10;
        FieldManager manager = new FieldManager(height, width);
        manager.placeMines(minesCount);

        manager.revealAllMines(FieldCellValueEnum.MINE);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                FieldCell cell = manager.getField()[i][j];
                if (cell.isMine()) {
                    assertEquals(FieldCellValueEnum.MINE, cell.getDisplayingSymbol());
                } else {
                    assertNotEquals(FieldCellValueEnum.MINE, cell.getDisplayingSymbol());
                }
            }
        }
    }

    @Test
    void testIsGameComplete() {
        int height = 5;
        int width = 10;
        int minesCount = 10;
        FieldManager manager = new FieldManager(height, width);
        manager.placeMines(minesCount);

        openAllNonMineCells(manager);

        assertTrue(manager.isGameComplete());
    }

    private void openAllNonMineCells(FieldManager manager) {
        FieldCell[][] field = manager.getField();
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (!field[i][j].isMine()) {
                    field[i][j].setRevealed(true);
                }
            }
        }
    }

    @Test
    void testCascadeOpenCells() {
        int height = 5;
        int width = 10;
        FieldManager manager = new FieldManager(height, width);
        manager.placeMines(0); // No mines for simplicity

        manager.cascadeOpenCells(2, 2);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                assertTrue(manager.getField()[i][j].isRevealed());
            }
        }
    }
}
