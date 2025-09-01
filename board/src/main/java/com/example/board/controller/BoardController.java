package com.example.board.controller;

import com.example.board.dto.BoardResponseDto;
import com.example.board.entity.Board;
import com.example.board.entity.BoardImage;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.ScrapRepository;
import com.example.board.repository.UserRepository;
import com.example.board.service.BoardLikeService;
import com.example.board.service.BoardService;
import com.example.board.config.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final S3UploadService s3UploadService;
    private final BoardLikeService boardLikeService;
    private final BoardRepository boardRepository; // (필요 없으면 제거해도 됨)
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;

    // 공통 매핑 헬퍼: 좋아요/댓글/스크랩 수 포함 DTO 변환
    private BoardResponseDto toDtoWithCounts(Board b) {
        Long bid = b.getPostId();
        int likeCount = boardLikeService.getLikeCount(bid);
        int commentCount = (int) commentRepository.countByBoardId(bid);
        int scrapCount = (int) scrapRepository.countByBoard_PostId(bid);
        return BoardResponseDto.from(b, likeCount, commentCount, scrapCount);
    }

    // 모든 게시글 조회 (엔티티 → DTO + 카운트 포함)
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> list() {
        List<BoardResponseDto> dtos = boardService.findAll().stream()
                .map(this::toDtoWithCounts)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> detail(@PathVariable Long id) {
        Board board = boardService.findById(id);
        if (board == null) return ResponseEntity.notFound().build();

        int likeCount = boardLikeService.getLikeCount(id);
        int commentCount = (int) commentRepository.countByBoardId(id);
        int scrapCount = (int) scrapRepository.countByBoard_PostId(id);

        return ResponseEntity.ok(BoardResponseDto.from(board, likeCount, commentCount, scrapCount));
    }

    // 게시글 작성 (토큰 사용자 닉네임을 writer로 저장)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Board> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles
    ) throws IOException {

        if (userDetails == null) return ResponseEntity.status(401).build();

        String username = userDetails.getUsername();
        String writer = userRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"))
                .getNickname();

        Board board = Board.builder()
                .title(title)
                .content(content)
                .writer(writer)
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

    // 글 수정
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Board> update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) List<Long> keepImageIds,
            @RequestParam(value = "images", required = false) List<MultipartFile> newImages
    ) throws IOException {

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

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }

    // 게시물 좋아요 토글
    @PostMapping("/{boardId}/like")
    public ResponseEntity<Map<String, Object>> togglelike(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        boolean liked = boardLikeService.toggleLike(boardId, userDetails.getUsername());
        int count = boardLikeService.getLikeCount(boardId);
        return ResponseEntity.ok(Map.of("liked", liked, "likecount", count));
    }

    // 키워드 검색
    @GetMapping("/search")
    public ResponseEntity<List<BoardResponseDto>> search(@RequestParam String keyword) {
        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Board> boards = boardService.searchByTitle(trimmedKeyword);
        List<BoardResponseDto> responseDtos = boards.stream()
                .map(this::toDtoWithCounts)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    // 게시물 페이지네이션
    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getBoardsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Board> boardPage = boardService.findAllPaged(pageable);

        List<BoardResponseDto> dtos = boardPage.getContent().stream()
                .map(this::toDtoWithCounts)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("boards", dtos);
        response.put("currentPage", boardPage.getNumber());
        response.put("totalItems", boardPage.getTotalElements());
        response.put("totalPages", boardPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // 본인이 작성한 글 목록
    @GetMapping("/myboard")
    public ResponseEntity<List<BoardResponseDto>> myBoards(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        String username = userDetails.getUsername();
        String writer = userRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"))
                .getNickname();

        List<BoardResponseDto> dtos = boardService.findAllByWriter(writer).stream()
                .map(this::toDtoWithCounts)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 내가 댓글 단 게시글 목록 (중복 제거)
    // BoardController.java
    @GetMapping("/commented")
    public ResponseEntity<List<BoardResponseDto>> myCommentedBoards(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        String userId = userDetails.getUsername();        // ✅ 핵심: userId 문자열
        var boards = commentRepository.findDistinctBoardsCommentedBy(userId);

        var dtos = boards.stream()
                .map(this::toDtoWithCounts)              // like/댓글/스크랩 카운트 포함
                .toList();

        return ResponseEntity.ok(dtos);
    }

}
