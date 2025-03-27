package ru.example.minesweeper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.minesweeper.model.Game;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findById(UUID gameId);
}
