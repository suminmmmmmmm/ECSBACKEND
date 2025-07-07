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

import java.util.List;

@Service

public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final ScrapRepository scrapRepository;

    public BoardService(BoardRepository boardRepository, BoardLikeRepository boardLikeRepository, ScrapRepository scrapRepository) {
        this.boardRepository = boardRepository;
        this.boardLikeRepository = boardLikeRepository;
        this.scrapRepository = scrapRepository;
    }

    public List<Board> findAll(){
        return boardRepository.findAll();
    }

    public Board findById(Long id){
        return boardRepository.findById(String.valueOf(id)).orElseThrow();
    }

    public Board save(Board board){
        return boardRepository.save(board);
    }

    @Transactional
    public void delete(Long id) {
        Board board = boardRepository.findById(String.valueOf(id)).orElseThrow();

        scrapRepository.deleteAllByBoard(board); //  scrap 먼저 삭제
        boardLikeRepository.deleteAllByBoard(board); //  좋아요도 삭제

        boardRepository.deleteById(String.valueOf(id)); //  게시글 삭제
    }



    //글 수정
    public Board updateBoard(String id, BoardRequestDto dto) {
        Board board = boardRepository.findById(id).orElseThrow();
        board.update(dto.getTitle(), dto.getContent());
        return boardRepository.save(board);
    }


    //제목 키워드 검색
    public List<Board> searchByTitle(String keyword) {
        return boardRepository.searchByTitleOrContent(keyword.trim());
    }

    public Page<Board> findAllPaged(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }
}
