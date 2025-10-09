package com.example.board.repository;

import com.example.board.entity.Board;
import com.example.board.entity.BoardLike;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    int countByBoard(Board board);
    Optional<BoardLike> findByBoardAndUser(Board board, User user);
    void deleteByBoardAndUser(Board board, User user);
    void deleteAllByBoard(Board board);
    List<BoardLike> findAllByUser(User user);

    boolean existsByBoard_PostIdAndUser_Id(Long postId, Long userId);

    //List<Board> findAllByUser(User user);
}
