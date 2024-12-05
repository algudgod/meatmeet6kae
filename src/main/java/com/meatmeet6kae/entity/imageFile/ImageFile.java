package com.meatmeet6kae.entity.imageFile;


import com.meatmeet6kae.entity.board.Board;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_files") // 데이터베이스 테이블 이름 지정
public class ImageFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK & 자동 증가
    private int imageNo;
    @ManyToOne
    @JoinColumn(name="board_no", nullable = false) //FK 설정
    private Board board;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "create_date", updatable = false, insertable = false)
    private LocalDateTime createDate;


    public int getImageNo() {
        return imageNo;
    }

    public void setImageNo(int imageNo) {
        this.imageNo = imageNo;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
}
