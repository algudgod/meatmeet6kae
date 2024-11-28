package com.meatmeet6kae.entity.comment;

import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK & 자동 증가
    @Column(name = "comment_no")
    private int commentNo;
    @ManyToOne
    @JoinColumn(name="board_no", nullable = false) //FK 설정
    private Board board;
    @ManyToOne
    @JoinColumn(name="login_id", nullable = false) //FK 설정
    private User user;
    @Column(name= "content", nullable = false)
    private String content;
    @Column(name = "create_date", updatable = false, insertable = false)
    private LocalDateTime createDate;
    @Column(name = "update_date")
    private LocalDateTime updateDate;
    @Column(name = "del_yn", nullable = false, insertable = false)
    private String delYn;


    // Getters and Setters
    public int getCommentNo() {
        return commentNo;
    }

    public void setCommentNo(int commentNo) {
        this.commentNo = commentNo;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getDelYn() {
        return delYn;
    }

    public void setDelYn(String delYn) {
        this.delYn = delYn;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentNo=" + commentNo +
                ", board=" + board +
                ", user=" + user +
                ", content='" + content + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", delYn='" + delYn + '\'' +
                '}';
    }
}
