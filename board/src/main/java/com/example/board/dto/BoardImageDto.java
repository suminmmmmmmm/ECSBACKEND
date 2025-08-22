//package com.example.board.dto;
//
//import com.example.board.entity.BoardImage;
//import lombok.*;
//
//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor @Builder
//public class BoardImageDto {
//    private Long id;
//    private String imageUrl;
//
//    public static BoardImageDto from(BoardImage image) {
//        return BoardImageDto.builder()
//                .id(image.getId())
//                .imageUrl(image.getImageUrl())
//                .build();
//    }
//}
package com.example.board.dto;

import com.example.board.entity.BoardImage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardImageDto {
    private Long id;
    private String imageUrl;

    public static BoardImageDto from(BoardImage image) {
        return BoardImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}
