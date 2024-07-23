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

    private Timestamp created_at;

    private Integer score;

    private String body;

    private Long book_id;

    public Long getBookId() { return book_id; } 
    
    public void setBookId(Long book_id) { this.book_id = book_id; }
}
