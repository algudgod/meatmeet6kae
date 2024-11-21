
# MeatMeet6kae 개발 일지

### 프로젝트 개요
- 프로젝트명: MeatMeet6kae
- 목적: 백엔드 개발 역량 강화 및 포트폴리오 제작을 위한 회원 관리 및 게시판 시스템 구현.
- 기술 스택:
  - backend: Java, Spring Boot, Gradle, Spring Security, JPA(MySQL).
  - frontend: Thymeleaf, HTML/CSS, JavaScript.
  - API: Daum 주소 검색, Kakao Map API
  - 기타: Naver SMTP(이메일발송).
---

## 📅 2024.11.20
### 오늘 한 일 (요약) 
- 조회수 로직 개선
- 글쓰기 시 현재 게시판을 표시하되, 게시판 선택 기능 추가.
- 카테고리 별 고유한 게시글 번호 부여 기능 구현.

### 이유 
- 사용자의 경험 상 비회원의 경우 글 읽기가 가능하나 조회수가 올라가지 않는 것이 일반적임을 깨달음.
- 사용자의 경험의 향상을 위해 글쓰기 시 현재 게시판을 보여주고, 선택도 가능하도록 개선하는 것이 필요하다고 느낌.
- 각 게시판마다 게시글번호를 부여하여, 정렬하고자 함.

### 내용
#### 1. 조회수 로직 개선
- user가 null이 아닌 조건을 추가.
  ```plaintext 
  if user != null 코드 추가.
  
#### 2. 글쓰기 페이지 개선
- 현재 게시판을 기본값으로 표시하되, 드롭 다운으로 다른 게시판 선택 가능하도록 HTML 수정.
- 서버에서 게시판 목록 데이터를 받아와 동적으로 렌더링.
  ```plaintext 
  <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton">
    <th:block th:each="category : ${boardCategorys}">
        <li>
            <a class="dropdown-item" href="#"
               th:data-value="${category}"
               th:text="${category.boardCategoryName}"
               onclick="selectCategory(this)">
            </a>
        </li>
    </th:block>
  </ul>
  
