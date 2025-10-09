//package com.example.board.dto;
//
//import com.example.board.entity.Board;
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.Builder;
//import lombok.Getter;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Builder
//public class ScrapResponseDto {
//    private Long scrapId;
//    private Long boardId;
//    private String boardTitle;
//    private String boardWriter;
//    private String boardContent;
//    private int likeCount;
//
//
//
//    // 게시글 연관관계
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "board_id", nullable = false)
//    @JsonBackReference
//    private Board board;
//
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
//    private LocalDateTime createdDate;
//
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
//    private LocalDateTime modifiedDate;
//}
