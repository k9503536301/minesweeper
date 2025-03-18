package ru.example.minesweeper.model;

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
public class Game {

    private UUID gameId;
    private long id;
    private int width;
    private int height;
    private int minesCount;
    private boolean completed;
    private FieldCell[][] field;
}
