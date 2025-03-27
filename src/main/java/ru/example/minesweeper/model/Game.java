package ru.example.minesweeper.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "games")
public class Game extends AbstractPersistable<UUID> {

    private int width;
    private int height;
    private int minesCount;
    private boolean completed;
    @Column(columnDefinition = "TEXT")
    private String field;
}
