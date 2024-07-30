package jp.co.metateam.library.model;

import java.security.Timestamp;


import lombok.Getter;
import lombok.Setter;

/**
 * 書籍レビューマスタDTO
 */
@Getter
@Setter
public class ReviewDto {

    private Long id; 

    private Timestamp createdAt;

    private Integer score;

    private String body;

    private Long bookId;

    private String title;
}