#### 3. 카테고리 별 번호 부여
- 레포지토리에서 `ROW_NUMBER()`를 사용하여 SQL 쿼리를 작성하고, 카테고리 별로 게시글 번호를 동적으로 계산.
  ```plaintext 
    @Query(value = "SELECT " +
            "b.board_category, " +
            "ROW_NUMBER() OVER (PARTITION BY b.board_category ORDER BY b.board_no) AS categoryBoardNo, " +
            "b.board_no, " +
            "b.title, " +
            "b.content, " +
            "u.nickname, " +
            "b.create_date " +  // create_date 추가
            "FROM board b " +
            "JOIN user u ON b.login_id = u.login_id " + // User 테이블과 조인
            "WHERE b.board_category = :boardCategory",
            nativeQuery = true)
    List<Object[]> findByCategoryWithRowNumber(@Param("boardCategory") String boardCategory);
- 카테고리별 ROW_NUMBER를 계산하여 동적으로 번호가 매겨진 게시글 정보를 가져옴.
- SELECT로 boardCategory를 가져오고, ROW_NUMBER로 각 행에 고유한 번호(categoryBoardNo)를 붙임.
  boardCategory 기준으로 데이터를 partition하고, 번호를 부여한 후, order by boardNo 순으로 정렬.
  부여된 번호를 categoryBoardNo으로 지정.
  boardCategory 파라미터 값이 :boardCategory에 대입되어 필터링.

 **JOIN 활용**
 - board 테이블과 user 테이블을 조인하여 작성자 닉네임(nickname)을 가져옵니다.
 - board.user_id와 user.id를 연결(JOIN)하여 닉네임 정보를 가져옵니다.
 - 게시판의 작성자 정보를 표시하기 위해 JOIN이 필요합니다.

 **쿼리 필드 설명**
 - b.boardCategory: 게시글의 카테고리
 - ROW_NUMBER() OVER(PARTITION BY b.boardCategory ORDER BY b.boardNo): 카테고리별로 순번(categoryBoardNo)을 생성
 - b.boardNo: 게시글 고유 번호
 - b.title: 게시글 제목
 - b.content: 게시글 내용
 - u.nickname: 작성자의 닉네임

- 서비스에서 Dto로 변환하여 처리.
  ```plaintext
      // 특정 카테고리 별로 고유한 번호가 매겨진 게시글을 조회
    public List<BoardDto> getBoardWithCategoryNumbers(String boardCategory){
        // 레포지토리에서 List<Object[]> 형태로 반환하는 findByCategoryWithRowNumber을 사용하기 위해 호출
        List<Object[]> rows = boardRepository.findByCategoryWithRowNumber(boardCategory);
        /* Object[] 배열로 담겨있는 여러개의 데이터를 필드명으로 명확하게 보기 위해 Dto 객체로 변환.
        변환된 객체를 한 곳에 모아 저장해야하는데, 빈 저장소를 만들어 준비.
        반복문으로 List<Object[]> 데이터를 하나씩 꺼내서 Dto로 변환. set으로 저장할 데이터 넣기.
            List<Object[]>는
               [0]: boardCategory
               [1]: categoryBoardNo (ROW_NUMBER로 생성된 카테고리별 글 번호)
               [2]: boardNo (전체 고유 글 번호)
               [3]: title
               [4]: content
        */
        List<BoardDto> boards = new ArrayList<>(); //빈 저장소
        for(Object[] row : rows) {
            BoardDto boardDto = new BoardDto();
            boardDto.setBoardCategory((String) row[0]);
            boardDto.setCategoryBoardNo(((Number) row[1]).intValue());
            boardDto.setBoardNo(((Number) row[2]).intValue());
            boardDto.setTitle((String) row[3]);
            boardDto.setContent((String) row[4]);
            boardDto.setNickname((String) row[5]);
            boardDto.setCreateDate(((Timestamp) row[6]).toLocalDateTime());

            boards.add(boardDto);
        }
        // Dto객체 오름차순으로 정렬
        //sort()사용법. board2의 boardNo와 board1의 boardNo를 비교하여 board2가 board1보다 크면 (즉, 내림차순 정렬) 앞에 오도록 설정
        boards.sort((board1, board2) -> Integer.compare(board2.getBoardNo(), board1.getBoardNo()));
        return boards;
    }

  ```plaintext
    @GetMapping("/boardList")
    public String boardList(@RequestParam(value = "boardCategory", required = false)String boardCategory, HttpSession session, Model model){

        User user = (User)session.getAttribute("user");
        model.addAttribute("user",user);

        // 모든 열거형(ENUM 반환)
        BoardCategory[] boardCategories = BoardCategory.values();
        System.out.println("boardCategories"+boardCategories);
        System.out.println("Arrays.toString().boardCategories="+ Arrays.toString(boardCategories));
        for(BoardCategory boardCategorys: boardCategories){
        } // 모든 열거형 뷰로 전달
        model.addAttribute("boardCategories",boardCategories);

        // boardCategory가 없다면 기본값으로 FREE 설정
        if(boardCategory == null|| boardCategory.isEmpty()){
            boardCategory = "FREE";
        }
        // 선택된 카테고리는 String으로 넘어오기 때문에, valueOf로 Enum 변환(오류방지)
        BoardCategory category = BoardCategory.valueOf(boardCategory);
        model.addAttribute("category",category);

        List<BoardDto> boardDtos = boardService.getBoardWithCategoryNumbers(boardCategory);
        model.addAttribute("boards", boardDtos);
  
        model.addAttribute("boardCategoryName",category.getBoardCategoryName());
        model.addAttribute("boardDescription",category.getBoardDescription());
        model.addAttribute("page","boardList");
        return "navigation/boardList";
  }

  <tr th:each="board : ${boardDtos}">
      <td th:text="${board.categoryBoardNo}">1</td> <!-- 카테고리별 고유 번호 -->
      <td th:text="${board.title}"> 제목 </td>
      <td th:text="${board.nickname}">닉네임</td>
      <td th:text="${board.createDate.toLocalDate().isEqual(T(java.time.LocalDate).now()) ? #temporals.format(board.createDate, 'HH:mm') : #temporals.format(board.createDate, 'yy.MM.dd')}"></td>
      <td th:text="${board.viewCount}">조회수</td>
  </tr>

#### 문제점 및 해결 방법
- **문제1:** 게시글 목록을 조회 시 필요한 필드를 제대로 확인하지 않아 오류 발생.
- **해결방법:** 사용하고 있는 dto에 필드의 유무를 확인하고 추가.
- **문제2:** 엔티티 -> Dto로 변환하는 과정에서 바인딩 오류
- **해결방법:** html 코드를 차근히 읽어가며 엔티티가 있는지 확인하며 수정.



### 느낀점
- 작은 UX 개선이 사용자의 입장에서는 큰 차이를 만들 수 있다는 점을 다시금 체감하였다.
- ROW_NUMBER()를 사용한 SQL 쿼리 작성은 정말 힘들었다. 하지만 꼭 필요하다는 것을 알고 끝까지 시도했고, 성공하고나서 성취감이 컸다.
  어려운 길일 수록 침착하게 계층과 코드의 흐름을 이해하며 코드를 작성하려고 노력하였다. 오류가 나는 것은 당연하다고 생각하며 당황하지 않고 읽고 해결하려고 했다. Dto에 빠진 필드들을 추가하고, 뷰에서 처리하는 도중에 빠드려서 오류가 났지만 하나씩 해결하면서 결국엔 목록을 불러오는데 성공했다.

### 배운점
- 글쓰기 페이지에서 드롭다운을 사용하고자 bootStrap에서 코드를 참고하였는데, href="#"이 무엇일까? 궁금했다. a 태그에서는 브라우저 기본 동작으로 페이지의 최상단으로 이동하거나, 특정 상황에서 페이지를 새로고침할 수 있는데, 이런 동작은 사용자가 원하지 않는 결과를 초래할 수 있다. 그래서 이러한  의도치 않은 페이지 새로고침을 방지하기 위해 href="#" 을 사용하는 것을 알게 되었다.
- SQL을 오랜만에 사용해보면서 다시 한 번 사용법을 되뇌어 활용해보면서 잘 작성할 수 있었다.
- Thymeleaf에서 리스트를 출력할때 th:each와 th:text 등 활용하여 반복문으로 데이터를 처리하는 법을 복습하였다.


---
## 📅 2024.11.20
### 오늘 한 일 (요약)
- 컨트롤러에서 비즈니스 로직 제거 및 서비스 계층으로 분리.
- 내 정보 보기: 폼 UI 개선 및 닉네임 필드 추가.
- 스크롤바 위치에 따라 움직이는 화면 고정.
- 게시글 조회수 로직 수정: 작성자가 자신의 글을 조회할 경우 조회수 증가 방지.
- 게시글 상세조회에 `BoardCategory` Enum 적용 및 컨트롤러와 서비스 역할 분리.

### 이유
- 컨트롤러는 HTTP 요청 처리 및 데이터 전달 역할에 집중하고, 비즈니스 로직은 서비스 계층에서 처리하도록 리팩토링하여 가독성, 재사용성, 유지보수성을 강화하기 위함.
- 스크롤바 유무에 따라 화면 레이아웃이 달라지는 불편함을 해소하고자 화면 고정 방식을 도입.
- 게시글 조회수 증가 로직이 부정확했던 문제를 해결하고, 데이터의 신뢰성을 높이고자 로직을 수정.
- 게시글 상세 조회 시 카테고리를 String 대신 Enum으로 처리하여 오류 방지, 가독성 향상, 그리고 로직의 안정성을 높이기 위함.
- 
### 내용
#### 1. 컨트롤러와 서비스 계층 분리
- 게시글 상세 조회 비즈니스 로직을 컨트롤러에서 서비스로 이동.
  ```plaintext 
    // 변경 전 (컨트롤러)
    Board board = boardService.getBoardByBoardNo(boardNo);
    model.addAttribute("board", board);
    
    // 카테고리 기본값 설정
    String boardCategory = board.getBoardCategory();
    if (boardCategory == null || boardCategory.isEmpty()) {
    boardCategory = "FREE";
    }
    model.addAttribute("boardCategory", boardCategory);
    return "boards/boardDetail";
    
    // 변경 후 (서비스 계층)
    @Transactional
    public Board getBoardDefaultCategory(int boardNo) {
    Board board = getBoardByBoardNo(boardNo);
    if (board.getBoardCategory() == null || board.getBoardCategory().isEmpty()) {
    board.setBoardCategory("FREE");
    }
    return board;
    }
    
    // 변경 후 (컨트롤러)
    Board board = boardService.getBoardDefaultCategory(boardNo);
    model.addAttribute("board", board);
    model.addAttribute("boardCategory", board.getBoardCategory());
    return "boards/boardDetail";

