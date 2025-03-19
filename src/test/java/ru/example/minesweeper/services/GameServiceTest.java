package ru.example.minesweeper.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import ru.example.minesweeper.dto.GameInfoResponse;
import ru.example.minesweeper.dto.GameTurnRequest;
import ru.example.minesweeper.dto.NewGameRequest;
import ru.example.minesweeper.exceptions.MinefieldException;
import ru.example.minesweeper.model.FieldCell;
import ru.example.minesweeper.model.FieldCellValueEnum;
import ru.example.minesweeper.model.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(gameService, "FIELD_LENGTH", 30);
    }

    @Test
    void testCreateGame_Success() {
        NewGameRequest mockNewGameRequest = mock(NewGameRequest.class);
        when(mockNewGameRequest.getHeight()).thenReturn(10);
        when(mockNewGameRequest.getWidth()).thenReturn(10);
        when(mockNewGameRequest.getMinesCount()).thenReturn(20);

        GameInfoResponse response = gameService.createGame(mockNewGameRequest);

        assertEquals(10, response.getHeight());
        assertEquals(10, response.getWidth());
        assertEquals(20, response.getMinesCount());
        assertFalse(response.isCompleted());
    }

    @Test
    void testCreateGame_IncorrectFieldSize() {
        NewGameRequest mockNewGameRequest = mock(NewGameRequest.class);
        when(mockNewGameRequest.getHeight()).thenReturn(31);
        when(mockNewGameRequest.getWidth()).thenReturn(10);

        assertThrows(MinefieldException.class, () -> gameService.createGame(mockNewGameRequest));
    }

    @Test
    void testCreateGame_IncorrectMineCount() {
        NewGameRequest mockNewGameRequest = mock(NewGameRequest.class);
        when(mockNewGameRequest.getHeight()).thenReturn(2);
        when(mockNewGameRequest.getWidth()).thenReturn(2);
        when(mockNewGameRequest.getMinesCount()).thenReturn(5);

        assertThrows(MinefieldException.class, () -> gameService.createGame(mockNewGameRequest));
    }

    @Test
    void testGameTurn_Success() {
        int height = 5;
        int width = 5;
        int row = 3;
        int col = 3;
        UUID gameId = UUID.randomUUID();

        Game game = mock(Game.class);

        FieldCell[][] mockField = new FieldCell[height][width];
        FieldCellValueEnum value = FieldCellValueEnum.EMPTY;

        for (int i = 0; i < mockField.length; i++) {
            for (int j = 0; j < mockField[i].length; j++) {
                mockField[i][j] = mock(FieldCell.class);
                when(mockField[i][j].getDisplayingSymbol()).thenReturn(value);
            }
        }

        when(game.getField()).thenReturn(mockField);
        when(game.getHeight()).thenReturn(height);
        when(game.getWidth()).thenReturn(width);

        Map<UUID, Game> gameMap = new HashMap<>();
        gameMap.put(gameId, game);
        ReflectionTestUtils.setField(gameService, "games", gameMap);

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);
        when(mockGameTurnRequest.getRow()).thenReturn(row);
        when(mockGameTurnRequest.getCol()).thenReturn(col);

        FieldCell mockCell = mockField[row][col];
        when(mockCell.isMine()).thenReturn(false);
        when(mockCell.isRevealed()).thenReturn(false);
        when(mockCell.getDisplayingSymbol()).thenReturn(FieldCellValueEnum.ONE);
        when(mockCell.getNearbyMine()).thenReturn(1);

        gameService.gameTurn(mockGameTurnRequest);

        verify(mockCell).setRevealed(true);
    }

    @Test
    void testGameTurn_GameOver() {
        int height = 5;
        int width = 5;
        int row = 3;
        int col = 3;
        UUID gameId = UUID.randomUUID();

        Game game = mock(Game.class);

        FieldCell[][] mockField = new FieldCell[height][width];
        FieldCellValueEnum value = FieldCellValueEnum.EMPTY;
        for (int i = 0; i < mockField.length; i++) {
            for (int j = 0; j < mockField[i].length; j++) {
                mockField[i][j] = mock(FieldCell.class);
                when(mockField[i][j].getDisplayingSymbol()).thenReturn(value);
            }
        }

        when(game.getField()).thenReturn(mockField);
        when(game.getHeight()).thenReturn(height);
        when(game.getWidth()).thenReturn(width);

        Map<UUID, Game> gameMap = new HashMap<>();
        gameMap.put(gameId, game);
        ReflectionTestUtils.setField(gameService, "games", gameMap);

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);
        when(mockGameTurnRequest.getRow()).thenReturn(row);
        when(mockGameTurnRequest.getCol()).thenReturn(col);

        FieldCell mockCell = mockField[row][col];
        when(mockCell.isMine()).thenReturn(true);
        when(mockCell.isRevealed()).thenReturn(false);
        when(mockCell.getNearbyMine()).thenReturn(1);

        gameService.gameTurn(mockGameTurnRequest);

        verify(game).setCompleted(true);
    }

    @Test
    void testGameTurn_InvalidCoordinates() {
        UUID gameId = UUID.randomUUID();
        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);
        when(mockGameTurnRequest.getRow()).thenReturn(-1);
        when(mockGameTurnRequest.getCol()).thenReturn(5);

        assertThrows(MinefieldException.class, () -> gameService.gameTurn(mockGameTurnRequest));
    }

    @Test
    void testGameTurn_GameAlreadyCompleted() {
        UUID gameId = UUID.randomUUID();

        Game game = mock(Game.class);
        when(game.isCompleted()).thenReturn(true);

        FieldCell[][] mockField = new FieldCell[5][5];
        for (int i = 0; i < mockField.length; i++) {
            for (int j = 0; j < mockField[i].length; j++) {
                mockField[i][j] = mock(FieldCell.class);
            }
        }

        Map<UUID, Game> gameMap = new HashMap<>();
        gameMap.put(gameId, game);
        ReflectionTestUtils.setField(gameService, "games", gameMap);

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);

        assertThrows(MinefieldException.class, () -> gameService.gameTurn(mockGameTurnRequest));
    }
}
