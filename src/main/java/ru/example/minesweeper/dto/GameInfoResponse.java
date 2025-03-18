package ru.example.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoResponse {
    @JsonProperty("game_id")
    private UUID gameId;

    @JsonProperty("mines_count")
    private int minesCount;

    private int width;
    private int height;
    private boolean completed;
    private String[][] field;
}
