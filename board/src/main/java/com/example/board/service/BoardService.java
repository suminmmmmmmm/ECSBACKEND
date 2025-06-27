package com.example.board.service;

import com.example.board.dto.BoardRequestDto;
import com.example.board.entity.Board;
import com.example.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
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

    public void delete(Long id){
        boardRepository.deleteById(String.valueOf(id));
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

}
