package com.example.board.service;

import com.example.board.dto.CommentDto;
import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // 댓글 작성
    public Comment addComment(Long boardId, CommentDto dto, User currentUser) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .user(currentUser)
                .createdDate(LocalDateTime.now())
                .board(board)
                .build();

        return commentRepository.save(comment);
    }

    // 댓글 조회
    public List<CommentDto> getCommentsByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        List<Comment> comments = commentRepository.findByBoard(board);

        // 단순히 1-depth 댓글만 반환
        return comments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // 댓글 삭제 (본인만)
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("본인 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    private CommentDto convertToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writer(comment.getUser().getNickname()) // 작성자 닉네임 노출
                .createdDate(comment.getCreatedDate())
                .build();
    }
}
