package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.ScrapRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    public void addScrap(String userId, Long boardId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        Board board = boardRepository.findById(String.valueOf(boardId)).orElseThrow();

        // 이미 스크랩했는지 확인 후 저장
        if (scrapRepository.findByUserAndBoard(user, board).isEmpty()) {
            Scrap scrap = Scrap.builder().user(user).board(board).build();
            scrapRepository.save(scrap);
        }
    }


    public List<Scrap> getUserScraps(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return scrapRepository.findAllByUser(user);
    }


    public void deleteScrap(Long scrapId) {
        Scrap scrap = scrapRepository.findById(scrapId).orElseThrow();
        scrapRepository.delete(scrap);
    }
}
