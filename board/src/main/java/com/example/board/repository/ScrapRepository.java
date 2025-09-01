package com.example.board.repository;

import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    Optional<Scrap> findByUserAndBoard(User user, Board board);

    List<Scrap> findAllByUser(User user);

    void deleteAllByBoard(Board board);

    @Query("select count(s) from Scrap s where s.board.postId = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);
}
