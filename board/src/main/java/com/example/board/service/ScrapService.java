package com.example.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.ScrapRepository;
import com.example.board.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /** 스크랩 추가 */
    @Transactional
    public void addScrap(String userId, Long boardId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        Board board = boardRepository.findById(boardId).orElseThrow();

        if (!scrapRepository.existsByBoardAndUser(board, user)) {
            scrapRepository.save(Scrap.builder().board(board).user(user).build());
        }
    }

    /** 스크랩 삭제 (게시글/유저 조합으로) */
    @Transactional
    public void removeScrap(Long boardId, String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        Board board = boardRepository.findById(boardId).orElseThrow();

        scrapRepository.findByBoardAndUser(board, user)
                .ifPresent(scrapRepository::delete);
        // 또는: scrapRepository.deleteByBoardAndUser(board, user);
    }

    /** 내 스크랩 여부 확인 */
    @Transactional(readOnly = true)
    public boolean isScrapped(Long boardId, String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        Board board = boardRepository.findById(boardId).orElseThrow();
        return scrapRepository.existsByBoardAndUser(board, user);
    }

    /** 내 스크랩 목록 */
    @Transactional(readOnly = true)
    public List<Scrap> getUserScraps(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        return scrapRepository.findAllByUser(user);
    }

    /** 스크랩 단건 삭제 (scrapId로) */
    @Transactional
    public void deleteScrap(Long scrapId) {
        scrapRepository.deleteById(scrapId);
    }
}