#### 2. 내 정보 보기 폼 수정
- 닉네임 필드를 추가하고, 데이터가 정렬되도록 폼 레이아웃을 개선.

#### 3, 스크롤바 위치 조정
- s레이아웃이 스크롤 유무에 따라 움직이는 문제를 해결하기 위해 스크롤바를 항상 표시하도록 수정.
  ```plaintext 
  /* styles.css */
  html {
  overflow-y: scroll; /* 스크롤바를 항상 표시 */
  }

#### 4. 게시글 조회수 로직 수정
- 작성자가 자신의 글을 조회했을 때 조회수가 올라가지 않도록 로직에 조건 추가.
  ```plaintext 
    @Transactional
    public void updateViewCount(int boardNo, User user) {
    Board board = getBoardByBoardNo(boardNo);
    if (user == null || !board.getUser().getLoginId().equals(user.getLoginId())) {
    board.setViewCount(board.getViewCount() + 1);
    }
  }
  
#### 5. 게시글 상세 조회에 `BoardCategory` Enum 적용.
- String 대신 `BoardCategory` Enum을 사용하도록 리팩토링하여 안정성을 강화.
- 컨트롤러에서 `BoardCategory` 처리 로직을 서비스로 이동하여 코드 간소화.
  ```plaintext
  controller 코드 추가.
    Board board = boardService.getBoardDefaultCategory(boardNo);
    model.addAttribute("board",board);

    BoardCategory[] boardCategories = BoardCategory.values();
    model.addAttribute("boardCategorys",boardCategories);

    BoardCategory category = BoardCategory.valueOf(board.getBoardCategory());
    model.addAttribute("boardCategory", category);
  
  html
  변경 전,
  <h2 class="boardDetail-title" style="font-size: 15px;" th:text="${boardCategory == 'NOTICE' ? '공지사항'
                : (boardCategory == 'HOT' ? 'HOT 게시판'
                : (boardCategory == 'FREE' ? '자유게시판' : '게시판'))}"> </h2>
  변경 후,
  <h2 class="boardDetail-title" style="font-size: 15px" th:text="${boardCategory.boardCategoryName}"></h2>
  
- `boardList`를 참고하여 작성을 완료하였으나, 컨트롤러에 비즈니스 로직을 서비스 계층으로 분리하는 리팩토링 추가 개선을 하고자 함.
    ```plaintext
  // 서비스: Enum 변환 및 반환 메서드 추가
  public BoardCategory[] getAllBoardCategories() {
  return BoardCategory.values();
  }
  
  public BoardCategory getBoardCategoryEnum(String boardCategory) {
  return BoardCategory.valueOf(boardCategory);
  }
  
  // 컨트롤러: Enum 관련 로직 제거 후 서비스 호출
  Board board = boardService.getBoardDefaultCategory(boardNo);
  model.addAttribute("board", board);
  model.addAttribute("boardCategorys", boardService.getAllBoardCategories());
  model.addAttribute("boardCategory", boardService.getBoardCategoryEnum(board.getBoardCategory()));

### 느낀점
- 컨트롤러와 비즈니스 로직을 서비스로 분리하면서 코드가 간결해지고, 유지보수가 쉬워졌다는 점에서 성취감을 느겼다. 조회수 로직에서 작성자의 조회를 제외하는 조건은 단순하지만 이 조건의 필요성을 알게되었고, 작은 기능이라도 하나씩 개선하고 완성하는 과정에서 성취감이 더욱 느껴지고, 점점 코드 작성이 재밌고 익숙해지는 것을 느낀다.
- `boardCategory` Enum을 활용해 카테고리 처리로직이 명확해지고 안정성이 향상된 점에서 앞으로의 확장 작업이나 수정 작업이 용이할 것 같다는 자신감이 생겼다.

