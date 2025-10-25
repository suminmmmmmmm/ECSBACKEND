package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.entity.BoardLike;
import com.example.board.entity.User;
import com.example.board.repository.BoardLikeRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardLikeService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;

    /**  좋아요 토글 (추가/취소) */
    public boolean toggleLike(Long postId, String userId) {
        Board board = boardRepository.findById(postId).orElseThrow();
        User user = userRepository.findByUserId(userId).orElseThrow();

        return boardLikeRepository.findByBoardAndUser(board, user)
                .map(like -> {
                    boardLikeRepository.delete(like);
                    return false; // 좋아요 취소
                })
                .orElseGet(() -> {
                    boardLikeRepository.save(BoardLike.builder()
                            .board(board)
                            .user(user)
                            .build());
                    return true; // 좋아요 추가
                });
    }

    /**  좋아요 개수 조회 */
    public int getLikeCount(Long postId) {
        Board board = boardRepository.findById(postId).orElseThrow();
        return boardLikeRepository.countByBoard(board);
    }

    /**  사용자가 특정 게시글에 좋아요했는지 확인 */
    public boolean isLiked(Long postId, String userId) {
        Board board = boardRepository.findById(postId).orElseThrow();
        User user = userRepository.findByUserId(userId).orElseThrow();
        return boardLikeRepository.findByBoardAndUser(board, user).isPresent();
    }

    /**  사용자가 좋아요한 게시글 목록 */
    public List<Board> getUserLikedBoards(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        List<BoardLike> likes = boardLikeRepository.findAllByUser(user);
        return likes.stream().map(BoardLike::getBoard).toList();
    }
}
