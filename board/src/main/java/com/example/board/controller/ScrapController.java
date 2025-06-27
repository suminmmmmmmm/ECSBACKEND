package com.example.board.controller;

import com.example.board.dto.ScrapResponseDto;
import com.example.board.entity.Scrap;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import com.example.board.service.BoardLikeService;
import com.example.board.service.BoardService;
import com.example.board.service.ScrapService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;
    private final UserRepository userRepository;
    private final BoardLikeService boardLikeService;

    @Getter
    public static class ScrapRequest {
        private Long userId;
        private Long boardId;
    }

    @PostMapping("/scraps")
    public ResponseEntity<String> addScrap(
            @RequestBody ScrapRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();  // 로그인한 사용자로부터 유저네임 추출
        scrapService.addScrap(username, req.getBoardId());
        return ResponseEntity.status(201).body("스크랩 성공");
    }

    @GetMapping("/scraps")
    public ResponseEntity<List<ScrapResponseDto>> getMyScraps(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElseThrow();

        List<Scrap> scraps = scrapService.getUserScraps(user.getId());

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




    @DeleteMapping("scraps/{scrapId}")
    public ResponseEntity<String> deleteScrap(@PathVariable Long scrapId) {
        scrapService.deleteScrap(scrapId);
        return ResponseEntity.noContent().build();
    }

}