### 배운점
- 컨트롤러와 서비스의 역할 분리가 가독성 및 유지보수성에 얼마나 중요한지 체감하고, 앞으로 코드를 작성할 때 각 계층의 역할을 명확히 해야겠다고 느낌.
- Enum 활용으로 로직의 단순화 및 안정성을 높이는 방법을 익힘.
- CSS를 활용하여 UI/UX를 개선하는 방법과 overflow-y 속성을 통한 레이아웃 고정 방법을 알게 됨.

### 예정 작업
- 게시판 기능 개선
  - 게시판 목록에서 말머리 기능 추가.
- 회원 관리 기능 개선
  - 회원 정보 수정 시 닉네임 변경 가능하도록 기능 확장.
- 게시판 공지사항을 상단에 고정하는 기능 구현.
- 댓글 기능 구현.
  - 게시글 하단에서 로그인한 회원이 댓글 작성, 수정, 삭제 가능.
- SNS 간편 로그인 도입.
- 배포환경 설정
  - CI/CD 파이프라인 구축.


---
## 📅 2024.11.19
### 오늘 한 일 (요약)
- **게시글 수정 및 삭제 기능 개선**
  - 수정 시 **유효성 검사** 로직 추가.
  - 삭제 시, 유효하지 않은 요청에 대해 간단한 **null 처리**로 대응.
  - 수정과 삭제 로직 대부분을 **서비스 계층으로 이동**하여 재사용성과 가독성을 높임.
  - 데이터의 무결성을 유지하기 위해 엔티티 대신 DTO를 사용하도록 변경.

-  **게시판별 부가 설명 설정**
  - 게시판마다 다른 부가 설명을 보여주기 위해 **열거형(Enum)**을 사용.
  - 게시판 카테고리를 Enum으로 정의하여 가독성과 유지보수성을 향상.

- **CSS 리팩토링**
  - 전체 레이아웃 및 테이블 디자인 수정.
  - 사이드바 및 구분선 스타일 추가.
  - 게시판 제목 및 버튼 디자인 개선.

- **데이터 무결성을 위한 리팩토링**
  - 데이터 임의 조작 방지를 위해 엔티티 대신 DTO로 수정 및 삭제 요청을 처리.
  - 컨트롤러에서 작성했던 세부 로직을 **서비스 계층으로 최대한 이동**하여 역할을 명확히 분리.
  
- **게시글 조회수 증가 로직 구현**
  - 게시글 상세보기 시, 조회수가 1씩 증가하도록 로직 추가.

- ** 게시판 새글 수 표시**
  - 게시판 목록 상단에 오늘날짜로 작성된 새 글의 수와 전체 글의 수를 보여주는 기능 추가.
  - 오늘 날짜로 작성된 게시글만 필터링하여 새 글의 수를 계산하도록 로직 구현. 

### 이유
- **유효성 검사 강화**: 사용자가 임의로 URL을 통해 데이터를 조작하지 못하도록 보안을 강화.
- **Enum 도입**: 게시판마다 다른 설명을 추가하면서 유지보수성과 관리 편의성을 높이기 위해.
- **DTO 활용**: 엔티티를 직접 노출하지 않음으로써 데이터 무결성을 유지하고 클라이언트 조작 방지.
- **UI 개선**: 사용자 경험을 개선하고, 프로젝트의 완성도를 높이기 위해.


### 내용

#### 1.게시글 수정, 삭제
- 게시글 존재 여부와 작성자 일치 여부를 확인하는 유효성 검사 추가.
- 서비스와 컨트롤러를 분리하여 역할을 명확화. 

#### 2. 게시판 별 제목과 부가 설명 설정 (Enum 도입)
- 연관된 상수의 값을 그룹화하고, 코드의 가독성을 높이며 실수를 방지하게 위해 사용함. 존재하지 않는 값의 입력, 소문자 입력, 문자열 오타 등으로 많은 오류가 발생하여 타입의 안정성이 부족한데, 내가 지정한 값들만 나열하여 사용할 수 있게 할 수 있음.
    ```plaintext
    package com.meatmeet6kae.common.enums;
  
    public enum BoardCategory {
  
      // 아래에 나열한 항목만 안전하게 사용하도록 함.
      NOTICE("공지사항", "공지사항을 알려드리겠습니다."),
      HOT("HOT","인기글을 모아보는 공간입니다."),
      FREE("자유게시판","육깨비를 좋아하는 사람들끼리 자유롭게 소통하는 곳입니다.");
  
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
- 재사용성을 높이기 위해 패키지를 따로 관리하도록 함.

#### 3. 게시글 조회수 증가 로직
- 게시글 상세보기 시 조회수를 1 증가하는 로직을 추가.
  ```plaintext
      @Transactional
    public void updateViewCount(int boardNo){
        Board board = getBoardByBoardNo(boardNo);

        board.setViewCount(board.getViewCount()+1);
    }

#### 4. 게시판 새 글 수 표시
- 게시판 상단에 오늘 날짜로 작성된 새 글의 수와 전체 글의 수를 함께 표시하도록 기능을 추가.
- `LocalDate`를 활용하여 오늘 날짜를 기준으로 새글을 필터링하며 계산하여 구현.

### 느낀점

