package com.example.board.dto;

import com.example.board.entity.Board;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardRequestDto {
    private String postId;
    private String title;
    private String content;
    private String category;
    private String author;
    private String imageUrl;
    private String deleteImage;
}

