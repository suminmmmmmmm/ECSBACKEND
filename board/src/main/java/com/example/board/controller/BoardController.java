package com.example.board.controller;

import com.example.board.entity.Board;
import com.example.board.entity.BoardImage;
import com.example.board.entity.BoardLike;
import com.example.board.service.BoardLikeService;
import com.example.board.service.BoardService;
import com.example.board.config.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final S3UploadService s3UploadService;
    private final BoardLikeService boardLikeService;

    //모든 게시글 조회
    @GetMapping
    public ResponseEntity<List<Board>> list() {
        return ResponseEntity.ok(boardService.findAll());
    }

    //특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> detail(@PathVariable Long id) {
        Board board = boardService.findById(id);
        return board != null ? ResponseEntity.ok(board) : ResponseEntity.notFound().build();
    }

    //게시글 작성
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Board> create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String author,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles) throws IOException {

        Board board = Board.builder()
                .title(title)
                .content(content)
                .writer(author)
                .build();

        List<BoardImage> images = new ArrayList<>();
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3UploadService.saveFile(file);
                images.add(BoardImage.builder()
                        .imageUrl(imageUrl)
                        .board(board)
                        .build());
            }
        }
        board.setImages(images);

        return ResponseEntity.ok(boardService.save(board));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Board> update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) List<Long> keepImageIds,
            @RequestParam(value = "images", required = false) List<MultipartFile> newImages) throws IOException {

        Board board = boardService.findById(id);
        board.setTitle(title);
        board.setContent(content);

        // 기존 이미지 중 유지할 이미지 ID를 제외하고 삭제
        if (board.getImages() != null) {
            Iterator<BoardImage> iterator = board.getImages().iterator();
            while (iterator.hasNext()) {
                BoardImage image = iterator.next();
                if (keepImageIds == null || !keepImageIds.contains(image.getId())) {
                    s3UploadService.deleteFile(image.getImageUrl());
                    iterator.remove();
                }
            }
        }

        // 새 이미지 업로드
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                String imageUrl = s3UploadService.saveFile(file);
                BoardImage image = BoardImage.builder()
                        .imageUrl(imageUrl)
                        .board(board)
                        .build();
                board.getImages().add(image);
            }
        }

        return ResponseEntity.ok(boardService.save(board));
    }

    //게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }

    //게시물 좋아요
    @PostMapping("/{boardId}/like")
    public ResponseEntity<Map<String, Object>> togglelike(@PathVariable Long boardId, @AuthenticationPrincipal UserDetails userDetails) {
        boolean liked = boardLikeService.toggleLike(boardId, userDetails.getUsername());
        int count = boardLikeService.getLikeCount(boardId);
        return ResponseEntity.ok(Map.of("liked", liked, "likecount", count));
    }
}
