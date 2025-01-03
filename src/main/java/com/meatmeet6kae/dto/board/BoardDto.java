package com.meatmeet6kae.dto.board;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class BoardDto {

    private int boardNo;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min=3, max = 100, message = "3자 이상, 100자이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수 항목입니다.")
    @Size(min = 5, max = 2000, message = "내용은 5자 이상, 2000자 이하로 입력해주세요.")
    private String content;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String boardCategory;

    private String nickname; // 작성자 닉네임 추가

    private int categoryBoardNo; // 카테고리별 게시글 번호

    private int viewCount;

    private List<MultipartFile> images; // 이미지 필드 추가




    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createDate;

    private int commentCount; // 댓글 수 필드 추가


    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getCategoryBoardNo() {
        return categoryBoardNo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setCategoryBoardNo(int categoryBoardNo) {
        this.categoryBoardNo = categoryBoardNo;
    }

    public int getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(int boardNo) {
        this.boardNo = boardNo;
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

    public String getBoardCategory() {
        return boardCategory;
    }

    public void setBoardCategory(String boardCategory) {
        this.boardCategory = boardCategory;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "BoardDto{" +
                "boardNo=" + boardNo +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", boardCategory='" + boardCategory + '\'' +
                ", nickname='" + nickname + '\'' +
                ", categoryBoardNo=" + categoryBoardNo +
                ", viewCount=" + viewCount +
                ", createDate=" + createDate +
                '}';
    }
}
