package com.example.board.controller;

import com.example.board.entity.Board;
import com.example.board.service.BoardService;
import com.example.board.config.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final S3UploadService s3UploadService;

    // 전체 글 조회
    @GetMapping
    public ResponseEntity<List<Board>> list() {
        return ResponseEntity.ok(boardService.findAll());
    }

    // 특정 글 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> detail(@PathVariable String id) {
        Board board = boardService.findById(id);
        return board != null ? ResponseEntity.ok(board) : ResponseEntity.notFound().build();
    }

    // 글 작성 (이미지 포함)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Board> create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String author,
            @RequestParam Integer likeCount,
            @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = s3UploadService.saveFile(file);
        }

        Board board = Board.builder()
                .title(title)
                .content(content)
                .writer(author)
                .likeCount(likeCount)
                .imageUrl(imageUrl)
                .build();

        return ResponseEntity.ok(boardService.save(board));
    }

    // 글 수정 (이미지 수정, 유지, 삭제 모두 지원)
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Board> update(
            @PathVariable String id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Boolean deleteImage,
            @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        Board board = boardService.findById(id);

        // 이미지 삭제 요청이 명시된 경우
        if (Boolean.TRUE.equals(deleteImage) && board.getImageUrl() != null) {
            s3UploadService.deleteFile(board.getImageUrl());
            board.setImageUrl(null);
        }

        // 새 이미지가 첨부된 경우 → 기존 이미지 교체
        if (file != null && !file.isEmpty()) {
            String newImageUrl = s3UploadService.saveFile(file);
            board.setImageUrl(newImageUrl);
        }

        // 기타 필드 업데이트
        board.setTitle(title);
        board.setContent(content);

        return ResponseEntity.ok(boardService.save(board));
    }

    // 글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
