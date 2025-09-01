package com.example.board.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.board.dto.ScrapResponseDto;
import com.example.board.entity.Scrap;
import com.example.board.service.BoardLikeService;
import com.example.board.service.ScrapService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scraps")
public class ScrapController {

    private final ScrapService scrapService;
    private final BoardLikeService boardLikeService;

    @Getter
    public static class ScrapRequest {
        private Long boardId;   // userId는 바디로 받지 않음(토큰에서 가져옴)
    }

    /** 스크랩 추가 */
    @PostMapping
    public ResponseEntity<String> addScrap(
            @RequestBody ScrapRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();              // principal = userId
        scrapService.addScrap(userId, req.getBoardId());        // (boardId, userId) 순서 주의
        return ResponseEntity.status(201).body("스크랩 성공");
    }

    /** 내 스크랩 목록 */
    @GetMapping
    public ResponseEntity<List<ScrapResponseDto>> getMyScraps(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();

        List<Scrap> scraps = scrapService.getUserScraps(userId);   // 서비스에서 userId로 조회

        List<ScrapResponseDto> result = scraps.stream().map(scrap ->
                ScrapResponseDto.builder()
                        .scrapId(scrap.getId())
                        .boardId(scrap.getBoard().getPostId())
                        .likeCount(boardLikeService.getLikeCount(scrap.getBoard().getPostId()))
                        .boardTitle(scrap.getBoard().getTitle())
                        .boardWriter(scrap.getBoard().getWriter())
                        .boardContent(scrap.getBoard().getContent())
                        .createdDate(scrap.getBoard().getCreatedDate())
                        .modifiedDate(scrap.getBoard().getModifiedDate())
                        .build()
        ).toList();

        return ResponseEntity.ok(result);
    }

    /** 스크랩 삭제 */
    @DeleteMapping("/{scrapId}")
    public ResponseEntity<Void> deleteScrap(@PathVariable Long scrapId) {
        scrapService.deleteScrap(scrapId);
        return ResponseEntity.noContent().build();
    }
}
