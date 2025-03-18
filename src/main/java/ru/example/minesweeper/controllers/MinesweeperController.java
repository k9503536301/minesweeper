package ru.example.minesweeper.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.example.minesweeper.dto.GameInfoResponse;
import ru.example.minesweeper.dto.GameTurnRequest;
import ru.example.minesweeper.dto.NewGameRequest;
import ru.example.minesweeper.services.GameService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MinesweeperController {

    private final GameService gameService;

    @PostMapping("/new")
    public GameInfoResponse createNewGame(@RequestBody NewGameRequest newGameRequest) {
        return gameService.createGame(newGameRequest);
    }

    @PostMapping("/turn")
    public GameInfoResponse createNewTurn(@RequestBody GameTurnRequest turnRequest) {
        return gameService.gameTurn(turnRequest);
    }
}
