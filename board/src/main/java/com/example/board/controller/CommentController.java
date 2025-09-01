package com.example.board.controller;

import com.example.board.dto.CommentDto;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.UserRepository;
import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository; // 로그인 User 매핑을 위해 필요
    private final CommentRepository commentRepository;
    // 댓글 작성
    @PostMapping("/{boardId}")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long boardId,
            @RequestBody CommentDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        // username → User 엔티티 조회
        User currentUser = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("로그인 사용자 정보 없음"));

        return ResponseEntity.ok(commentService.addComment(boardId, dto, currentUser));
    }

    // 댓글 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentsByBoard(boardId));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User currentUser = userRepository.findByUserId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("로그인 사용자 정보 없음"));

        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }


}
