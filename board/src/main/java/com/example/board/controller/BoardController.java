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
import com.example.board.service.ScrapService;
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
    private final ScrapService scrapService;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;

    /**  공통 변환 헬퍼: 좋아요/스크랩 여부 + 카운트 포함 */
    private BoardResponseDto toDtoWithCounts(Board board, UserDetails userDetails) {
        Long boardId = board.getPostId();
        int likeCount = boardLikeService.getLikeCount(boardId);
        int commentCount = (int) commentRepository.countByBoardId(boardId);
        int scrapCount = (int) scrapRepository.countByBoard_PostId(boardId);

        boolean isLiked = false;
        boolean isScrapped = false;

        if (userDetails != null) {
            String userId = userDetails.getUsername();
            isLiked = boardLikeService.isLiked(boardId, userId);
            isScrapped = scrapService.isScrapped(boardId, userId);
        }

        return BoardResponseDto.from(board, isLiked, isScrapped, likeCount, commentCount, scrapCount);
    }

    /**  전체 게시글 조회 */
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> list(@AuthenticationPrincipal UserDetails userDetails) {
        List<BoardResponseDto> dtos = boardService.findAll().stream()
                .map(board -> toDtoWithCounts(board, userDetails))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**  특정 게시글 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Board board = boardService.findById(id);
        if (board == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(toDtoWithCounts(board, userDetails));
    }

    /**  게시글 작성 */
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

        // 이미지 업로드
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

    /**  게시글 수정 */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Board> update(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) List<Long> keepImageIds,
            @RequestParam(value = "images", required = false) List<MultipartFile> newImages
    ) throws IOException {

        Board board = boardService.findById(id);

        if (title != null && !title.isBlank()) board.setTitle(title);
        if (content != null && !content.isBlank()) board.setContent(content);

        // 기존 이미지 유지
        List<Long> safeKeepIds = (keepImageIds == null) ? new ArrayList<>() : keepImageIds;

        if (board.getImages() != null) {
            Iterator<BoardImage> iterator = board.getImages().iterator();
            while (iterator.hasNext()) {
                BoardImage image = iterator.next();
                if (!safeKeepIds.contains(image.getId())) {
                    s3UploadService.deleteFile(image.getImageUrl());
                    iterator.remove();
                }
            }
        }

        // 새 이미지 추가
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                String imageUrl = s3UploadService.saveFile(file);
                board.getImages().add(BoardImage.builder()
                        .imageUrl(imageUrl)
                        .board(board)
                        .build());
            }
        }

        return ResponseEntity.ok(boardService.save(board));
    }

    /**  게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }

    /**  좋아요 토글 */
    @PostMapping("/{boardId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        boolean liked = boardLikeService.toggleLike(boardId, userDetails.getUsername());
        int count = boardLikeService.getLikeCount(boardId);

        return ResponseEntity.ok(Map.of("isLiked", liked, "likeCount", count));
    }

    /**  키워드 검색 */
    @GetMapping("/search")
    public ResponseEntity<List<BoardResponseDto>> search(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserDetails userDetails) {

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty()) return ResponseEntity.badRequest().build();

        List<Board> boards = boardService.searchByTitle(trimmedKeyword);
        List<BoardResponseDto> responseDtos = boards.stream()
                .map(board -> toDtoWithCounts(board, userDetails))
                .toList();

        return ResponseEntity.ok(responseDtos);
    }

    /**  페이지네이션 */
    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getBoardsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Board> boardPage = boardService.findAllPaged(pageable);

        List<BoardResponseDto> dtos = boardPage.getContent().stream()
                .map(board -> toDtoWithCounts(board, userDetails))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("boards", dtos);
        response.put("currentPage", boardPage.getNumber());
        response.put("totalItems", boardPage.getTotalElements());
        response.put("totalPages", boardPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**  내가 작성한 게시글 */
    @GetMapping("/myboard")
    public ResponseEntity<List<BoardResponseDto>> myBoards(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        String username = userDetails.getUsername();
        String writer = userRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"))
                .getNickname();

        List<BoardResponseDto> dtos = boardService.findAllByWriter(writer).stream()
                .map(board -> toDtoWithCounts(board, userDetails))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**  내가 댓글 단 게시글 */
    @GetMapping("/commented")
    public ResponseEntity<List<BoardResponseDto>> myCommentedBoards(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();

        String userId = userDetails.getUsername();
        var boards = commentRepository.findDistinctBoardsCommentedBy(userId);

        var dtos = boards.stream()
                .map(board -> toDtoWithCounts(board, userDetails))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**  내가 좋아요한 게시글 */
    @GetMapping("/my/likeboard")
    public ResponseEntity<List<BoardResponseDto>> getMyLikedBoards(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).build();
        String userId = userDetails.getUsername();

        List<Board> boards = boardLikeService.getUserLikedBoards(userId);

        List<BoardResponseDto> result = boards.stream()
                .map(board -> toDtoWithCounts(board, userDetails))
                .toList();

        return ResponseEntity.ok(result);
    }
}
