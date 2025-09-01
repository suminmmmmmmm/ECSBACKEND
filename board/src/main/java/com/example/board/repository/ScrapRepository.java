package com.example.board.repository;

import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // Scrap.board.postId 를 따라가서 count
    long countByBoard_PostId(Long postId);

    // 유틸로 많이 쓰는 메서드들 (있으면 편해요)
    boolean existsByBoard_PostIdAndUser_UserId(Long postId, String userId);
    Optional<Scrap> findByBoard_PostIdAndUser_UserId(Long postId, String userId);

    @Transactional
    @Modifying
    int deleteByBoard_PostIdAndUser_UserId(Long postId, String userId);

//    long countByBoard_PostId(Long postId);

    void deleteAllByBoard(Board board);

    boolean existsByBoardAndUser(Board board, User user);

    Optional<Scrap> findByBoardAndUser(Board board, User user);

    List<Scrap> findAllByUser(User user);
}
