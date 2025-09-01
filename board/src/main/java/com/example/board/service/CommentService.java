package com.example.board.service;

import com.example.board.dto.BoardResponseDto;
import com.example.board.dto.CommentDto;
import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.ScrapRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeService boardLikeService;
    private final ScrapRepository scrapRepository;

    // 댓글 작성 (writer는 서버에서 채움: nickname, authorUserId 저장)
    @Transactional
    public Comment addComment(Long boardId, CommentDto dto) {
        Board board = boardRepository.findById(boardId).orElseThrow();

        String userId = currentUserId();                              // JWT principal = userId
        User user = userRepository.findByUserId(userId).orElseThrow();
        String nickname = user.getNickname();

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .writer(nickname)              // 표시용: 닉네임
                .authorUserId(userId)          // 권한/소유권 판단용
                .createdDate(LocalDateTime.now())
                .board(board)
                .build();

        if (dto.getParentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentId()).orElseThrow();
            comment.setParent(parent);
        }

        return commentRepository.save(comment);
    }

    // 게시글의 댓글 트리 조회 (필요 시 정렬 추가)
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        List<Comment> comments = commentRepository.findByBoard(board);

        // 루트만 추려서 트리화, 생성일 정렬(선택)
        return comments.stream()
                .filter(c -> c.getParent() == null)
                .sorted(Comparator.comparing(Comment::getCreatedDate))
                .map(this::convertToDtoWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 소유자만 삭제 가능 (필요 시 주석 해제)
        // if (!comment.getAuthorUserId().equals(currentUserId())) {
        //     throw new AccessDeniedException("내 댓글만 삭제할 수 있습니다.");
        // }

        commentRepository.delete(comment); // 자식은 cascade로 처리
    }

    private CommentDto convertToDtoWithChildren(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writer(comment.getWriter()) // 화면 표시는 닉네임
                .createdDate(comment.getCreatedDate())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(comment.getChildren().stream()
                        .sorted(Comparator.comparing(Comment::getCreatedDate))
                        .map(this::convertToDtoWithChildren)
                        .collect(Collectors.toList()))
                .build();
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // CustomUserDetailsService에서 withUsername(userId)로 설정했으므로 userId 반환
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> getBoardsICommented() {
        String userId = currentUserId();
        List<Board> boards = commentRepository.findDistinctBoardsCommentedBy(userId);

        return boards.stream()
                .map(b -> {
                    long bid = b.getPostId();
                    int likeCount    = boardLikeService.getLikeCount(bid);
                    int commentCount = Math.toIntExact(commentRepository.countByBoardId(bid));
                    int scrapCount   = (int) scrapRepository.countByBoard_PostId(bid);
                    return BoardResponseDto.from(b, likeCount, commentCount, scrapCount);
                })
                .toList();
    }

}
