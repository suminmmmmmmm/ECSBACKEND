package com.example.board.controller;

import com.example.board.dto.BoardResponseDto;
import com.example.board.dto.CommentDto;
import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import com.example.board.repository.CommentRepository;
import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.board.dto.CommentDto.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;

    @PostMapping("/{boardId}")
    public ResponseEntity<Comment> addComment(@PathVariable Long boardId, @RequestBody CommentDto dto) {
        return ResponseEntity.ok(commentService.addComment(boardId, dto));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentsByBoard(boardId));
    }

    /** 내가 작성한 댓글 목록 */
    @GetMapping("/my/comments")
    public ResponseEntity<List<CommentDto>> getMyComments(@AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();

        // ① 로그인한 사용자의 댓글만 조회
        List<Comment> comments = commentService.getUserComments(userId);

        // ② Comment → CommentDto 변환
        List<CommentDto> result = comments.stream()
                .map(comment -> CommentDto.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .writer(comment.getWriter())            // 닉네임
                        .createdDate(comment.getCreatedDate())
                        .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                        .build())
                .toList();

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }
        commentService.deleteComment(commentId,userDetails.getUsername());
        return ResponseEntity.noContent().build();

    }

}

