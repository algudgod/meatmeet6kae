package com.meatmeet6kae.common.enums;

public enum BoardCategory {

    // 아래에 나열한 항목만 안전하게 사용하도록 함.
    NOTICE("공지사항", "공지사항을 알려드리겠습니다."),
    HOT("HOT","인기글을 모아보는 공간입니다."),
    FREE("자유게시판","육깨비를 좋아하는 사람들끼리 자유롭게 소통하는 곳입니다."),
    WORK("직장인게시판",""),
    INFO("정보게시판","유머,뉴스, 레시피 등 다양한 정보가 가득한 곳입니다.");

    private final String boardCategoryName;
    private final String boardDescription;

    BoardCategory(String boardCategoryName, String boardDescription) {
        this.boardCategoryName = boardCategoryName;
        this.boardDescription = boardDescription;
    }

    public String getBoardDescription() {
        return boardDescription;
    }

    public String getBoardCategoryName() {
        return boardCategoryName;
    }

}
