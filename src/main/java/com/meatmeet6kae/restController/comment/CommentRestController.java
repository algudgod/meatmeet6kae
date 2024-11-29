package com.meatmeet6kae.restController.comment;


import com.meatmeet6kae.dto.comment.CommentDto;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.comment.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment") // 댓글 데이터를 JSON으로 반환
public class CommentRestController {

    public CommentRestController(CommentService commentService) {
        this.commentService = commentService;
    }

    private final CommentService commentService;

    @PostMapping("/addComment")
    public ResponseEntity<CommentDto> addComment(@RequestBody CommentDto commentDto, HttpSession session){
        System.out.println("commentDto ????: " + commentDto.toString());

        User user = (User) session.getAttribute("user");
        if (user == null) { // 로그인 정보가 없는 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }session.setAttribute("user", user);
        // 저장 후, 저장된 CommentDto 반환
        CommentDto savedComment = commentService.addComment(commentDto, user);
        // 저장된 데이터를 JSON으로 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }

    @GetMapping("/listComment/{boardNo}")
    public ResponseEntity<List<CommentDto>> getCommentsByBoardNo(@PathVariable int boardNo){
        List<CommentDto> commentDtos = commentService.getCommentsListByBoardNo(boardNo);

        return ResponseEntity.ok(commentDtos);
    }

    @PutMapping("/updateComment/{commentNo}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable int commentNo, @RequestBody CommentDto commentDto, HttpSession session){
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        CommentDto updatedComment = commentService.updateComment(commentDto, user);

        return ResponseEntity.ok(updatedComment);

    }

    @DeleteMapping("/deleteComment/{commentNo}")
    public ResponseEntity<String> deleteComment(@PathVariable int commentNo, HttpSession session){
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        commentService.deleteComment(commentNo, user);

        return ResponseEntity.ok("삭제되었습니다.");
    }


}
