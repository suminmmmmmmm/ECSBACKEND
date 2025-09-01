package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.entity.BoardLike;
import com.example.board.entity.User;
import com.example.board.repository.BoardLikeRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardLikeService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;

    // 게시글 좋아요 토글 (좋아요 상태 변경)
    public boolean toggleLike(Long boardId, String userId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        User user = userRepository.findByUserId(userId).orElseThrow();

        return boardLikeRepository.findByBoardAndUser(board, user)
                .map(like -> {
                    boardLikeRepository.delete(like); // 이미 좋아요한 경우 -> 취소
                    return false;
                })
                .orElseGet(() -> {
                    boardLikeRepository.save(BoardLike.builder().board(board).user(user).build());
                    return true;
                });
    }

    // 특정 게시글의 좋아요 개수 조회
    public int getLikeCount(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        return boardLikeRepository.countByBoard(board);
    }

    // 사용자가 특정 게시글에 좋아요를 눌렀는지 확인 (Optional 기반)
    public boolean isLiked(Long boardId, String userId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        User user = userRepository.findByUserId(userId).orElseThrow();
        return boardLikeRepository.findByBoardAndUser(board, user).isPresent();
    }

    // 사용자가 특정 게시글에 좋아요 했는지 확인 (existsBy 활용)
    public boolean hasLiked(Long boardId, String userId) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        User user = userRepository.findByUserId(userId).orElseThrow();
        return boardLikeRepository.existsByBoard_PostIdAndUser_Id(board.getPostId(), user.getId());
    }
}
