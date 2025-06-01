package com.example.board.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String writer;

    private String createdDate;
    private String modifiedDate;
    private Integer likeCount;

    public void ifPresent(Object board) {
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Optional<ResponseEntity<Object>> map(Object o) {
        return Optional.empty();
    }


}
