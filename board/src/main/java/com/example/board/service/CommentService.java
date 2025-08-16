package com.example.board.service;

import com.example.board.dto.CommentDto;
import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public Comment addComment(Long boardId, CommentDto dto) {
        Board board = boardRepository.findById(String.valueOf(boardId)).orElseThrow();

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .writer(dto.getWriter())
                .createdDate(LocalDateTime.now())
                .board(board)
                .build();

        // 대댓글일 경우 parent 설정
        if (dto.getParentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentId()).orElseThrow();
            comment.setParent(parent);
        }


        return commentRepository.save(comment);
    }

    public List<CommentDto> getCommentsByBoard(Long boardId) {
        Board board = boardRepository.findById(String.valueOf(boardId)).orElseThrow();
        List<Comment> comments = commentRepository.findByBoard(board);

        // 계층 구조 정리
        List<Comment> roots = comments.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());

        return roots.stream().map(this::convertToDtoWithChildren).collect(Collectors.toList());
    }

    public void deleteComment(Long commentId,String currentUserName) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if(comment.getWriter() == null || !comment.getWriter().equals(currentUserName)) {
            throw new SecurityException("본인의 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);//자식 댓글도 cascase로 자동 삭제됨
    }

    private CommentDto convertToDtoWithChildren(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writer(comment.getWriter())
                .createdDate(comment.getCreatedDate())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(comment.getChildren().stream()
                        .map(this::convertToDtoWithChildren)
                        .collect(Collectors.toList()))
                .build();
    }



}
