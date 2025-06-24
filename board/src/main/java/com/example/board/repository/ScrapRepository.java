package com.example.board.repository;

import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByUserAndBoard(User user, Board board);
    List<Scrap> findAllByUser(User user);
}
