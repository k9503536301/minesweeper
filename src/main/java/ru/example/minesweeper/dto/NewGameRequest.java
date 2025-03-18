package ru.example.minesweeper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewGameRequest {

    private int width;
    private int height;
    @JsonProperty("mines_count")
    private int minesCount;
}
