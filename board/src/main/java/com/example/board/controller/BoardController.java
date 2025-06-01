package com.example.board.controller;

import com.example.board.dto.BoardRequestDto;
import com.example.board.entity.Board;
import com.example.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    //전체 글 조회
    @GetMapping
    public ResponseEntity<List<Board>> list() {
        return ResponseEntity.ok(boardService.findAll());
    }

    //특정 글 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> detail(@PathVariable String id) {
        Board board = boardService.findById(id);
        return board != null ? ResponseEntity.ok(board) : ResponseEntity.notFound().build();
    }

    //글 작성
    @PostMapping
    public ResponseEntity<Board> create(@RequestBody Board board) {
        return ResponseEntity.ok(boardService.save(board));
    }

    //글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Board> update(@PathVariable String id, @RequestBody BoardRequestDto dto) {
        return ResponseEntity.ok(boardService.updateBoard(id, dto));
    }
}


