package com.example.board.repository;

import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByBoard(Board board);
    List<Comment> findByAuthorUserId(String userId);
    // 사용자가 댓글 단 게시판 목록 (최근 작성된 게시판순)
    @Query("""
           select distinct c.board
           from Comment c
           where c.authorUserId = :userId
           order by c.board.createdDate desc
           """)
    List<Board> findDistinctBoardsCommentedBy(@Param("userId") String userId);

    @Query("select count(c) from Comment c where c.board.postId = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);

  //  List<Comment> findAllByUser(User user);
}
