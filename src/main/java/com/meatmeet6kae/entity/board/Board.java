package com.meatmeet6kae.entity.board;

import com.meatmeet6kae.entity.user.User;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK & 자동 증가
    @Column(name = "board_no")
    private int boardNo;
    @Column(name = "board_category", length = 10, nullable = false)
    private String boardCategory;
    @Column(name= "board_tag", length = 10)
    private String boardTag;
    @Column(name = "title", length = 100, nullable = false)
    private String title;
    @Lob // 대용량 데이터 저장
    @Column(name = "content", nullable = false)
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "create_date", updatable = false, insertable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "update_date")
    private LocalDateTime updateDate;
    @ManyToOne
    @JoinColumn(name="login_id", referencedColumnName = "loginId") // FK 설정
    private User user;
    @Column(name = "view_count", columnDefinition = "int default 0")
    private int viewCount;

    // Getters and Setters

    public int getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(int boardNo) {
        this.boardNo = boardNo;
    }

    public String getBoardCategory() {
        return boardCategory;
    }

    public void setBoardCategory(String boardCategory) {
        this.boardCategory = boardCategory;
    }

    public String getBoardTag() {
        return boardTag;
    }

    public void setBoardTag(String boardTag) {
        this.boardTag = boardTag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
