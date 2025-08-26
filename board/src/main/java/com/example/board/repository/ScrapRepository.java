package com.example.board.repository;

import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // 기존 메서드 (다른 코드에서 쓰고 있으면 유지)
    Optional<Scrap> findByUserAndBoard(User user, Board board);
    List<Scrap> findAllByUser(User user);

    // 게시글 삭제 전 연관 스크랩 일괄 삭제
    void deleteAllByBoard(Board board);

    // 존재 여부 체크 (이미 있음)
    boolean existsByBoardAndUser(Board board, User user);

    // ✅ 서비스에서 사용하는 시그니처 추가
    Optional<Scrap> findByBoardAndUser(Board board, User user);
    
    // (선택) 바로 삭제할 때 편한 메서드
    void deleteByBoardAndUser(Board board, User user);
}
