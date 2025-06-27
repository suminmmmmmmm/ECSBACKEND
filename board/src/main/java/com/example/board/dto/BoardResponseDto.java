package com.example.board.dto;

import com.example.board.entity.Board;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoardResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String author;
    private int likeCount;

    public static BoardResponseDto from(Board board, int likeCount) {
        return BoardResponseDto.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getWriter())
                .likeCount(likeCount)
                .build();
    }


}
