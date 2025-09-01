package com.example.board.dto;

import com.example.board.entity.Board;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String author;
    private String createdDate;
    private String modifiedDate;
    private List<BoardImageDto> images;
    private int likeCount;
    private int commentCount;
    private int scrapCount;

    // 하위호환: 댓글/스크랩 카운트가 필요 없는 기존 호출부 지원
    public static BoardResponseDto from(Board board, int likeCount) {
        return from(board, likeCount, 0, 0);
    }

    // 확장 버전: 좋아요/댓글/스크랩 카운트를 모두 포함
    public static BoardResponseDto from(Board board, int likeCount, int commentCount, int scrapCount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<BoardImageDto> imageDtos =
                (board.getImages() == null) ? List.of()
                        : board.getImages().stream()
                        .map(BoardImageDto::from)
                        .collect(Collectors.toList());

        String created = (board.getCreatedDate() != null) ? board.getCreatedDate().format(formatter) : null;
        String modified = (board.getModifiedDate() != null) ? board.getModifiedDate().format(formatter) : null;

        return BoardResponseDto.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getWriter())
                .createdDate(created)
                .modifiedDate(modified)
                .images(imageDtos)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .scrapCount(scrapCount)
                .build();
    }
}
