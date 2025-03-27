package ru.example.minesweeper.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.example.minesweeper.repositories.GameRepository;

@Service
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameService {

    @Value("${minefield.fieldlength}")
    private int FIELD_LENGTH;

    @Autowired
    private GameRepository gameRepository;

    public GameInfoResponse createGame(NewGameRequest request) {
        this.validateNewGameRequest(request);

        int height = request.getHeight();
        int width = request.getWidth();
        int minesCount = request.getMinesCount();

        FieldManager fieldManager = new FieldManager(height, width);
        fieldManager.placeMines(minesCount);

        Game newGame = Game.builder()
                .height(height)
                .width(width)
                .minesCount(minesCount)
                .completed(false)
                .field(fieldManager.fieldToString())
                .build();

        Game savedGame = gameRepository.save(newGame);

        return toGameInfo(savedGame, fieldManager.getField());
    }

    public GameInfoResponse gameTurn(GameTurnRequest turnRequest) {
        Game game = gameRepository.findById(turnRequest.getGameId())
                .orElseThrow(()-> new MinefieldException("There is no Game with id: " + turnRequest.getGameId()));

        this.validateGameTurnRequest(turnRequest, game);

        int row = turnRequest.getRow();
        int col = turnRequest.getCol();

        FieldManager fieldManager = new FieldManager(game.getField());

        FieldCell revealingCell = fieldManager.getCellToReveal(row, col);

        if (revealingCell.isMine()) {
            game.setCompleted(true);
            fieldManager.revealAllMines(FieldCellValueEnum.OPENED_MINE);
            return toGameInfo(game, fieldManager.getField());
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

        game.setField(fieldManager.fieldToString());
        gameRepository.save(game);

        return toGameInfo(game, fieldManager.getField());
    }

    private GameInfoResponse toGameInfo(Game game, FieldCell[][] field) {
        return GameInfoResponse.builder()
                .gameId(game.getId())
                .height(game.getHeight())
                .width(game.getWidth())
                .minesCount(game.getMinesCount())
                .completed(game.isCompleted())
                .field(getFieldForResponse(field))
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

    private void validateGameTurnRequest(GameTurnRequest gameTurnRequest, Game game) {
        if (game.isCompleted()) {
            throw new MinefieldException("Game already completed");
        }

        int row = gameTurnRequest.getRow();
        int col = gameTurnRequest.getCol();

        if (row < 0 || row >= game.getHeight() || col < 0 || col >= game.getWidth()) {
            throw new MinefieldException("Invalid move coordinates");
        }
    }
}
