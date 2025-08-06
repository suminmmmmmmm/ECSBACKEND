package com.example.board.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private String writer;
    private LocalDateTime createdDate;
    private Long parentId;
    private List<CommentDto> children = new ArrayList<>();
    public Long getParentId() {
        return parentId;
    }
}

