package ru.example.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class GameTurnRequest {

    @JsonProperty("game_id")
    private UUID gameId;
    private int col;
    private int row;
}
