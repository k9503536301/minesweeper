package ru.example.minesweeper.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.example.minesweeper.dto.GameInfoResponse;
import ru.example.minesweeper.dto.GameTurnRequest;
import ru.example.minesweeper.dto.NewGameRequest;
import ru.example.minesweeper.exceptions.MinefieldException;
import ru.example.minesweeper.managers.FieldManager;
import ru.example.minesweeper.model.FieldCell;
import ru.example.minesweeper.model.FieldCellValueEnum;
import ru.example.minesweeper.model.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
public class GameService {

    @Value("${minefield.fieldlength}")
    private int FIELD_LENGTH;

    private Map<UUID, Game> games = new HashMap<>();

    public GameInfoResponse createGame(NewGameRequest request) {
        this.validateNewGameRequest(request);

        int height = request.getHeight();
        int width = request.getWidth();
        int minesCount = request.getMinesCount();

        FieldManager fieldManager = new FieldManager(height, width);
        fieldManager.placeMines(minesCount);

        Game newGame = Game.builder()
                .gameId(UUID.randomUUID())
                .height(height)
                .width(width)
                .minesCount(minesCount)
                .completed(false)
                .field(fieldManager.getField())
                .build();

        games.put(newGame.getGameId(), newGame);

        return toGameInfo(newGame);
    }

    public GameInfoResponse gameTurn(GameTurnRequest turnRequest) {
        this.validateGameTurnRequest(turnRequest);

        Game game = games.get(turnRequest.getGameId());

        int row = turnRequest.getRow();
        int col = turnRequest.getCol();

        FieldCell revealingCell = game.getField()[row][col];

        FieldManager fieldManager = new FieldManager(game.getField());

        if (revealingCell.isMine()) {
            game.setCompleted(true);
            fieldManager.revealAllMines(FieldCellValueEnum.OPENED_MINE);
            return toGameInfo(game);
        }

        revealingCell.setRevealed(true);
        revealingCell.setDisplayingSymbol(
                FieldCellValueEnum.fromSymbol(
                        String.valueOf(revealingCell.getNearbyMine())
                )
        );

        if (revealingCell.getNearbyMine() == 0) {
            fieldManager.cascadeOpenCells(row, col);
        }

        if (fieldManager.isGameComplete()) {
            game.setCompleted(true);
            fieldManager.revealAllMines(FieldCellValueEnum.MINE);
        }

        return toGameInfo(game);
    }

    private GameInfoResponse toGameInfo(Game game) {
        return GameInfoResponse.builder()
                .gameId(game.getGameId())
                .height(game.getHeight())
                .width(game.getWidth())
                .minesCount(game.getMinesCount())
                .completed(game.isCompleted())
                .field(getFieldForResponse(game.getField()))
                .build();
    }

    private String[][] getFieldForResponse(FieldCell[][] mineField) {
        int height = mineField.length;
        int width = mineField[0].length;

        String[][] cells = new String[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = mineField[i][j].getDisplayingSymbol().getSymbol();
            }
        }

        return cells;
    }

    private void validateNewGameRequest(NewGameRequest newGameRequest) {
        if (newGameRequest.getHeight() > FIELD_LENGTH || newGameRequest.getWidth() > FIELD_LENGTH) {
            throw new MinefieldException("Incorrect field size");
        }

        if (newGameRequest.getMinesCount() >= newGameRequest.getHeight() * newGameRequest.getWidth()) {
            throw new MinefieldException("Mine count should be less then cell counts");
        }
    }

    private void validateGameTurnRequest(GameTurnRequest gameTurnRequest) {
        if (!games.containsKey(gameTurnRequest.getGameId())) {
            throw new MinefieldException("There is no Game with id: " + gameTurnRequest.getGameId());
        }

        Game game = games.get(gameTurnRequest.getGameId());

        if (game.isCompleted()) {
            throw new MinefieldException("Game already completed");
        }

        int row = gameTurnRequest.getRow();
        int col = gameTurnRequest.getCol();

        if (row < 0 || row >= game.getHeight() || col < 0 || col >= game.getWidth()) {
            throw new MinefieldException("Invalid move coordinates");
        }

        if (game.getField()[row][col].isRevealed()) {
            throw new MinefieldException("Cell already revealed");
        }
    }
}
