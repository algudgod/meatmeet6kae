package com.meatmeet6kae.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CommentDto {

    private int commentNo;
    private int boardNo;
    private String nickname;
    @NotBlank(message = "댓글은 필수 항목입니다.")
    @Size(min = 1, max = 300, message = "댓글은 1자 이상, 300자 이하로 입력해주세요.")
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createDate;


    public int getCommentNo() {
        return commentNo;
    }

    public void setCommentNo(int commentNo) {
        this.commentNo = commentNo;
    }

    public int getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(int boardNo) {
        this.boardNo = boardNo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    @Override
    public String toString() {
        return "commentDto{" +
                "commentNo=" + commentNo +
                ", boardNo=" + boardNo +
                ", nickname='" + nickname + '\'' +
                ", content='" + content + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
