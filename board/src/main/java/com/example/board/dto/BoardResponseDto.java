package com.example.board.dto;

import com.example.board.entity.Board;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoardResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String category;
    private String author;

    public static BoardResponseDto from(Board board) {
        return BoardResponseDto.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getWriter())
                .build();
    }
}
