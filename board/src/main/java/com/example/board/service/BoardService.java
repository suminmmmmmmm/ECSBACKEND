package com.example.board.service;

import com.example.board.dto.BoardRequestDto;
import com.example.board.entity.Board;
import com.example.board.repository.BoardLikeRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final ScrapRepository scrapRepository;

    /** 전체 조회 */
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    /** 단건 조회 (Long 사용) */
    public Board findById(Long id) {
        return boardRepository.findById(id).orElseThrow();
    }

    /** 저장 */
    public Board save(Board board) {
        return boardRepository.save(board);
    }

    /** 삭제 (연관 삭제 포함) */
    @Transactional
    public void delete(Long id) {
        Board board = boardRepository.findById(id).orElseThrow();

        // 연관 데이터 먼저 정리
        scrapRepository.deleteAllByBoard(board);
        boardLikeRepository.deleteAllByBoard(board);

        boardRepository.deleteById(id);
    }

    /** 글 수정 (Long 사용) */
    @Transactional
    public Board updateBoard(Long id, BoardRequestDto dto) {
        Board board = boardRepository.findById(id).orElseThrow();
        board.update(dto.getTitle(), dto.getContent());
        // 영속 상태라 save 없어도 flush 시 반영되지만, 명시적으로 save 유지
        return boardRepository.save(board);
    }

    /** 제목 키워드 검색 */
    public List<Board> searchByTitle(String keyword) {
        return boardRepository.searchByTitle(keyword.trim());
    }

    /** 페이지 조회 */
    public Page<Board> findAllPaged(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }



    // 본인 글 전체
    public List<Board> findAllByWriter(String writer) {
        return boardRepository.findByWriterOrderByCreatedDateDesc(writer);
    }
}
