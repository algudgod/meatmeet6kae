package com.meatmeet6kae.common.enums;

public enum BoardTag {
    RECIPE("맛집", "정보게시판"),
    COOK("요리", "정보게시판"),
    HUMOR("유머", "정보게시판"),
    NEWS("뉴스", "정보게시판");

    private final String displayName;
    private final String applicableBoard; // 조건: 사용 가능한 게시판 이름

    BoardTag(String displayName, String applicableBoard) {
        this.displayName = displayName;
        this.applicableBoard = applicableBoard;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApplicableBoard() {
        return applicableBoard;
    }
}