- 컨트롤러와 서비스의 역할을 분리해보면서 코드의 가독성과 재사용성이 향상되는 것을 느끼고, 최대한 구분하려고 했다. 이번 프로젝트에서 Dto를 처음 활용해보면서 데이터의 흐름을 좀 더 알 수 있었다. 프로젝트를 진행하면서 Dto와 entity가 필요한 상황과 그 중요성을 더욱 이해할 수 있게 되었다.
- 게시판 카테고리와 부가설명을 작성하면서, 서비스, 컨트롤러 그리고 뷰에서 동일한 로직을 반복적으로 추가하는 문제를 해결하고 싶었다. 고정된 값을 적용해야하는 현재 상황에서 Enum을 다시 공부하고 적용해보았는데 그 결과, 코드의 가독성과 유지보수성이 눈에 띄게 향상되었음을 느꼈다. 특히, 새로운 게시판 카테고리를 추가하거나 수정할때 Enum 클래스를 통해 관리하면 작업 범위가 최소화된다는 점에서 효율적인 접근방식이었다고 생각한다.
- 조회수 증가나 새 글 수 표시 기능은 사용자에게 익숙한 기능이라고 생각하여 추가하고 싶었다. 실제로 UI에서 확인해보니 훨씬 더 보기 좋았다.
- 새 글 수를 보여주는 로직은 필터링과 데이터 계산의 기본적인 원리를 다시복습하는 기회가 되어서 좋았다. 이러한 작은 기능들도 개발자가 사용자에게 편리한 기능을 제공해줄 수 있는 요소임을 깨달았다. 

### 배운 점
- **유효성 검사와 데이터 검증**:
  - 요청 데이터의 유효성을 항상 확인해야 한다는 점을 다시 깨달음.
  - 특히, 수정이나 삭제와 같은 작업일수록 철저한 검증이 필요 하는 것을 알게 됨.
  - 가독성 높은 코드는 개발자에게 크게 도움이 되는 것을 느낌.
  - DTO를 활용하여 데이터를 안전하게 처리하고 클라이언트 조작을 방지하는 것이 얼마나 중요한지 알게 됨.
  - **Enum 활용법**: Enum을 사용하면 유지보수성과 가독성이 크게 향상됨.
  - **CSS와 UI/UX 개선의 중요성**: 사용자가 익숙한 디자인으로 작업하려고 하였고, 통일성 있는 디자인은 프로젝트의 완성도를 크게 높이고 사용자 만족도를 증대시킴.

### 예정 작업
- 로그인 후 내 아이디 클릭 시, 동작하는 오픈캔버스 작동 확인.
- 게시판 기능 개선
  - 게시판 목록에서 말머리 기능 추가.
- 회원 관리 기능 개선
  - 회원 정보 수정 시 닉네임 변경 가능하도록 기능 확장.
  - 회원 정보 페이지에서 닉네임 표시 추가.
- 게시판 공지사항을 상단에 고정하는 기능 구현.
- 댓글 기능 구현.
  - 게시글 하단에 로그인한 회원은 댓글 작성, 수정, 삭제할 수 있음.
- SNS 간편 로그인 도입.
- 배포환경을 위한 CI/CD 파이프라인 구축.


---
## 📅 2024.11.18
### 오늘 한 일 (요약)
- `header`의 로그인 버튼이 정상적으로 작동하도록 세션 확인하여 로그인 상태 확인하고 설정.
- 게시글 수정 기능 구현 및 보안 로직 추가.
  - 사용자가 본인이 작성한 게시글만 수정할 수 있도록 `equals()`활용하여 작성자 확인 로직 추가.
- 게시글 삭제 기능 구현.
  - 작성자만 삭제할 수 있도록 제한하며 아닌 경우 게시판 목록으로 리다이렉트.
