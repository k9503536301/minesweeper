package ru.example.minesweeper.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import ru.example.minesweeper.dto.GameInfoResponse;
import ru.example.minesweeper.dto.GameTurnRequest;
import ru.example.minesweeper.dto.NewGameRequest;
import ru.example.minesweeper.exceptions.MinefieldException;
import ru.example.minesweeper.model.FieldCell;
import ru.example.minesweeper.model.FieldCellValueEnum;
import ru.example.minesweeper.model.Game;
import ru.example.minesweeper.repositories.GameRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(gameService, "FIELD_LENGTH", 30);
    }

    @Test
    void testCreateGame_Success() {
        UUID expectedUuid = UUID.randomUUID();
        NewGameRequest mockNewGameRequest = mock(NewGameRequest.class);
        when(mockNewGameRequest.getHeight()).thenReturn(10);
        when(mockNewGameRequest.getWidth()).thenReturn(10);
        when(mockNewGameRequest.getMinesCount()).thenReturn(20);
        given(gameRepository.save(any(Game.class)))
                .willAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    ReflectionTestUtils.setField(game, "id", expectedUuid);

                    return game;
                });

        GameInfoResponse response = gameService.createGame(mockNewGameRequest);

        verify(gameRepository).save(any(Game.class)); // Проверка вызова save
        assertEquals(expectedUuid, response.getGameId());
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

        MinefieldException minefieldException = assertThrows(
                MinefieldException.class,
                () -> gameService.createGame(mockNewGameRequest)
        );
        assertEquals("Incorrect field size", minefieldException.getMessage());
    }

    @Test
    void testCreateGame_IncorrectMineCount() {
        NewGameRequest mockNewGameRequest = mock(NewGameRequest.class);
        when(mockNewGameRequest.getHeight()).thenReturn(2);
        when(mockNewGameRequest.getWidth()).thenReturn(2);
        when(mockNewGameRequest.getMinesCount()).thenReturn(5);

        MinefieldException minefieldException = assertThrows(
                MinefieldException.class,
                () -> gameService.createGame(mockNewGameRequest)
        );
        assertEquals("Mine count should be less then cell counts", minefieldException.getMessage());
    }

    @Test
    void testGameTurn_Success() throws JsonProcessingException {
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

        String stringField = objectMapper.writeValueAsString(mockField);
        when(game.getField()).thenReturn(stringField);
        when(game.getHeight()).thenReturn(height);
        when(game.getWidth()).thenReturn(width);

        given(gameRepository.findById(gameId)).willReturn(Optional.of(game));

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);
        when(mockGameTurnRequest.getRow()).thenReturn(row);
        when(mockGameTurnRequest.getCol()).thenReturn(col);

        gameService.gameTurn(mockGameTurnRequest);

        verify(game).setField(anyString());
        verify(gameRepository).save(game);
    }

    @Test
    void testGameTurn_GameOver() throws JsonProcessingException {
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

        String stringField = objectMapper.writeValueAsString(mockField);
        when(game.getField()).thenReturn(stringField);
        when(game.getHeight()).thenReturn(height);
        when(game.getWidth()).thenReturn(width);

        given(gameRepository.findById(gameId)).willReturn(Optional.of(game));

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);
        when(mockGameTurnRequest.getRow()).thenReturn(row);
        when(mockGameTurnRequest.getCol()).thenReturn(col);

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

        Game game = mock(Game.class);
        when(game.getField()).thenReturn(".......");
        when(game.getHeight()).thenReturn(3);
        when(game.getWidth()).thenReturn(3);
        when(game.getId()).thenReturn(gameId);

        given(gameRepository.findById(gameId)).willReturn(Optional.of(game));

        MinefieldException minefieldException = assertThrows(
                MinefieldException.class,
                () -> gameService.gameTurn(mockGameTurnRequest)
        );
        assertEquals("Invalid move coordinates", minefieldException.getMessage());
    }

    @Test
    void testGameTurn_GameAlreadyCompleted() {
        UUID gameId = UUID.randomUUID();

        Game game = mock(Game.class);
        when(game.isCompleted()).thenReturn(true);
        when(game.getField()).thenReturn(".......");
        when(game.getHeight()).thenReturn(3);
        when(game.getWidth()).thenReturn(3);
        when(game.getId()).thenReturn(gameId);
        given(gameRepository.findById(gameId)).willReturn(Optional.of(game));

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);

        MinefieldException minefieldException = assertThrows(
                MinefieldException.class,
                () -> gameService.gameTurn(mockGameTurnRequest)
        );
        assertEquals("Game already completed", minefieldException.getMessage());
    }

    @Test
    void testGameTurn_GameNotFound() {
        UUID gameId = UUID.randomUUID();

        GameTurnRequest mockGameTurnRequest = mock(GameTurnRequest.class);
        when(mockGameTurnRequest.getGameId()).thenReturn(gameId);

        MinefieldException minefieldException = assertThrows(
                MinefieldException.class,
                () -> gameService.gameTurn(mockGameTurnRequest)
        );
        assertTrue(minefieldException.getMessage().startsWith("There is no Game with id"));
    }
}
