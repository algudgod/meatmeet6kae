package com.meatmeet6kae.restController.comment;


import com.meatmeet6kae.dto.comment.CommentDto;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.entity.comment.Comment;
import com.meatmeet6kae.repository.comment.CommentRepository;
import com.meatmeet6kae.service.comment.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class CommentRestControllerTest {

    @Autowired
    private CommentRestController commentRestController;

    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;

    //@Test
    void updateComment() {

        // 1. 테스트 데이터
        User user = new User();
        user.setLoginId("dcmicrowave");
        user.setNickname("닥터전자레인지");

        Comment comment = new Comment();
        comment.setCommentNo(34);
        comment.setContent("이거 성공하면 수정 버튼 만들거임");

        CommentDto commentDto = new CommentDto();
        commentDto.setCommentNo(34);
        commentDto.setContent("이번 진짜 성공임_진짜최종");

        CommentDto updatedComment = commentService.updateComment(commentDto, user);

        // 3. 결과 검증
        assertNotNull(updatedComment); // 수정된 댓글이 반환되는지 확인
        assertEquals("이번 진짜 성공임_진짜최종", updatedComment.getContent()); // 내용이 업데이트되었는지 확인
        assertNotNull(updatedComment.getUpdateDate()); // 업데이트 날짜가 기록되었는지 확인
    }


    //@Test
    void deleteComment() {

        User user = new User();
        user.setLoginId("dcmicrowave");
        user.setNickname("닥터전자레인지");

        Comment comment = new Comment();
        comment.setCommentNo(34);
        comment.setContent("이번 진짜 성공임_진짜최종");

        commentService.deleteComment(34,user);

        Optional<Comment> deletedComment = commentRepository.findById(34);
        assertTrue(deletedComment.isEmpty()); // 댓글이 삭제되었는지 확인

    }
}