- HTML/CSS 수정
  - 구분선 스타일 (#888, 중간 회색으로 통일)
  - 추가된 게시글 상세페이지, 게시글 수정 form을 기존 디자인으로 통일.

### 이유
- 로그인 체크 및 세션 관리는 게시글 수정 시, 본인만 수정할 수 있도록 권한을 제한하는 보안적인 이유로 필요함.
- UI/UX는 항상 통일하여 시각적으로 보기 좋게 개선하고자 함.

### 내용
- 기본적인 CRUD 중 R(상세조회), U(수정), D(삭제)를 구현 함.
  - 로그인한 사용자와 로그인하지 않은 사용자 모두 글을 읽을 수 있으나 게시글의 수정과 삭제는 로그인한 회원이면서 작성자의 id와 동일해야 가능하게 함.  
  - 공통 디자인을 사용하여 form을 통일함.

### 느낀점
- 사용자 권한 체크 부분을 추가하면서 로그인된 사용자와 게시글 작성자가 일치하지 않으면 수정할 수 없도록 하여 보안 강화에 대한 중요성을 느꼈다.
- UI 통일성을 위해 버튼과 구분선을 통일하면서, 점차 일관성있는 UI가 되고 있음을 느낀다.

### 배운점
- 세션 처리와 로그인한 상태 관리를 더욱 명확하게 이해할 수 있었다.
- 게시글 작성자 확인을 위해 equals() 메서드를 활용하는 방법을 찾았는데, 오류가 나서 내일 보완할 예정이다. 보안을 향상시키는 것이 중요하다는 것을 배웠다.

### 예정 작업
- 작성자와 세션 상태의 loginId 비교 로직 추가.
- 로그인 후 내 아이디 클릭 시, 동작하는 오픈캔버스 작동 확인.
- 게시판 기능 개선
  - 게시판 목록에서 말머리 기능 추가.
  - 게시글 목록에서 카테고리 및 말머리 기능 개선(enum 사용).
  - - 게시글 조회 시, 조회수 증가 로직 추가.
- 회원 관리 기능 개선
  - 회원 정보 수정 시 닉네임 변경 가능하도록 기능 확장.
  - 회원 정보 페이지에서 닉네임 표시 추가.
- 게시판 공지사항을 상단에 고정하는 기능 구현.
- 댓글 기능 구현.
  - 게시글 하단에 로그인한 회원은 댓글 작성, 수정, 삭제할 수 있음.
- SNS 간편 로그인 도입.
- 배포환경을 위한 CI/CD 파이프라인 구축.

### 회고
게시글 수정 및 삭제에서 보안 강화와 UI통일성을 위한 노력으로 조금 더 완성도가 높아진 느낌이다. 예정 작업이 아직 많은데, 최대한 구현하여 사용자에게 친화적인 기능을 만들고 싶다.
오늘은 특히 URL 연결 오류나 세션 처리 문제로 시간이 많이 소요되었지만, 이를 해결하며 문제 해결 능력이 늘었다는 게 느껴져서 좋았다. 오타나 작은 실수가 전체 흐름에 큰 영향을 미칠 수 있다는 점을 깨닫고, 더욱 세심하게 코드와 연결 상태를 점검해야겠다고 다짐했다.

---
## 📅 2024.11.15
### 오늘 한 일 (요약)
- 기본 게시판 페이지를 자유게시판으로 설정.
  - 'boardCategory'가 없을 경우, 기본 값을 'FREE'로 설정하는 컨트롤러 코드 추가.
- 게시글 목록에서 보여주는 작성자 정보를 `loginId`에서 `nickname`으로 변경.
- 게시판 목록 페이지에서 제목의 정렬 방식 개선 및 스타일 수정.

### 이유
- 자유게시판을 기본 페이지로 설정한 이유는 사용자가 접근했을 때 별도의 카테고리를 지정하지 않아도 가장 많이 사용하는 게시판을 바로 볼 수 있도록 함.
- 작성자를 'loginId'에서 'nickname'으로 변경한 이유는 사용자의 개인정보의 노출을 최소하 하기 위함.

### 내용
- 'boardCategory'의 값이 없거나 빈 값일 경우, 기본값을 "FREE"로 설정하도록 함.
  ```plaintext
  if (boardCategory == null || boardCategory.isEmpty()) {
  boardCategory = "FREE";
  }
- 게시글 목록 페이지에서 노출되는 작성자의 정보 변경.
  ```plaintext
  <td th:text="${board.user.nickname}">닉네임</td>

- 게시판의 제목과 설명을 직관적으로 볼 수 있도록 개선.
  ```plaintext
  <h2 class="board-title" th:text="${boardCategory == 'NOTICE' ? '공지사항'
  : (boardCategory == 'HOT' ? 'HOT 게시판'
  : (boardCategory == 'FREE' ? '자유게시판' : '게시판'))}">
  </h2> 

### 느낀점
- 오늘 수정한 부분들 덕분에 화면이 한층 개선되었다. 공지사항, HOT(인기글), 자유게시판 중, 자주 사용하는 게시판을 기본 게시판으로 설정한 덕분에 별도의 선택없이 편하게 접근할 수 있게 한 점이 만족스럽다.
- nickname은 초기 설계에서 신경쓰지 않아 설계하지 않았는데, 사용함으로써 사용자의 개인정보 보호를 강화할 수 있던 것도 좋은 결정이었다.
- 오늘은 문서화 작업을 중점으로 한 결과, 단순한 변경밖에 하지 못했지만, 사용자의 입장이 되어 생각하고 개선할 수 있는 부분이 많다는 것을 다시 한 번 느꼈다.

### 배운점
`Thymeleaf`를 계속해서 활용하면서 템플릿에 더욱 익숙해지고 있고, 사용자의 개인정보의 중요성을 다시 한 번 더 인식하게 되었다.


---
## 📅 2024.11.14
### 오늘 한 일 (요약)
- **게시판 리팩토링:**`boardCode`를 `boardCategory`로 변경하고, `boardTag`필드 추가.
- **회원 관리 개선:** 회원 `nickname`필드 추가 및 닉네임 중복 체크 기능 구현.
- **회원가입 시스템 개선:** 닉네임 입력 및 중복 체크 기능 추가, 테스트 완료.

### 이유
- 게시판 카테고리와 태그 네이밍이 직관적이지 않아 **가독성 향상**을 위해 변경.
- 회원 ID를 직접 노출하는 대신 **닉네임을 활용**하여 개인정보 보호 강화.
- 회원 가입 시 **닉네임 중복 문제를 방지**하기 위하여 중복체크 추가 기능 도입.

### 내용
#### 1.게시판 시스템 확장 및 개선 리팩토링
- 기존 `boardCode`를 `boardCategory`로 변경하여 더 명확하게 구분할 수 있도록 함.
- 말머리 기능 추가를 위해 `board_tag` 필드를 추가.
    - `boardCategory`: 게시판의 큰 카테고리 구분 (예: 자유게시판, 공지사항).
    - `boardTag`: 세부 분류를 위한 태그 (예: 일상, 맛집공유 등).
- `BOARD` 테이블 정의서 수정:
  ```plaintext
  board_no         int(11)      -- 게시글 번호 (PK)
  board_category   varchar(10)  -- 게시판 카테고리 (예: 자유게시판, 공지사항)
  board_tag        varchar(10)  -- 게시판 태그 (말머리, 예: 유머, 맛집공유)
  title            varchar(100) -- 게시글 제목
  content          mediumtext   -- 게시글 내용
  create_date      datetime     -- 게시글 생성일 (기본값: 현재 시간)
  update_date      datetime     -- 게시글 수정일
  login_id         varchar(20)  -- 작성자 회원 ID (FK)
  view_count       int(11)      -- 조회수 (기본값: 0)

#### 2. 회원 시스템 개선.
- 게시글 작성 시 **회원의 고유 ID 대신 닉네임**을 사용하여 개인정보 노출 최소화.
- `nickname` 필드에 **UNIQUE 제약 조건** 설정.
- `USER` 테이블 정의서 수정:
    ```plaintext
    login_id         varchar(20)  -- 회원 고유 ID (PK)
    use_yn           char(1)      -- 사용 여부 (Y/N)
    password         varchar(100) -- 비밀번호
    name             varchar(50)  -- 회원 이름
    nickname         varchar(15)  -- 회원 닉네임 (UNIQUE)
    role             varchar(5)   -- 역할 (admin/user)
    gender           char(1)      -- 성별 (M/F)
    addr             varchar(100) -- 주소
    email            varchar(50)  -- 이메일 (UNIQUE)
    email_yn         char(1)      -- 이메일 수신 여부 (Y/N)
    create_date      datetime     -- 회원 가입 일자 (기본값: 현재 시간)
    withdraw_date    datetime     -- 회원 탈퇴 일자 (NULL 가능)
  
#### 3.닉네임 중복체크 기능 추가.
- 회원가입 시 닉네임 중복 체크 기능을 추가하여, 중복된 닉네임을 사용하지 않도록 개선.
- **JavaScript와 UserContoller**를 연동하여 비동기 방식으로 닉네임 중복 확인.
- 회원 가입 완료 시 닉네임을 환영 메세지에 표시하도록 수정.
- 표준 CSS로 변경

#### 문제점 및 해결 방법
- **문제1:** 닉네임 중복 체크 시 데이터가 null로 전달되는 문제 발생. 
- **해결방법:** Thymeleaf 폼에서 @modelAttribute 객체 바인딩 오류 수정 후 정상 동작 확인.

### 느낀점
- **네이밍의 중요성:** 개발 초기 단계에서 직관적이지 않은 네이밍 때문에 혼동이 잦았으나, 개선 후 코드 가독성이 크게 향상 됨
- **비동기 처리의 효율성:** `JavaScript`와 `Controller`의 페이지의 새로고침없이 서버와 데이터를 주고받는 비동기처리(Asynchronous Processing)를 해보면서 동기화 문제를 해결하는 방법을 배웠다.
    - 사용자가 닉네임을 입력하고 '중복확인' 버튼을 누르면, 페이지를 새로고침하지 않아도 서버로부터 즉각적인 응답을 받아 결과를 화면에 표시함.
    - 불필요한 페이지 새로고침을 없애고, 원활한 인터페이스를 제공함으로써 사용자가 편리하게 서비스를 이용하게 함.
    - 전체 페이지를 새로고침하지 않기 때문에 서버 리소스 사용을 줄여 성능을 개선.
- **문제 해결의 성취감:** 비동기 처리와 같은 기술을 다루면서 새로운 것을 익히고 배웠던 것을 복습하며 문제를 해결할 수록 자신감이 생김.

### 배운점
- Spring Boot의 다양한 기능(JPA, Validation Spring Security)을 사용해보면서 능력이 향상.
- `JPA`를 사용한 데이터베이스 쿼리 최적화의 중요성을 깨달음.
- `Thymeleaf`를 활용한 템플릿 엔진 사용법을 더 익힘.
- **JavaScript를 활용한 비동기 처리**와 폼 데이터 검증의 중요성을 느낌.
  - 사용자가 입력한 데이터를 서버에 전송하기 전에 클라이언트 측에서 미리 검증하는 것은 불필요한 서버 요청을 줄이고, 보안과 데이터 무결성을 보장하는데 중요하다.
  - 서버의 부담이 감소되며 사용자가 폼을 제출하기 전, 입력 오류를 즉시 확인할 수 있어 실수를 줄임.

### 다음 작업 예정
- 게시판 목록에서 말머리 기능 추가.
  - 게시글 목록에서 카테고리 및 태그 필터 기능 추가.
- 회원 관리 기능 개선
  - 회원 정보 수정 시 닉네임 변경 가능하도록 기능 확장.
  - 회원 정보 페이지에서 닉네임 표시 추가.
- 게시판 공지사항을 상단에 고정하는 기능 구현.
- 댓글 기능 구현.
  - 게시글 하단에 로그인한 회원은 댓글 작성, 수정, 삭제할 수 있음.
- SNS 간편 로그인 도입.
- 배포환경을 위한 CI/CD 파이프라인 구축.

### 회고
- 이번 프로젝트를 통해 문제 해결 능력과 기술 습득의 속도가 크게 향상됨을 느꼈다. 특히, 배웠던 것을 복습하고 새로운 기술을 도입하며 개선해 나가는 과정에서 지속적인 학습과 발전의 필요성을 크게 깨달핬다. 이 과정을 기록하는 것 또한 큰 도움이 되었다.
- 앞으로는 설계했던 기능들을 모두 구현하고, CI/CI 파이프 라인과 테스트 코드 작성을 통해 더 나은 품질의 코드로 개선할 것이며, 곧 배포 프로세스를 구축할 계획이다.


---
## 📅 ~ 2024.11.13
### 한 일 (요약)
- **프로젝트 환경 구성:** IntelliJ와 Spring Boot, Gradle을 활용한 프로젝트 세팅 및 GitHub 연동.
- **회원 관리 시스템 구현:** 회원가입, 로그인, 정보 수정, 탈퇴(삭제) 기능 개발.
  - 이메일 인증 시스템 도입 및 비밀번호 암호화.
  - Daum 주소 API를 사용한 주소 검색 기능 추가.
- **UI/UX 개선:** 공통 헤더, 푸터 구성 및 CSS 통일.
  - JavaScript를 활용한 네비게이션 메뉴 `active` 클래스 동적 적용.
- **지도 API 연동:** Kakao Map API를 사용한 위치 정보 표시 기능 추가.
- 
### 이유
- **회원 관리 및 게시판 시스템 구현:** 백엔드 개발자로서 필요한 기본 CRUD 기능들을 직접 구현하여 역량을 강화하기 위함.
- **이메일 인증 도입** 사용자의 고유한 메일을 사용하여 신뢰성을 높이기 위해 이메일 인증을 통한 회원가입 절차 도입.
- **주소 검색 및 지도 API 연동:** 사용자의 편의성을 고려하여 주소 입력과 검색 기능 추가.
- **UI/UX:** 프로젝트의 완성도를 높이기 위해 BootStrap의 깔끔한 디자인으로 통일.
- 
### 내용
#### 1. 프로젝트 초기 설정 및 회원 관리 시스템 구축
- IntelliJ에서 Spring Boot 기반 프로젝트를 생성하고, Gradle을 통해 의존성 관리 설정.
- USER 테이블 설계 및 회원 CRUD 기능을 위한 엔티티, 서비스, 레포지토리 작성.
- 회원 가입, 로그인, 회원 정보 수정, 탈퇴 기능 구현.
  - 로그인 시 세션 관리를 통해 사용자의 상태를 유지.
  - 회원 가입 시 비밀번호 암호화를 위해 Spring Security 사용.
#### 2. 이메일 인증 시스템 및 데이터 유효성 검증
- JavaMailSender를 활용해 Naver SMTP 서버를 통한 이메일 발송 기능 추가.
- 회원가입 시 인증 이메일 발송 및 이메일 인증 여부(emailYn) 확인.
- DTO 및 @Valid 어노테이션을 활용하여 데이터 유효성 검증 강화.
  - 클라이언트 측 JavaScript와 서버 측 검증 로직 통합.
- 이메일 중복 체크 및 로그인 ID 중복 체크 기능 추가.
#### 3. 주소 검색 및 회원 탈퇴 기능 개선
- Daum 주소 API를 활용한 주소 검색 기능 추가.
  - 회원가입 및 정보 수정 시 주소 자동 입력 기능 구현.
- 실제 데이터를 삭제하지 않고 useYn 필드 값을 'N'으로 변경하여 탈퇴 처리.
  - 탈퇴 회원의 경우 로그인 불가 처리 추가.
#### 4. UI/UX 개선 및 지도 기능 연동
- 공통 레이아웃 적용(헤더, 푸터, 스타일 통일).
- JavaScript를 활용하여 네비게이션 메뉴에서 현재 페이지에 'active' 클래스 동적 적용.
- Kakao Map API를 활용해 Location(오시는길) 페이지에 지도 표시 기능 추가.
- 
### 느낀점
- 프로젝트 관리의 중요성: 개인 프로젝트를 진행하면서 기록하는 것에 어려움을 느꼈지만, 꼭 필요하기 떄문에 여러가지 방법을 시도하다가 체계적으로 기록하지 못한 점이 아쉬웠고, 이번 계기를 통해 개발 로그가 얼마나 중요한지 다시한 번 느끼게 되었음.
- 끈기와 문제 해결 능력: 이메일 인증이나 주소 검색 API 연동 등 처음 접하는 기술들을 다루면서 많은 시행착오를 겪었으나 이를 포기하지 않고 해결하면서 개발자로서의 자신감을 키울 수 있었음.
- 지속적인 개선 의지: 현재 기능에 만족하지 않고, 생각나는 기능들을 따로 적어두어 추가적으로 계속 구현하고자 함. SNS 로그인 및 CI/CD 도입 등을 통해 프로젝트를 더욱 탄탄하게 만들고 싶음.

### 배운점
- Spring Boot 활용: 회원 관리 및 게시판 기능을 구현하면서, Spring Boot의 다양한 기능들(JPA, Security, Validation 등)을 해봤던 것들은 복습하고 새로운 것을 익히며 실전에 적용할 수 있었음.
- 이메일 인증 및 API 활용 능력 향상: Naver SMTP 서버와 Daum/Kakao API를 연동하면서 외부 API를 활용하는 방법과 문제 해결 능력을 키울 수 있었음.
- DTO와 유효성 검증의 중요성: 데이터를 안전하게 처리하기 위해 DTO를 사용하고, 서버와 클라이언트 측에서 유효성 검사를 강화하여 신뢰성 높은 시스템을 구축할 수 있었음.
- UI/UX 개선의 필요성: 백엔드 기능 구현뿐만 아니라, 깔끔하고 통일된 UI/UX 설계가 기본적이더라도 프로젝트의 완성도에 얼마나 중요한지 깨달음.
