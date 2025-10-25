package com.example.board.controller;

import com.example.board.dto.BoardResponseDto;
import com.example.board.entity.Board;
import com.example.board.entity.Scrap;
import com.example.board.service.BoardLikeService;
import com.example.board.service.ScrapService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scraps")
public class ScrapController {

    private final ScrapService scrapService;
    private final BoardLikeService boardLikeService;

    @Getter
    public static class ScrapRequest {
        private Long boardId;
    }

    /**  스크랩 추가 */
    @PostMapping
    public ResponseEntity<String> addScrap(
            @RequestBody ScrapRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        scrapService.addScrap(userId, req.getBoardId());
        return ResponseEntity.status(201).body("스크랩 성공");
    }

    /**  내 스크랩 목록 (isLiked, isScrapped 포함) */
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getMyScraps(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        List<Scrap> scraps = scrapService.getUserScraps(userId);

        List<BoardResponseDto> result = scraps.stream()
                .map(scrap -> {
                    Board board = scrap.getBoard();
                    int likeCount = boardLikeService.getLikeCount(board.getPostId());
                    boolean isLiked = boardLikeService.isLiked(board.getPostId(), userId);
                    boolean isScrapped = scrapService.isScrapped(board.getPostId(), userId);
                    int commentCount = board.getComments().size();
                    int scrapCount = board.getScraps().size();

                    return BoardResponseDto.from(board, isLiked, isScrapped, likeCount, commentCount, scrapCount);
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    /**  스크랩 삭제 */
    @DeleteMapping("/{scrapId}")
    public ResponseEntity<Void> deleteScrap(@PathVariable Long scrapId) {
        scrapService.deleteScrap(scrapId);
        return ResponseEntity.noContent().build();
    }
}
