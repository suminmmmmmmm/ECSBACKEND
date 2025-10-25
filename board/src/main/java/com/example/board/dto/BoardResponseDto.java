package com.example.board.dto;

import com.example.board.entity.Board;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isliked")
    private boolean liked;
    @JsonProperty("isScrapped")
    private boolean scrapped;
    private int commentCount;
    private int scrapCount;


    public static BoardResponseDto from(Board board, int likeCount) {
        return from(board, false, false, likeCount, 0, 0);
    }


    public static BoardResponseDto from(Board board, boolean liked, int likeCount) {
        return from(board, liked, false, likeCount, 0, 0);
    }


    public static BoardResponseDto from(Board board, int likeCount, int commentCount, int scrapCount) {
        return from(board, false, false, likeCount, commentCount, scrapCount);
    }


    public static BoardResponseDto from(Board board,
                                        boolean liked,
                                        boolean scrapped,
                                        int likeCount,
                                        int commentCount,
                                        int scrapCount) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<BoardImageDto> imageDtos =
                (board.getImages() == null) ? List.of()
                        : board.getImages().stream()
                        .map(BoardImageDto::from)
                        .collect(Collectors.toList());

        return BoardResponseDto.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getWriter())
                .createdDate(board.getCreatedDate() != null
                        ? board.getCreatedDate().format(formatter)
                        : null)
                .modifiedDate(board.getModifiedDate() != null
                        ? board.getModifiedDate().format(formatter)
                        : null)
                .images(imageDtos)
                .likeCount(likeCount)
                .liked(liked)
                .scrapped(scrapped)
                .commentCount(commentCount)
                .scrapCount(scrapCount)
                .build();
    }
}
