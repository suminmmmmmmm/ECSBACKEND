package com.example.board.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String writer;

    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private ZonedDateTime modifiedDate;

    private Integer likeCount;

    // 여러 이미지 연관관계
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BoardImage> images = new ArrayList<>();

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
