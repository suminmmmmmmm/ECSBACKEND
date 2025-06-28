package com.example.board.dto;

import com.example.board.entity.Board;
import com.example.board.entity.BoardImage;
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

    public static BoardResponseDto from(Board board, int likeCount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<BoardImageDto> imageDtos = board.getImages().stream()
                .map(BoardImageDto::from)
                .collect(Collectors.toList());

        return BoardResponseDto.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getWriter())
                .createdDate(board.getCreatedDate().format(formatter))
                .modifiedDate(board.getModifiedDate().format(formatter))
                .images(imageDtos)
                .likeCount(likeCount)
                .build();
    }
}
